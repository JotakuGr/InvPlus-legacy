package br.com.invplus.modules.vendas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.PageRequest;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.ReservaEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.service.ReservaEstoqueService;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Funcionario;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.FuncionarioRepository;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.vendas.dto.request.PedidoVendaRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.PedidoVendaItem;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.mapper.PedidoVendaMapper;
import br.com.invplus.modules.vendas.repository.PedidoVendaHistoricoRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaItemRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.modules.vendas.validator.PedidoVendaValidator;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.service.UsuarioEmpresaAccessValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras de cabecalho do PedidoVenda.
 */
@ExtendWith(MockitoExtension.class)
class PedidoVendaServiceTest {

    private final UUID empresaId = UUID.randomUUID();

    @Mock
    private PedidoVendaRepository repository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private FilialRepository filialRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PedidoVendaItemRepository itemRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private ReservaEstoqueService reservaEstoqueService;

    @Mock
    private PedidoVendaHistoricoRepository historicoRepository;

    @Mock
    private AuditorAware<UUID> auditorAware;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmpresaContext empresaContext;

    @Mock
    private UsuarioEmpresaAccessValidator accessValidator;

    private PedidoVendaService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        PedidoVendaMapper mapper = new PedidoVendaMapper();
        PedidoVendaValidator validator = new PedidoVendaValidator(repository);
        VendaHistoricoService historicoService = new VendaHistoricoService(historicoRepository, auditorAware);
        when(empresaContext.getEmpresaId()).thenReturn(empresaId);
        service = new PedidoVendaService(
                repository,
                mapper,
                validator,
                empresaRepository,
                filialRepository,
                clienteRepository,
                funcionarioRepository,
                itemRepository,
                estoqueRepository,
                reservaEstoqueService,
                historicoService,
                eventPublisher,
                empresaContext,
                accessValidator
        );
    }

    /**
     * Garante criacao com status inicial ABERTO e valor zerado.
     */
    @Test
    void deveCriarPedidoVendaComSucesso() {
        Empresa empresa = empresaAtiva();
        Cliente cliente = clienteAtivo();
        Funcionario vendedor = funcionarioAtivo();
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "PV-001"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(cliente.getId())).thenReturn(Optional.of(cliente));
        when(funcionarioRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(vendedor.getId())).thenReturn(Optional.of(vendedor));
        when(repository.save(any(PedidoVenda.class))).thenAnswer(invocation -> {
            PedidoVenda pedido = invocation.getArgument(0);
            pedido.setId(UUID.randomUUID());
            return pedido;
        });

        PedidoVendaResponse response = service.criar(request(null, cliente.getId(), vendedor.getId(), "PV-001"));

        assertThat(response.status()).isEqualTo(StatusPedidoVenda.ABERTO);
        assertThat(response.valorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.clienteNome()).isEqualTo("Cliente Venda");
        assertThat(response.vendedorNome()).isEqualTo("Vendedor Venda");
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    /**
     * Bloqueia empresa inexistente ou inativa.
     */
    @Test
    void deveBloquearEmpresaInexistente() {
        UUID clienteId = UUID.randomUUID();
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresaId, "PV-001"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request(null, clienteId, null, "PV-001")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Empresa nao encontrada");
    }

    /**
     * Bloqueia cliente inexistente ou inativo.
     */
    @Test
    void deveBloquearClienteInexistente() {
        Empresa empresa = empresaAtiva();
        UUID clienteId = UUID.randomUUID();
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "PV-001"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(clienteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request(null, clienteId, null, "PV-001")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente nao encontrado");
    }

    /**
     * Bloqueia numero duplicado dentro da mesma empresa.
     */
    @Test
    void deveBloquearNumeroDuplicadoPorEmpresa() {
        Empresa empresa = empresaAtiva();
        PedidoVenda existente = pedidoAtivo(empresa, clienteAtivo());
        when(repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresa.getId(), "PV-001"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request(null, UUID.randomUUID(), null, "PV-001")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe pedido de venda ativo");
    }

    /**
     * Bloqueia desconto e acrescimo negativos.
     */
    @Test
    void deveBloquearValoresNegativos() {
        PedidoVendaRequest request = new PedidoVendaRequest(
                null,
                UUID.randomUUID(),
                null,
                "PV-NEG",
                LocalDate.now(),
                null,
                BigDecimal.valueOf(-1),
                BigDecimal.ZERO,
                null
        );

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Desconto nao pode ser negativo");
    }

    /**
     * Aprova apenas pedidos abertos.
     */
    @Test
    void deveAprovarPedidoAberto() {
        PedidoVenda pedido = pedidoAtivo(empresaAtiva(), clienteAtivo());
        PedidoVendaItem item = itemPedido(pedido);
        Estoque estoque = estoqueDisponivel(pedido.getEmpresa(), item.getProduto());
        when(repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId(), empresaId))
                .thenReturn(Optional.of(pedido));
        when(itemRepository.findByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId()))
                .thenReturn(java.util.List.of(item));
        when(estoqueRepository.findDisponiveisParaReserva(item.getProduto().getId(), pedido.getEmpresa().getId(), item.getQuantidade()))
                .thenReturn(java.util.List.of(estoque));
        when(reservaEstoqueService.criar(any(ReservaEstoqueRequest.class))).thenReturn(reservaResponse(estoque, pedido.getId()));
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(UUID.randomUUID()));

        PedidoVendaResponse response = service.aprovar(pedido.getId());

        assertThat(response.status()).isEqualTo(StatusPedidoVenda.APROVADO);
        assertThat(response.aprovadoEm()).isNotNull();
        verify(reservaEstoqueService).criar(any(ReservaEstoqueRequest.class));
    }

    /**
     * Cancela pedidos ainda nao faturados.
     */
    @Test
    void deveCancelarPedido() {
        PedidoVenda pedido = pedidoAtivo(empresaAtiva(), clienteAtivo());
        when(repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId(), empresaId))
                .thenReturn(Optional.of(pedido));

        PedidoVendaResponse response = service.cancelar(pedido.getId());

        assertThat(response.status()).isEqualTo(StatusPedidoVenda.CANCELADO);
        assertThat(response.canceladoEm()).isNotNull();
    }

    /**
     * Bloqueia alteracao depois do cancelamento.
     */
    @Test
    void deveBloquearAlteracaoDePedidoCancelado() {
        Empresa empresa = empresaAtiva();
        Cliente cliente = clienteAtivo();
        PedidoVenda pedido = pedidoAtivo(empresa, cliente);
        pedido.setStatus(StatusPedidoVenda.CANCELADO);
        when(repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedido.getId(), empresaId))
                .thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.atualizar(pedido.getId(),
                request(null, cliente.getId(), null, "PV-001")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Pedido cancelado ou faturado nao pode ser alterado");
    }

    /**
     * Mantem a listagem sempre paginada e filtrada por specification.
     */
    @SuppressWarnings("unchecked")
    @Test
    void deveListarComFiltrosPaginado() {
        when(repository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(null, null, null, "PV", StatusPedidoVenda.ABERTO,
                LocalDate.now().minusDays(1), LocalDate.now(), true, PageRequest.of(0, 20));

        verify(repository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class));
    }

    private PedidoVendaRequest request(UUID filialId, UUID clienteId, UUID vendedorId, String numero) {
        return new PedidoVendaRequest(
                filialId,
                clienteId,
                vendedorId,
                numero,
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 10),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "Pedido de venda"
        );
    }

    private PedidoVenda pedidoAtivo(Empresa empresa, Cliente cliente) {
        PedidoVenda pedido = new PedidoVenda();
        pedido.setId(UUID.randomUUID());
        pedido.setEmpresa(empresa);
        pedido.setCliente(cliente);
        pedido.atualizar("PV-001", LocalDate.of(2026, 7, 7), null, BigDecimal.ZERO, BigDecimal.ZERO, null);
        return pedido;
    }

    private PedidoVendaItem itemPedido(PedidoVenda pedido) {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCodigo("PROD-VENDA");
        produto.setNome("Produto venda");

        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setId(UUID.randomUUID());
        unidadeMedida.atualizar("UN", "Unidade", (short) 0);

        ProdutoUnidade unidade = new ProdutoUnidade();
        unidade.setId(UUID.randomUUID());
        unidade.setProduto(produto);
        unidade.setUnidadeMedida(unidadeMedida);
        unidade.atualizar(BigDecimal.ONE, true, false, true, true, false, "Unidade venda");

        PedidoVendaItem item = new PedidoVendaItem();
        item.setId(UUID.randomUUID());
        item.setPedidoVenda(pedido);
        item.setProduto(produto);
        item.setProdutoUnidade(unidade);
        item.atualizar(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, "Item");
        return item;
    }

    private Estoque estoqueDisponivel(Empresa empresa, Produto produto) {
        Deposito deposito = new Deposito();
        deposito.setId(UUID.randomUUID());
        deposito.setEmpresa(empresa);
        deposito.setNome("Deposito");
        deposito.setPrincipal(true);

        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setId(UUID.randomUUID());
        localizacao.setDeposito(deposito);
        localizacao.setCodigo("LOC");
        localizacao.setPrincipal(true);

        Estoque estoque = new Estoque();
        estoque.setId(UUID.randomUUID());
        estoque.setEmpresa(empresa);
        estoque.setProduto(produto);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(BigDecimal.valueOf(10), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null, null, null);
        return estoque;
    }

    private ReservaEstoqueResponse reservaResponse(Estoque estoque, UUID pedidoId) {
        return new ReservaEstoqueResponse(UUID.randomUUID(), estoque.getId(), estoque.getProduto().getId(),
                estoque.getProduto().getCodigo(), estoque.getProduto().getNome(), estoque.getEmpresa().getId(),
                estoque.getEmpresa().getRazaoSocial(), null, null, estoque.getDeposito().getId(),
                estoque.getDeposito().getNome(), estoque.getLocalizacao().getId(), estoque.getLocalizacao().getCodigo(),
                BigDecimal.ONE, StatusReservaEstoque.ATIVA, MovimentacaoOrigem.VENDA, pedidoId, null, null, null,
                "Reserva", true, null, null);
    }

    private Empresa empresaAtiva() {
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        empresa.setNomeFantasia("Empresa Venda");
        return empresa;
    }

    private Cliente clienteAtivo() {
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setPessoa(pessoa("Cliente Venda", "cliente-venda@teste.com"));
        return cliente;
    }

    private Funcionario funcionarioAtivo() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(UUID.randomUUID());
        funcionario.setPessoa(pessoa("Vendedor Venda", "vendedor-venda@teste.com"));
        return funcionario;
    }

    private Pessoa pessoa(String nome, String email) {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.FISICA, nome, null, cpfUnico(), null,
                "7133334444", "71999998888", email, null);
        return pessoa;
    }

    private String cpfUnico() {
        return "123" + "%08d".formatted(Math.abs(System.nanoTime() % 100_000_000));
    }
}
