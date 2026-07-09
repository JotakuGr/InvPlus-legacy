package br.com.invplus.modules.financeiro.relatorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
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
 * Testes dos calculos da DRE financeira agrupada por plano de conta.
 */
@ExtendWith(MockitoExtension.class)
class FinanceiroDreServiceTest {

    @Mock
    private MovimentoFinanceiroRepository movimentoRepository;
    @Mock
    private ContaReceberRepository contaReceberRepository;
    @Mock
    private ContaPagarRepository contaPagarRepository;

    private FinanceiroResumoService service;
    private final LocalDate inicio = LocalDate.of(2026, 7, 1);
    private final LocalDate fim = LocalDate.of(2026, 7, 31);

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new FinanceiroResumoService(movimentoRepository, contaReceberRepository, contaPagarRepository);
    }

    @Test
    void deveCalcularDreComReceitasDespesasEResultado() {
        UUID empresaId = UUID.randomUUID();
        when(movimentoRepository.somarReceitasPorPlanoConta(inicio, fim, empresaId, null, null, null))
                .thenReturn(List.<Object[]>of(
                        new Object[]{"3.1", "Receita de Vendas", new BigDecimal("1500.00")}));
        when(movimentoRepository.somarDespesasPorPlanoConta(inicio, fim, empresaId, null, null, null))
                .thenReturn(List.<Object[]>of(
                        new Object[]{"4.1", "Despesa Operacional", new BigDecimal("425.50")}));

        var dre = service.gerarDre(inicio, fim, empresaId, null, null, null);

        assertThat(dre.receitaBruta()).isEqualByComparingTo("1500.00");
        assertThat(dre.despesasOperacionais()).isEqualByComparingTo("425.50");
        assertThat(dre.resultadoOperacional()).isEqualByComparingTo("1074.50");
        assertThat(dre.saldoFinal()).isEqualByComparingTo("1074.50");
        assertThat(dre.receitas()).extracting("codigo").containsExactly("3.1");
        assertThat(dre.despesas()).extracting("nome").containsExactly("Despesa Operacional");
    }

    @Test
    void deveAplicarFiltrosDaDreAoRepository() {
        UUID empresaId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        UUID centroCustoId = UUID.randomUUID();
        UUID planoContaId = UUID.randomUUID();
        when(movimentoRepository.somarReceitasPorPlanoConta(
                inicio, fim, empresaId, filialId, centroCustoId, planoContaId))
                .thenReturn(List.<Object[]>of(
                        new Object[]{"3.1", "Receita Filtrada", new BigDecimal("800.00")}));
        when(movimentoRepository.somarDespesasPorPlanoConta(
                inicio, fim, empresaId, filialId, centroCustoId, planoContaId))
                .thenReturn(List.of());

        var dre = service.gerarDre(inicio, fim, empresaId, filialId, centroCustoId, planoContaId);

        assertThat(dre.receitaBruta()).isEqualByComparingTo("800.00");
        assertThat(dre.despesasOperacionais()).isZero();
        assertThat(dre.resultadoOperacional()).isEqualByComparingTo("800.00");
    }

    @Test
    void deveBloquearPeriodoInvalidoNaDre() {
        assertThatThrownBy(() -> service.gerarDre(fim, inicio, null, null, null, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Data final");
    }
}
