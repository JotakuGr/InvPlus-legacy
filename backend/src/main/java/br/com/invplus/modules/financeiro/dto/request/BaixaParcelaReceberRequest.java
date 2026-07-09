package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados de baixa de parcela, mantendo juros, multa e desconto separados do principal.
 */
public record BaixaParcelaReceberRequest(
        @NotNull @Positive BigDecimal valorPago,
        @PositiveOrZero BigDecimal juros,
        @PositiveOrZero BigDecimal multa,
        @PositiveOrZero BigDecimal desconto,
        @NotNull LocalDate dataPagamento,
        String observacao
) {
}
