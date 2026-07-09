package br.com.invplus.modules.financeiro.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado calculado para uma parcela financeira.
 */
public record ParcelaGerada(
        Integer numeroParcela,
        LocalDate vencimento,
        BigDecimal valor
) {
}
