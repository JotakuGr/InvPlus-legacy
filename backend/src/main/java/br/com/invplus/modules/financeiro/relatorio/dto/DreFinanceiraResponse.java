package br.com.invplus.modules.financeiro.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Demonstrativo financeiro de resultado por periodo.
 */
public record DreFinanceiraResponse(
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal receitaBruta,
        BigDecimal despesasOperacionais,
        BigDecimal resultadoOperacional,
        BigDecimal saldoFinal,
        List<DreLinhaResponse> receitas,
        List<DreLinhaResponse> despesas
) {
}
