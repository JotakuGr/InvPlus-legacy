package br.com.invplus.modules.financeiro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.financeiro.dto.response.ContaReceberResponse;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.mapper.ContaReceberMapper;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.validator.ContaReceberValidator;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.VendaFaturamento;
import br.com.invplus.modules.vendas.repository.VendaFaturamentoRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testa a geracao automatica do financeiro a partir de uma venda faturada.
 */
@ExtendWith(MockitoExtension.class)
class ContaReceberPorVendaServiceTest {

    @Mock
    private ContaReceberRepository repository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private VendaFaturamentoRepository vendaFaturamentoRepository;
    @Mock
    private ParcelaReceberService parcelaReceberService;
    @Mock
    private CentroCustoRepository centroCustoRepository;
    @Mock
    private PlanoContaRepository planoContaRepository;

    private ContaReceberService service;

    @BeforeEach
    void setUp() {
        service = new ContaReceberService(
                repository,
                empresaRepository,
                filialRepository,
                clienteRepository,
                vendaFaturamentoRepository,
                parcelaReceberService,
                centroCustoRepository,
                planoContaRepository,
                new ContaReceberMapper(),
                new ContaReceberValidator(repository)
        );
    }

    @Test
    void deveGerarContaReceberPorVendaFaturadaComParcelas() {
        VendaFaturamento faturamento = faturamento();
        UUID condicaoPagamentoId = UUID.randomUUID();
        when(repository.existsByVendaFaturamentoIdAndAtivoTrueAndDeletedAtIsNull(faturamento.getId())).thenReturn(false);
        when(repository.save(any(ContaReceber.class))).thenAnswer(invocation -> {
            ContaReceber conta = invocation.getArgument(0);
            conta.setId(UUID.randomUUID());
            return conta;
        });
        when(parcelaReceberService.gerarPorContaReceber(any(UUID.class), any(UUID.class), any(LocalDate.class)))
                .thenReturn(List.of());

        ContaReceberResponse response = service.gerarPorVendaFaturamento(
                faturamento,
                condicaoPagamentoId,
                "Financeiro da venda"
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.vendaFaturamentoId()).isEqualTo(faturamento.getId());
        assertThat(response.valorTotal()).isEqualByComparingTo("300.00");
        assertThat(response.valorAberto()).isEqualByComparingTo("300.00");
        assertThat(response.status()).isEqualTo(StatusContaReceber.ABERTA);
        verify(parcelaReceberService).gerarPorContaReceber(response.id(), condicaoPagamentoId,
                faturamento.getDataFaturamento().toLocalDate());
    }

    @Test
    void deveBloquearContaDuplicadaParaMesmaVenda() {
        VendaFaturamento faturamento = faturamento();
        when(repository.existsByVendaFaturamentoIdAndAtivoTrueAndDeletedAtIsNull(faturamento.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.gerarPorVendaFaturamento(faturamento, UUID.randomUUID()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ja possui conta a receber");
    }

    private VendaFaturamento faturamento() {
        PedidoVenda pedido = pedido();
        VendaFaturamento faturamento = new VendaFaturamento();
        faturamento.setId(UUID.randomUUID());
        faturamento.faturar(pedido, "VF-001", "Faturamento");
        return faturamento;
    }

    private PedidoVenda pedido() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.atualizar("Empresa Venda", "Empresa Venda", "12345678000199", "ISENTO", null,
                "NORMAL", 3, "71999990000", "empresa-venda@teste.com");

        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente Venda", null, "12345678000188", null,
                null, null, null, null);

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setPessoa(pessoa);

        PedidoVenda pedido = new PedidoVenda();
        pedido.setId(UUID.randomUUID());
        pedido.setEmpresa(empresa);
        pedido.setCliente(cliente);
        pedido.atualizar("PV-001", LocalDate.now(), null, BigDecimal.ZERO, BigDecimal.ZERO, "Pedido");
        pedido.atualizarValorTotal(new BigDecimal("300.00"));
        return pedido;
    }
}
