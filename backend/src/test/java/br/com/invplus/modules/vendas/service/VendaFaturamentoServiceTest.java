package br.com.invplus.modules.vendas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.entity.ReservaEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.modules.estoque.service.ReservaEstoqueService;
import br.com.invplus.modules.financeiro.dto.response.ContaReceberResponse;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.service.ContaReceberService;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.vendas.dto.request.VendaFaturamentoRequest;
import br.com.invplus.modules.vendas.dto.response.VendaFaturamentoResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.VendaFaturamento;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.enums.StatusVendaFaturamento;
import br.com.invplus.modules.vendas.mapper.VendaFaturamentoMapper;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.modules.vendas.repository.VendaFaturamentoRepository;
import br.com.invplus.modules.vendas.validator.VendaFaturamentoValidator;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.security.context.EmpresaContext;

/**
 * Testes unitarios do fluxo de faturamento da venda.
 */
@ExtendWith(MockitoExtension.class)
class VendaFaturamentoServiceTest {

    private final UUID empresaId = UUID.randomUUID();

    @Mock
    private VendaFaturamentoRepository repository;
    @Mock
    private PedidoVendaRepository pedidoRepository;
    @Mock
    private ReservaEstoqueRepository reservaRepository;
    @Mock
    private ReservaEstoqueService reservaService;
    @Mock
    private ContaReceberService contaReceberService;
    @Mock
    private VendaHistoricoService historicoService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private EmpresaContext empresaContext;

