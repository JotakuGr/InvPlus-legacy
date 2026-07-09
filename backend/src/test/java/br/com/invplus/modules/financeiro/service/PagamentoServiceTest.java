package br.com.invplus.modules.financeiro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.PagamentoRequest;
import br.com.invplus.modules.financeiro.dto.response.PagamentoResponse;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.modules.financeiro.enums.StatusParcelaPagar;
import br.com.invplus.modules.financeiro.enums.StatusPagamentoFinanceiro;
import br.com.invplus.modules.financeiro.mapper.PagamentoMapper;
import br.com.invplus.modules.financeiro.repository.ParcelaPagarRepository;
import br.com.invplus.modules.financeiro.repository.PagamentoRepository;
import br.com.invplus.modules.financeiro.validator.PagamentoValidator;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
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
 * Testes unitarios das regras de Pagamento.
 */
@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository repository;
    @Mock
    private ParcelaPagarRepository parcelaPagarRepository;
    @Mock
    private FormaPagamentoRepository formaPagamentoRepository;
    @Mock
    private ParcelaPagarService parcelaPagarService;
    @Mock
    private MovimentoFinanceiroService movimentoFinanceiroService;

    private PagamentoService service;
    private ParcelaPagar parcela;
    private FormaPagamento formaPagamento;

    @BeforeEach
    void setUp() {
        service = new PagamentoService(repository, parcelaPagarRepository, formaPagamentoRepository,
                parcelaPagarService, movimentoFinanceiroService, new PagamentoMapper(), new PagamentoValidator());
        parcela = parcela("300.00");
        formaPagamento = formaPagamento();
    }

    @Test
    void deveCriarPagamentoTotalComSucesso() {
        prepararBuscaParcelaEForma();
        when(repository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        PagamentoResponse response = service.criar(request("300.00"));

        assertThat(response.status()).isEqualTo(StatusPagamentoFinanceiro.ATIVO);
        assertThat(response.valorPago()).isEqualByComparingTo("300.00");
        assertThat(response.valorLiquido()).isEqualByComparingTo("300.00");
        verify(parcelaPagarService).baixar(any(), any());
    }

    @Test
    void deveCriarPagamentoParcialComSucesso() {
        prepararBuscaParcelaEForma();
        when(repository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        PagamentoResponse response = service.criar(request("100.00"));

        assertThat(response.valorPago()).isEqualByComparingTo("100.00");
        verify(parcelaPagarService).baixar(any(), any());
    }

    @Test
    void deveBloquearParcelaInexistente() {
        when(parcelaPagarRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request("100.00")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcela");
    }

    @Test
    void deveBloquearParcelaPaga() {
        parcela.setStatus(StatusParcelaPagar.PAGA);
        prepararBuscaParcelaEForma();

        assertThatThrownBy(() -> service.criar(request("100.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("paga");
    }

    @Test
    void deveBloquearParcelaCancelada() {
        parcela.setStatus(StatusParcelaPagar.CANCELADA);
        prepararBuscaParcelaEForma();

        assertThatThrownBy(() -> service.criar(request("100.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void deveBloquearFormaPagamentoInexistente() {
        when(parcelaPagarRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));
        when(formaPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(formaPagamento.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request("100.00")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Forma de pagamento");
    }

    @Test
    void deveBloquearValorZero() {
        prepararBuscaParcelaEForma();

        assertThatThrownBy(() -> service.criar(request("0.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void deveBloquearValorAcimaDoSaldo() {
        prepararBuscaParcelaEForma();

        assertThatThrownBy(() -> service.criar(request("301.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ultrapassar");
    }

    @Test
    void deveCancelarPagamentoComEstorno() {
        Pagamento pagamento = pagamentoAtivo();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(pagamento.getId())).thenReturn(Optional.of(pagamento));

        PagamentoResponse response = service.cancelar(pagamento.getId());

        assertThat(response.status()).isEqualTo(StatusPagamentoFinanceiro.CANCELADO);
        verify(parcelaPagarService).estornarPagamento(parcela.getId(), pagamento.getValorPago(),
                pagamento.getJuros(), pagamento.getMulta(), pagamento.getDesconto(),
                "Estorno do pagamento " + pagamento.getId());
    }

    private void prepararBuscaParcelaEForma() {
        when(parcelaPagarRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));
        when(formaPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(formaPagamento.getId())).thenReturn(Optional.of(formaPagamento));
    }

    private PagamentoRequest request(String valor) {
        return new PagamentoRequest(parcela.getId(), formaPagamento.getId(), new BigDecimal(valor),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.of(2026, 7, 10), "PIX");
    }

    private Pagamento pagamentoAtivo() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.registrar(parcela, formaPagamento, new BigDecimal("100.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.of(2026, 7, 10), "PIX");
        return pagamento;
    }

    private ParcelaPagar parcela(String valor) {
        ContaPagar conta = conta();
        ParcelaPagar entity = new ParcelaPagar();
        entity.setId(UUID.randomUUID());
        entity.abrir(conta, 1, LocalDate.of(2026, 7, 10), new BigDecimal(valor), null);
        return entity;
    }

    private ContaPagar conta() {
        Empresa empresa = new Empresa("Empresa Teste", "Empresa Teste", "12345678000199", null, null, null);
        empresa.setId(UUID.randomUUID());
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor Teste", null, "12345678000188", null, null, null, null, null);
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(UUID.randomUUID());
        fornecedor.setPessoa(pessoa);
        ContaPagar conta = new ContaPagar();
        conta.setId(UUID.randomUUID());
        conta.abrir(empresa, null, fornecedor, null, "CP-1", "Conta teste", LocalDate.of(2026, 7, 8),
                new BigDecimal("300.00"), null);
        return conta;
    }

    private FormaPagamento formaPagamento() {
        FormaPagamento entity = new FormaPagamento();
        entity.setId(UUID.randomUUID());
        entity.atualizar("PIX", "Pix", TipoFormaPagamento.PIX, false, false);
        return entity;
    }
}
