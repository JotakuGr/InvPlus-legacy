package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Parametros para gerar parcelas de uma conta a pagar.
 */
public record GerarParcelasPagarRequest(
        @NotNull(message = "Condicao de pagamento e obrigatoria")
        UUID condicaoPagamentoId,
        @NotNull(message = "Data base e obrigatoria")
        LocalDate dataBase
) {
}
