package br.com.invplus.modules.financeiro.service;

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

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.RecebimentoRequest;
import br.com.invplus.modules.financeiro.dto.response.RecebimentoResponse;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.entity.Recebimento;
import br.com.invplus.modules.financeiro.enums.StatusParcelaReceber;
import br.com.invplus.modules.financeiro.enums.StatusRecebimento;
import br.com.invplus.modules.financeiro.mapper.RecebimentoMapper;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaReceberRepository;
import br.com.invplus.modules.financeiro.repository.RecebimentoRepository;
import br.com.invplus.modules.financeiro.validator.RecebimentoValidator;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras de Recebimento.
 */
@ExtendWith(MockitoExtension.class)
class RecebimentoServiceTest {

    @Mock
    private RecebimentoRepository repository;
    @Mock
    private ParcelaReceberRepository parcelaReceberRepository;
    @Mock
    private FormaPagamentoRepository formaPagamentoRepository;
    @Mock
    private ParcelaReceberService parcelaReceberService;
    @Mock
    private MovimentoFinanceiroService movimentoFinanceiroService;
    @Mock
    private ContaBancariaRepository contaBancariaRepository;
    @Mock
    private EmpresaContext empresaContext;

    private RecebimentoService service;
    private ParcelaReceber parcela;
    private FormaPagamento formaPagamento;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new RecebimentoService(repository, parcelaReceberRepository, formaPagamentoRepository,
                parcelaReceberService, movimentoFinanceiroService, new RecebimentoMapper(),
                new RecebimentoValidator(), contaBancariaRepository, empresaContext);
        parcela = parcela("300.00");
        formaPagamento = formaPagamento();
    }

    @Test
    void deveCriarRecebimentoTotalComSucesso() {
        prepararBuscaParcelaEForma();
        when(repository.save(any(Recebimento.class))).thenAnswer(invocation -> {
            Recebimento entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        RecebimentoResponse response = service.criar(request("300.00"));

        assertThat(response.status()).isEqualTo(StatusRecebimento.ATIVO);
        assertThat(response.valorRecebido()).isEqualByComparingTo("300.00");
        assertThat(response.valorLiquido()).isEqualByComparingTo("300.00");
        verify(parcelaReceberService).baixar(any(), any());
    }

    @Test
    void deveCriarRecebimentoParcialComSucesso() {
        prepararBuscaParcelaEForma();
        when(repository.save(any(Recebimento.class))).thenAnswer(invocation -> {
            Recebimento entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        RecebimentoResponse response = service.criar(request("100.00"));

        assertThat(response.valorRecebido()).isEqualByComparingTo("100.00");
        verify(parcelaReceberService).baixar(any(), any());
    }

    @Test
    void deveBloquearParcelaInexistente() {
        when(parcelaReceberRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request("100.00")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcela");
    }

    @Test
    void deveBloquearParcelaPaga() {
        parcela.setStatus(StatusParcelaReceber.PAGA);
        prepararBuscaParcelaEForma();

        assertThatThrownBy(() -> service.criar(request("100.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("paga");
    }

    @Test
    void deveBloquearParcelaCancelada() {
        parcela.setStatus(StatusParcelaReceber.CANCELADA);
        prepararBuscaParcelaEForma();

        assertThatThrownBy(() -> service.criar(request("100.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void deveBloquearFormaPagamentoInexistente() {
        when(parcelaReceberRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));
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
    void deveCancelarRecebimentoComEstorno() {
        Recebimento recebimento = recebimentoAtivo();
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(recebimento.getId())).thenReturn(Optional.of(recebimento));

        RecebimentoResponse response = service.cancelar(recebimento.getId());

        assertThat(response.status()).isEqualTo(StatusRecebimento.CANCELADO);
        verify(parcelaReceberService).estornarRecebimento(parcela.getId(), recebimento.getValorRecebido(),
                recebimento.getJuros(), recebimento.getMulta(), recebimento.getDesconto(),
                "Estorno do recebimento " + recebimento.getId());
    }

    private void prepararBuscaParcelaEForma() {
        when(parcelaReceberRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));
        when(formaPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(formaPagamento.getId())).thenReturn(Optional.of(formaPagamento));
        ContaBancaria conta = new ContaBancaria();
        conta.setEmpresa(parcela.getContaReceber().getEmpresa());
        when(contaBancariaRepository.findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(
                parcela.getContaReceber().getEmpresa().getId())).thenReturn(Optional.of(conta));
    }

    private RecebimentoRequest request(String valor) {
        return new RecebimentoRequest(parcela.getId(), formaPagamento.getId(), new BigDecimal(valor),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.of(2026, 7, 10), "PIX");
    }

    private Recebimento recebimentoAtivo() {
        Recebimento recebimento = new Recebimento();
        recebimento.setId(UUID.randomUUID());
        recebimento.registrar(parcela, formaPagamento, new BigDecimal("100.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.of(2026, 7, 10), "PIX");
        return recebimento;
    }

    private ParcelaReceber parcela(String valor) {
        ContaReceber conta = conta();
        ParcelaReceber entity = new ParcelaReceber();
        entity.setId(UUID.randomUUID());
        entity.abrir(conta, 1, LocalDate.of(2026, 7, 10), new BigDecimal(valor), null);
        return entity;
    }

    private ContaReceber conta() {
        Empresa empresa = new Empresa("Empresa Teste", "Empresa Teste", "12345678000199", null, null, null);
        empresa.setId(UUID.randomUUID());
        Pessoa pessoa = new Pessoa();
        pessoa.setId(UUID.randomUUID());
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente Teste", null, "12345678000188", null, null, null, null, null);
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setPessoa(pessoa);
        ContaReceber conta = new ContaReceber();
        conta.setId(UUID.randomUUID());
        conta.abrir(empresa, null, cliente, null, "CR-1", "Conta teste", LocalDate.of(2026, 7, 8),
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
