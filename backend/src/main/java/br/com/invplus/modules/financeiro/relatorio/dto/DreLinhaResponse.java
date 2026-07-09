package br.com.invplus.modules.financeiro.relatorio.dto;

import java.math.BigDecimal;

/**
 * Linha sintetica da DRE agrupada por plano de conta.
 */
public record DreLinhaResponse(
        String codigo,
        String nome,
        BigDecimal valor
) {
}
