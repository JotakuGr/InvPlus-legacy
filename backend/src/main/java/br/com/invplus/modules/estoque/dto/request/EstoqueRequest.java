package br.com.invplus.modules.estoque.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados de entrada para criar ou ajustar o saldo atual de estoque.
 */
public record EstoqueRequest(
        @NotNull UUID produtoId,
        UUID filialId,
        @NotNull UUID depositoId,
        @NotNull UUID localizacaoId,
        @DecimalMin("0.000") BigDecimal quantidade,
        @DecimalMin("0.000") BigDecimal quantidadeReservada,
        @DecimalMin("0.000") BigDecimal quantidadeAvariada,
        @DecimalMin("0.000") BigDecimal quantidadeTransito,
        @DecimalMin("0.000") BigDecimal estoqueMinimo,
        @DecimalMin("0.000") BigDecimal estoqueMaximo,
        OffsetDateTime ultimoMovimentoEm,
        UUID ultimoMovimentoId
) {
}
