package br.com.invplus.modules.estoque.dto.request;

import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados de entrada para criar uma reserva de estoque.
 */
public record ReservaEstoqueRequest(
        @NotNull UUID estoqueId,
        @NotNull @DecimalMin(value = "0.000", inclusive = false) BigDecimal quantidade,
        @NotNull MovimentacaoOrigem origem,
        UUID origemId,
        OffsetDateTime expiraEm,
        String observacao
) {
}
