package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Solicita a geração de parcelas de uma conta usando uma condição de pagamento.
 */
public record GerarParcelasReceberRequest(
        @NotNull UUID condicaoPagamentoId,
        @NotNull LocalDate dataBase
) {
}
