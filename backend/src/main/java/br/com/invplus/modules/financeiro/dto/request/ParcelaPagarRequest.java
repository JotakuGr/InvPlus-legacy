package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload para criar ou atualizar parcela a pagar.
 */
public record ParcelaPagarRequest(
        @NotNull(message = "Conta a pagar e obrigatoria")
        UUID contaPagarId,
        @NotNull(message = "Numero da parcela e obrigatorio")
        @Positive(message = "Numero da parcela deve ser maior que zero")
        Integer numeroParcela,
        @NotNull(message = "Data de vencimento e obrigatoria")
        LocalDate dataVencimento,
        @NotNull(message = "Valor e obrigatorio")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal valor,
        String observacao
) {
}
