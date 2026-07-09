package br.com.invplus.modules.pagamentos.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload para simular vencimentos e valores de parcelas.
 */
public record SimulacaoCondicaoPagamentoRequest(
        @NotNull(message = "Data base e obrigatoria")
        LocalDate dataBase,
        @NotNull(message = "Valor total e obrigatorio")
        @Positive(message = "Valor total deve ser maior que zero")
        BigDecimal valorTotal
) {
}
