package br.com.invplus.modules.financeiro.relatorio.dto;

import java.math.BigDecimal;

/**
 * Indicadores sinteticos para o dashboard financeiro do ERP.
 */
public record FinanceiroDashboardResponse(
        BigDecimal contasReceberAberto,
        BigDecimal contasPagarAberto,
        BigDecimal entradasPeriodo,
        BigDecimal saidasPeriodo,
        BigDecimal saldoPeriodo,
        Long contasReceberVencidas,
        Long contasPagarVencidas
) {
}
