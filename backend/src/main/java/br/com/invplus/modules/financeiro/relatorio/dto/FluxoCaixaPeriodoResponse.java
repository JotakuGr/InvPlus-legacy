package br.com.invplus.modules.financeiro.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Posicao diaria do fluxo de caixa, preparada para graficos e series historicas.
 */
public record FluxoCaixaPeriodoResponse(
        LocalDate data,
        BigDecimal entradas,
        BigDecimal saidas,
        BigDecimal saldo
) {
}