    private VendaFaturamentoService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        when(empresaContext.getEmpresaId()).thenReturn(empresaId);
        VendaFaturamentoMapper mapper = new VendaFaturamentoMapper();
        VendaFaturamentoValidator validator = new VendaFaturamentoValidator(repository, reservaRepository);
        service = new VendaFaturamentoService(
                repository,
                pedidoRepository,
                reservaRepository,
                reservaService,
                contaReceberService,
                mapper,
                validator,
                historicoService,
                eventPublisher,
                empresaContext
        );
    }

    @Test
    void deveFaturarPedidoAprovadoComReservasAtivas() {
        PedidoVenda pedido = pedido(StatusPedidoVenda.APROVADO);
        ReservaEstoque reserva = reserva(pedido);
        when(pedidoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId(), empresaId))
                .thenReturn(Optional.of(pedido));
        when(repository.existsByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId())).thenReturn(false);
        when(repository.existsByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(pedido.getEmpresa().getId(), "VF-001"))
                .thenReturn(false);
        when(reservaRepository.existsByOrigemAndOrigemIdAndStatusAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA, pedido.getId(), StatusReservaEstoque.ATIVA, empresaId)).thenReturn(true);
        when(reservaRepository.findByOrigemAndOrigemIdAndStatusAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA, pedido.getId(), StatusReservaEstoque.ATIVA, empresaId)).thenReturn(List.of(reserva));
        when(repository.save(any(VendaFaturamento.class))).thenAnswer(invocation -> {
            VendaFaturamento faturamento = invocation.getArgument(0);
            faturamento.setId(UUID.randomUUID());
            return faturamento;
        });
        ContaReceber conta = contaReceber(pedido);
        when(contaReceberService.gerarPorVendaFaturamento(any(VendaFaturamento.class), any(UUID.class), any()))
                .thenReturn(contaResponse(conta));
        when(contaReceberService.buscarEntidade(conta.getId())).thenReturn(conta);

        VendaFaturamentoResponse response = service.faturar(request(pedido.getId(), "VF-001"));

        assertThat(response.status()).isEqualTo(StatusVendaFaturamento.FATURADO);
        assertThat(response.valorTotal()).isEqualByComparingTo("150.00");
        assertThat(response.pedidoVendaId()).isEqualTo(pedido.getId());
        assertThat(response.contaReceberId()).isEqualTo(conta.getId());
        assertThat(pedido.getStatus()).isEqualTo(StatusPedidoVenda.FATURADO);
        verify(reservaService).baixar(reserva.getId());
        verify(historicoService).registrarPedido(pedido, StatusPedidoVenda.APROVADO, StatusPedidoVenda.FATURADO,
                "Pedido de venda faturado");
        verify(eventPublisher, times(3)).publishEvent(any(Object.class));
    }

    @Test
    void deveBloquearPedidoInexistente() {
        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedidoId, empresaId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.faturar(request(pedidoId, "VF-404")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pedido de venda nao encontrado");
    }

    @Test
    void deveBloquearPedidoNaoAprovado() {
        PedidoVenda pedido = pedido(StatusPedidoVenda.ABERTO);
        when(pedidoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId(), empresaId))
                .thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.faturar(request(pedido.getId(), "VF-ABERTO")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Somente pedido aprovado pode ser faturado");
    }

    @Test
    void deveBloquearPedidoSemReservaAtiva() {
        PedidoVenda pedido = pedido(StatusPedidoVenda.APROVADO);
        when(pedidoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId(), empresaId))
                .thenReturn(Optional.of(pedido));
        when(repository.existsByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId())).thenReturn(false);
        when(repository.existsByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(pedido.getEmpresa().getId(), "VF-SEM-RES"))
                .thenReturn(false);
        when(reservaRepository.existsByOrigemAndOrigemIdAndStatusAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA, pedido.getId(), StatusReservaEstoque.ATIVA, empresaId)).thenReturn(false);

        assertThatThrownBy(() -> service.faturar(request(pedido.getId(), "VF-SEM-RES")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("reserva ativa");
    }

    @Test
    void deveBloquearPedidoJaFaturado() {
        PedidoVenda pedido = pedido(StatusPedidoVenda.APROVADO);
        when(pedidoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId(), empresaId))
                .thenReturn(Optional.of(pedido));
        when(repository.existsByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.faturar(request(pedido.getId(), "VF-DUP")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ja possui faturamento");
    }

    private VendaFaturamentoRequest request(UUID pedidoId, String numero) {
        return new VendaFaturamentoRequest(pedidoId, UUID.randomUUID(), numero, "Conta " + numero, "Faturar");
    }

    private ContaReceber contaReceber(PedidoVenda pedido) {
        ContaReceber conta = new ContaReceber();
        conta.setId(UUID.randomUUID());
        conta.abrir(pedido.getEmpresa(), null, pedido.getCliente(), null, "CR-" + pedido.getNumero(),
                "Conta do pedido", LocalDate.now(), pedido.getValorTotal(), null);
        return conta;
    }

    private ContaReceberResponse contaResponse(ContaReceber conta) {
        return new ContaReceberResponse(
                conta.getId(),
                conta.getEmpresa().getId(),
                conta.getEmpresa().getNomeFantasia(),
                null,
                null,
                conta.getCliente().getId(),
                conta.getCliente().getPessoa().getNome(),
                null,
                conta.getNumero(),
                conta.getDescricao(),
                conta.getDataEmissao(),
                conta.getValorTotal(),
                BigDecimal.ZERO,
                conta.getValorTotal(),
                StatusContaReceber.ABERTA,
                null,
                true,
                null,
                null
        );
    }

    private PedidoVenda pedido(StatusPedidoVenda status) {
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        empresa.atualizar("Empresa Venda", "Empresa Venda", cnpjUnico(), "ISENTO", null,
                "NORMAL", 3, "71999990000", "empresa-venda@teste.com");

        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente Venda", "Cliente", cnpjUnico(), "ISENTO",
                "7133334444", null, "cliente-venda@teste.com", null);

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setPessoa(pessoa);

        PedidoVenda pedido = new PedidoVenda();
        pedido.setId(UUID.randomUUID());
        pedido.setEmpresa(empresa);
        pedido.setCliente(cliente);
        pedido.atualizar("PV-" + UUID.randomUUID(), LocalDate.now(), null, BigDecimal.ZERO, BigDecimal.ZERO, "Pedido");
        pedido.atualizarValorTotal(new BigDecimal("150.00"));
        pedido.setStatus(status);
        return pedido;
    }

    private ReservaEstoque reserva(PedidoVenda pedido) {
        Estoque estoque = new Estoque();
        estoque.setId(UUID.randomUUID());
        estoque.setProduto(new Produto());
        estoque.setEmpresa(pedido.getEmpresa());
        estoque.setDeposito(new Deposito());
        estoque.setLocalizacao(new LocalizacaoEstoque());
        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setId(UUID.randomUUID());
        reserva.vincularEstoque(estoque);
        reserva.atualizarDados(new BigDecimal("10.000"), MovimentacaoOrigem.VENDA, pedido.getId(), null, "Reserva");
        reserva.setStatus(StatusReservaEstoque.ATIVA);
        return reserva;
    }

    private String cnpjUnico() {
        return "7%013d".formatted(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10_000_000_000_000L);
    }
}
