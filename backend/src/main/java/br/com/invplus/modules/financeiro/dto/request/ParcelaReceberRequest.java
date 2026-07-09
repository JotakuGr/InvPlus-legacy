package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados aceitos para criar ou editar uma parcela a receber.
 */
public record ParcelaReceberRequest(
        @NotNull UUID contaReceberId,
        @NotNull @Positive Integer numeroParcela,
        @NotNull LocalDate dataVencimento,
        @NotNull @Positive BigDecimal valor,
        String observacao
) {
}
