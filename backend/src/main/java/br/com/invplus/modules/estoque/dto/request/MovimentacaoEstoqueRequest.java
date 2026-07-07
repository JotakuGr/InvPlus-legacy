package br.com.invplus.modules.estoque.dto.request;

import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Payload interno para registrar uma movimentacao de estoque.
 */
public record MovimentacaoEstoqueRequest(
        @NotNull UUID estoqueId,
        @NotNull TipoMovimentacaoEstoque tipoMovimentacao,
        @NotNull @DecimalMin(value = "0.000", inclusive = false) BigDecimal quantidade,
        @NotNull BigDecimal saldoAnterior,
        @NotNull BigDecimal saldoPosterior,
        MovimentacaoOrigem origem,
        UUID origemId,
        String documento,
        String observacao,
        OffsetDateTime dataMovimentacao
) {
}
