package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Valores informados para baixa parcial ou total de parcela a pagar.
 */
public record ParcelaPagarBaixaRequest(
        @NotNull(message = "Valor pago e obrigatorio")
        @Positive(message = "Valor pago deve ser maior que zero")
        BigDecimal valorPago,
        @PositiveOrZero(message = "Juros nao podem ser negativos")
        BigDecimal juros,
        @PositiveOrZero(message = "Multa nao pode ser negativa")
        BigDecimal multa,
        @PositiveOrZero(message = "Desconto nao pode ser negativo")
        BigDecimal desconto,
        @NotNull(message = "Data de pagamento e obrigatoria")
        LocalDate dataPagamento,
        String observacao
) {
}
