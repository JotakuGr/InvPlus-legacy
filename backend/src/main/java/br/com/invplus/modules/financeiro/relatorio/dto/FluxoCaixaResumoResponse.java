package br.com.invplus.modules.financeiro.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Totais consolidados de entradas, saidas e saldo em um periodo.
 */
public record FluxoCaixaResumoResponse(
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldo,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
