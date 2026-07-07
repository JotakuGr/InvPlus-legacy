package br.com.invplus.modules.compras.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Produto recebido e local de destino na entrada de mercadoria.
 */
public record EntradaMercadoriaItemRequest(
        @NotNull UUID entradaMercadoriaId,
        UUID pedidoCompraItemId,
        @NotNull UUID produtoId,
        UUID produtoUnidadeId,
        @NotNull UUID depositoId,
        @NotNull UUID localizacaoId,
        @NotNull @DecimalMin(value = "0.000001") BigDecimal quantidade,
        @NotNull @DecimalMin("0.00") BigDecimal valorUnitario,
        String observacao
) {
}
