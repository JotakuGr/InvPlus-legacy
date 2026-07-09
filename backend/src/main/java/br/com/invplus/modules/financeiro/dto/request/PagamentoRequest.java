package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados de entrada para registrar o pagamento de uma parcela.
 */
public record PagamentoRequest(
        @NotNull UUID parcelaPagarId,
        @NotNull UUID formaPagamentoId,
        @NotNull @Positive BigDecimal valorPago,
        @PositiveOrZero BigDecimal juros,
        @PositiveOrZero BigDecimal multa,
        @PositiveOrZero BigDecimal desconto,
        @NotNull LocalDate dataPagamento,
        String observacao
) {
}
