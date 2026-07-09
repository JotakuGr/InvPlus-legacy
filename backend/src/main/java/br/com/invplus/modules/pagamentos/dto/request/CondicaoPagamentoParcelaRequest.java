package br.com.invplus.modules.pagamentos.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Payload de uma parcela da condicao de pagamento.
 */
public record CondicaoPagamentoParcelaRequest(
        @NotNull(message = "Numero da parcela e obrigatorio")
        @Min(value = 1, message = "Numero da parcela deve ser maior ou igual a 1")
        Integer numeroParcela,
        @NotNull(message = "Dias da parcela e obrigatorio")
        @Min(value = 0, message = "Dias da parcela nao pode ser negativo")
        Integer dias,
        @NotNull(message = "Percentual da parcela e obrigatorio")
        @DecimalMin(value = "0.0001", message = "Percentual da parcela deve ser maior que zero")
        @DecimalMax(value = "100.0000", message = "Percentual da parcela deve ser menor ou igual a 100")
        BigDecimal percentual
) {
}
