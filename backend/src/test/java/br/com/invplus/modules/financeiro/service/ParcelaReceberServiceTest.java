package br.com.invplus.modules.financeiro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.BaixaParcelaReceberRequest;
import br.com.invplus.modules.financeiro.dto.request.GerarParcelasReceberRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaReceberRequest;
import br.com.invplus.modules.financeiro.dto.response.ParcelaReceberResponse;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.enums.StatusParcelaReceber;
import br.com.invplus.modules.financeiro.mapper.ParcelaReceberMapper;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaReceberRepository;
import br.com.invplus.modules.financeiro.validator.ParcelaReceberValidator;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamentoParcela;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitarios das regras de ParcelaReceber.
 */
@ExtendWith(MockitoExtension.class)
class ParcelaReceberServiceTest {

    @Mock
    private ParcelaReceberRepository repository;
    @Mock
    private ContaReceberRepository contaReceberRepository;
    @Mock
    private CondicaoPagamentoRepository condicaoPagamentoRepository;

    private ParcelaReceberService service;
    private ContaReceber conta;

    @BeforeEach
    void setUp() {
        service = new ParcelaReceberService(repository, contaReceberRepository, condicaoPagamentoRepository,
                new GeradorParcelasService(new CalculadoraVencimentoService()), new ParcelaReceberMapper(),
                new ParcelaReceberValidator(repository));
        conta = conta();
    }

    @Test
    void deveCriarParcelaComSucesso() {
        when(contaReceberRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));
        when(repository.findByContaReceberIdAndNumeroParcelaAndAtivoTrueAndDeletedAtIsNull(conta.getId(), 1))
                .thenReturn(Optional.empty());
        when(repository.save(any(ParcelaReceber.class))).thenAnswer(invocation -> {
            ParcelaReceber parcela = invocation.getArgument(0);
            parcela.setId(UUID.randomUUID());
            return parcela;
        });

        ParcelaReceberResponse response = service.criar(request(1, "300.00"));

        assertThat(response.status()).isEqualTo(StatusParcelaReceber.ABERTA);
        assertThat(response.valorPago()).isEqualByComparingTo("0.00");
        assertThat(response.valorAberto()).isEqualByComparingTo("300.00");
    }

    @Test
    void deveBloquearNumeroDuplicado() {
        ParcelaReceber existente = parcela(1, "300.00");
        when(contaReceberRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));
        when(repository.findByContaReceberIdAndNumeroParcelaAndAtivoTrueAndDeletedAtIsNull(conta.getId(), 1))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(request(1, "300.00")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ja existe parcela");
    }

    @Test
    void deveBloquearContaCancelada() {
        conta.setStatus(StatusContaReceber.CANCELADA);
        when(contaReceberRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.criar(request(1, "300.00")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void deveGerarParcelasPelaCondicaoPagamento() {
        CondicaoPagamento condicao = condicaoTresParcelas();
        when(contaReceberRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(Optional.of(conta));
        when(repository.existsByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(false);
        when(condicaoPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(condicao.getId())).thenReturn(Optional.of(condicao));
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<ParcelaReceber> parcelas = invocation.getArgument(0);
            parcelas.forEach(parcela -> parcela.setId(UUID.randomUUID()));
            return parcelas;
        });

        List<ParcelaReceberResponse> response = service.gerarParcelas(conta.getId(),
                new GerarParcelasReceberRequest(condicao.getId(), LocalDate.of(2026, 7, 10)));

        assertThat(response).hasSize(3);
        assertThat(response).extracting(ParcelaReceberResponse::valor)
                .containsExactly(new BigDecimal("300.00"), new BigDecimal("300.00"), new BigDecimal("300.00"));
    }

    @Test
    void deveBaixarParcelaTotalEAtualizarConta() {
        ParcelaReceber parcela = parcela(1, "300.00");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));
        when(repository.findByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(List.of(parcela));

        ParcelaReceberResponse response = service.baixar(parcela.getId(),
                new BaixaParcelaReceberRequest(new BigDecimal("300.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, LocalDate.of(2026, 7, 10), "PIX"));

        assertThat(response.status()).isEqualTo(StatusParcelaReceber.PAGA);
        assertThat(conta.getValorPago()).isEqualByComparingTo("300.00");
        assertThat(conta.getStatus()).isEqualTo(StatusContaReceber.PARCIALMENTE_PAGA);
    }

    @Test
    void deveBaixarParcelaParcial() {
        ParcelaReceber parcela = parcela(1, "300.00");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));
        when(repository.findByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(conta.getId())).thenReturn(List.of(parcela));

        ParcelaReceberResponse response = service.baixar(parcela.getId(),
                new BaixaParcelaReceberRequest(new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, LocalDate.of(2026, 7, 10), "Parcial"));

        assertThat(response.status()).isEqualTo(StatusParcelaReceber.PARCIALMENTE_PAGA);
        assertThat(response.valorAberto()).isEqualByComparingTo("200.00");
    }

    @Test
    void deveBloquearBaixaAcimaDoSaldo() {
        ParcelaReceber parcela = parcela(1, "300.00");
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(parcela.getId())).thenReturn(Optional.of(parcela));

        assertThatThrownBy(() -> service.baixar(parcela.getId(),
                new BaixaParcelaReceberRequest(new BigDecimal("301.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, LocalDate.of(2026, 7, 10), null)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ultrapassar");
    }

    private ParcelaReceberRequest request(int numero, String valor) {
        return new ParcelaReceberRequest(conta.getId(), numero, LocalDate.of(2026, 7, 10), new BigDecimal(valor), "Teste");
    }

    private ParcelaReceber parcela(int numero, String valor) {
        ParcelaReceber parcela = new ParcelaReceber();
        parcela.setId(UUID.randomUUID());
        parcela.abrir(conta, numero, LocalDate.of(2026, 7, 10), new BigDecimal(valor), null);
        return parcela;
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
        ContaReceber entity = new ContaReceber();
        entity.setId(UUID.randomUUID());
        entity.abrir(empresa, null, cliente, null, "CR-1", "Conta CR-1", LocalDate.of(2026, 7, 8),
                new BigDecimal("900.00"), null);
        return entity;
    }

    private CondicaoPagamento condicaoTresParcelas() {
        CondicaoPagamento condicao = new CondicaoPagamento();
        condicao.setId(UUID.randomUUID());
        condicao.atualizar("3X", "Tres parcelas", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                RegraVencimento.MANTER_DATA, true);
        condicao.substituirParcelas(List.of(parcelaCondicao(condicao, 1, 0), parcelaCondicao(condicao, 2, 30),
                parcelaCondicao(condicao, 3, 60)));
        return condicao;
    }

    private CondicaoPagamentoParcela parcelaCondicao(CondicaoPagamento condicao, int numero, int dias) {
        CondicaoPagamentoParcela parcela = new CondicaoPagamentoParcela();
        parcela.atualizar(condicao, numero, dias, new BigDecimal("33.3333"));
        if (numero == 3) {
            parcela.setPercentual(new BigDecimal("33.3334"));
        }
        return parcela;
    }
}
