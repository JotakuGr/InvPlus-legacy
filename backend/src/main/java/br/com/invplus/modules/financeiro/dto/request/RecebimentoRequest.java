package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados de entrada para registrar o recebimento de uma parcela.
 */
public record RecebimentoRequest(
        @NotNull UUID parcelaReceberId,
        @NotNull UUID formaPagamentoId,
        @NotNull @Positive BigDecimal valorRecebido,
        @PositiveOrZero BigDecimal juros,
        @PositiveOrZero BigDecimal multa,
        @PositiveOrZero BigDecimal desconto,
        @NotNull LocalDate dataRecebimento,
        String observacao
) {
}
