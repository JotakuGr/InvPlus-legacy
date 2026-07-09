package br.com.invplus.modules.financeiro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.dto.response.MovimentoFinanceiroResponse;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.modules.financeiro.entity.Recebimento;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.mapper.MovimentoFinanceiroMapper;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.validator.MovimentoFinanceiroValidator;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitarios das regras e integracoes internas de MovimentoFinanceiro.
 */
@ExtendWith(MockitoExtension.class)
class MovimentoFinanceiroServiceTest {

    @Mock
    private MovimentoFinanceiroRepository repository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private FormaPagamentoRepository formaPagamentoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private FornecedorRepository fornecedorRepository;
    @Mock
    private CentroCustoRepository centroCustoRepository;
    @Mock
    private PlanoContaRepository planoContaRepository;

    private MovimentoFinanceiroService service;
    private Empresa empresa;
    private FormaPagamento formaPagamento;
    private Cliente cliente;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        service = new MovimentoFinanceiroService(repository, empresaRepository, filialRepository,
                formaPagamentoRepository, clienteRepository, fornecedorRepository,
                new MovimentoFinanceiroMapper(), new MovimentoFinanceiroValidator(), centroCustoRepository,
                planoContaRepository);
        empresa = empresa();
        formaPagamento = formaPagamento();
        cliente = cliente();
        fornecedor = fornecedor();
    }

    @Test
    void deveCriarMovimentoManualDeEntrada() {
        prepararRelacionamentos(cliente, null);
        when(repository.save(any(MovimentoFinanceiro.class))).thenAnswer(invocation -> {
            MovimentoFinanceiro entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        MovimentoFinanceiroResponse response = service.criarManual(request(
                TipoMovimentoFinanceiro.ENTRADA, OrigemMovimentoFinanceiro.AJUSTE_MANUAL,
                cliente.getId(), null, "150.00"));

        assertThat(response.tipoMovimento()).isEqualTo(TipoMovimentoFinanceiro.ENTRADA);
        assertThat(response.clienteId()).isEqualTo(cliente.getId());
        assertThat(response.valor()).isEqualByComparingTo("150.00");
    }

    @Test
    void deveCriarMovimentoManualDeSaida() {
        prepararRelacionamentos(null, fornecedor);
        when(repository.save(any(MovimentoFinanceiro.class))).thenAnswer(invocation -> {
            MovimentoFinanceiro entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        MovimentoFinanceiroResponse response = service.criarManual(request(
                TipoMovimentoFinanceiro.SAIDA, OrigemMovimentoFinanceiro.TRANSFERENCIA,
                null, fornecedor.getId(), "80.00"));

        assertThat(response.tipoMovimento()).isEqualTo(TipoMovimentoFinanceiro.SAIDA);
        assertThat(response.fornecedorId()).isEqualTo(fornecedor.getId());
    }

    @Test
    void deveBloquearValorNaoPositivoEOrigemAutomaticaManual() {
        prepararRelacionamentos(null, null);

        assertThatThrownBy(() -> service.criarManual(request(
                TipoMovimentoFinanceiro.ENTRADA, OrigemMovimentoFinanceiro.AJUSTE_MANUAL,
                null, null, "0.00")))
                .isInstanceOf(InvalidOperationException.class);

        assertThatThrownBy(() -> service.criarManual(request(
                TipoMovimentoFinanceiro.ENTRADA, OrigemMovimentoFinanceiro.RECEBIMENTO,
                null, null, "10.00")))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deveRegistrarRecebimentoEPagamentoAutomaticamente() {
        when(repository.save(any(MovimentoFinanceiro.class))).thenAnswer(invocation -> {
            MovimentoFinanceiro entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });
        Recebimento recebimento = new Recebimento();
        recebimento.setId(UUID.randomUUID());
        recebimento.setEmpresa(empresa);
        recebimento.setFormaPagamento(formaPagamento);
        recebimento.setCliente(cliente);
        recebimento.setValorLiquido(new BigDecimal("120.00"));
        recebimento.setDataRecebimento(LocalDate.of(2026, 7, 8));

        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setEmpresa(empresa);
        pagamento.setFormaPagamento(formaPagamento);
        pagamento.setFornecedor(fornecedor);
        pagamento.setValorLiquido(new BigDecimal("70.00"));
        pagamento.setDataPagamento(LocalDate.of(2026, 7, 8));

        MovimentoFinanceiro entrada = service.registrarRecebimento(recebimento);
        MovimentoFinanceiro saida = service.registrarPagamento(pagamento);

        assertThat(entrada.getOrigem()).isEqualTo(OrigemMovimentoFinanceiro.RECEBIMENTO);
        assertThat(entrada.getTipoMovimento()).isEqualTo(TipoMovimentoFinanceiro.ENTRADA);
        assertThat(saida.getOrigem()).isEqualTo(OrigemMovimentoFinanceiro.PAGAMENTO);
        assertThat(saida.getTipoMovimento()).isEqualTo(TipoMovimentoFinanceiro.SAIDA);
    }

    @Test
    void deveCancelarMovimentoPorOrigem() {
        MovimentoFinanceiro movimento = new MovimentoFinanceiro();
        movimento.setStatus(StatusMovimentoFinanceiro.ATIVO);
        UUID origemId = UUID.randomUUID();
        when(repository.findByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                OrigemMovimentoFinanceiro.PAGAMENTO, origemId, StatusMovimentoFinanceiro.ATIVO))
                .thenReturn(Optional.of(movimento));

        service.cancelarPorOrigem(OrigemMovimentoFinanceiro.PAGAMENTO, origemId);

        assertThat(movimento.getStatus()).isEqualTo(StatusMovimentoFinanceiro.CANCELADO);
    }

    private void prepararRelacionamentos(Cliente clienteInformado, Fornecedor fornecedorInformado) {
        when(empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(empresa.getId()))
                .thenReturn(Optional.of(empresa));
        when(formaPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(formaPagamento.getId()))
                .thenReturn(Optional.of(formaPagamento));
        if (clienteInformado != null) {
            when(clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(clienteInformado.getId()))
                    .thenReturn(Optional.of(clienteInformado));
        }
        if (fornecedorInformado != null) {
            when(fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedorInformado.getId()))
                    .thenReturn(Optional.of(fornecedorInformado));
        }
    }

    private MovimentoFinanceiroRequest request(TipoMovimentoFinanceiro tipo, OrigemMovimentoFinanceiro origem,
            UUID clienteId, UUID fornecedorId, String valor) {
        return new MovimentoFinanceiroRequest(empresa.getId(), null, formaPagamento.getId(), clienteId,
                fornecedorId, tipo, origem, UUID.randomUUID(), new BigDecimal(valor),
                LocalDate.of(2026, 7, 8), "Movimento de teste", null);
    }

    private Empresa empresa() {
        Empresa entity = new Empresa("Empresa Movimento", "Empresa Movimento", "12345678000199",
                null, null, null);
        entity.setId(UUID.randomUUID());
        return entity;
    }

    private FormaPagamento formaPagamento() {
        FormaPagamento entity = new FormaPagamento();
        entity.setId(UUID.randomUUID());
        entity.atualizar("PIX", "Pix", TipoFormaPagamento.PIX, false, false);
        return entity;
    }

    private Cliente cliente() {
        Pessoa pessoa = pessoa("Cliente Movimento");
        Cliente entity = new Cliente();
        entity.setId(UUID.randomUUID());
        entity.setPessoa(pessoa);
        return entity;
    }

    private Fornecedor fornecedor() {
        Pessoa pessoa = pessoa("Fornecedor Movimento");
        Fornecedor entity = new Fornecedor();
        entity.setId(UUID.randomUUID());
        entity.setPessoa(pessoa);
        return entity;
    }

    private Pessoa pessoa(String nome) {
        Pessoa entity = new Pessoa();
        entity.setId(UUID.randomUUID());
        entity.atualizar(TipoPessoa.JURIDICA, nome, null, "12345678000199",
                null, null, null, null, null);
        return entity;
    }
}
