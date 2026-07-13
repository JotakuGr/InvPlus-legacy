package br.com.invplus.modules.financeiro.relatorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes dos calculos consolidados dos relatorios financeiros.
 */
@ExtendWith(MockitoExtension.class)
class FinanceiroResumoServiceTest {

    @Mock
    private MovimentoFinanceiroRepository movimentoRepository;
    @Mock
    private ContaReceberRepository contaReceberRepository;
    @Mock
    private ContaPagarRepository contaPagarRepository;
    @Mock
    private EmpresaContext empresaContext;

    private FinanceiroResumoService service;
    private final LocalDate inicio = LocalDate.of(2026, 7, 1);
    private final LocalDate fim = LocalDate.of(2026, 7, 31);

    @BeforeEach
    void setUp() {
        service = new FinanceiroResumoService(
                movimentoRepository, contaReceberRepository, contaPagarRepository, empresaContext);
    }

    @Test
    void deveCalcularEntradasSaidasESaldo() {
        when(movimentoRepository.somarPorTipoNoPeriodo(TipoMovimentoFinanceiro.ENTRADA, inicio, fim))
                .thenReturn(new BigDecimal("1500.00"));
        when(movimentoRepository.somarPorTipoNoPeriodo(TipoMovimentoFinanceiro.SAIDA, inicio, fim))
                .thenReturn(new BigDecimal("425.50"));

        var resumo = service.fluxoCaixa(inicio, fim);

        assertThat(resumo.totalEntradas()).isEqualByComparingTo("1500.00");
        assertThat(resumo.totalSaidas()).isEqualByComparingTo("425.50");
        assertThat(resumo.saldo()).isEqualByComparingTo("1074.50");
    }

    @Test
    void deveTratarSomasNulasComoZero() {
        var resumo = service.fluxoCaixa(inicio, fim);

        assertThat(resumo.totalEntradas()).isZero();
        assertThat(resumo.totalSaidas()).isZero();
        assertThat(resumo.saldo()).isZero();
    }

    @Test
    void deveMontarDashboardComContasAbertasEVencidas() {
        when(movimentoRepository.somarPorTipoNoPeriodo(TipoMovimentoFinanceiro.ENTRADA, inicio, fim))
                .thenReturn(new BigDecimal("900.00"));
        when(movimentoRepository.somarPorTipoNoPeriodo(TipoMovimentoFinanceiro.SAIDA, inicio, fim))
                .thenReturn(new BigDecimal("300.00"));
        when(contaReceberRepository.somarValorAberto()).thenReturn(new BigDecimal("1200.00"));
        when(contaPagarRepository.somarValorAberto()).thenReturn(new BigDecimal("700.00"));
        when(contaReceberRepository.countByStatusAndAtivoTrueAndDeletedAtIsNull(StatusContaReceber.VENCIDA))
                .thenReturn(3L);
        when(contaPagarRepository.countByStatusAndAtivoTrueAndDeletedAtIsNull(StatusContaPagar.VENCIDA))
                .thenReturn(2L);

        var dashboard = service.dashboard(inicio, fim);

        assertThat(dashboard.contasReceberAberto()).isEqualByComparingTo("1200.00");
        assertThat(dashboard.contasPagarAberto()).isEqualByComparingTo("700.00");
        assertThat(dashboard.saldoPeriodo()).isEqualByComparingTo("600.00");
        assertThat(dashboard.contasReceberVencidas()).isEqualTo(3L);
        assertThat(dashboard.contasPagarVencidas()).isEqualTo(2L);
    }

    @Test
    void deveBloquearPeriodoInvalido() {
        assertThatThrownBy(() -> service.fluxoCaixa(fim, inicio))
                .isInstanceOf(InvalidOperationException.class);
    }
}
