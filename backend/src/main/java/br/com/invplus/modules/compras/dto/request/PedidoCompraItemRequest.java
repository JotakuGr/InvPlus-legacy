package br.com.invplus.modules.compras.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dados de produto comprado dentro de um pedido.
 */
public record PedidoCompraItemRequest(
        @NotNull UUID pedidoCompraId,
        @NotNull UUID produtoId,
        UUID produtoUnidadeId,
        @NotNull @DecimalMin(value = "0.000001") BigDecimal quantidade,
        @NotNull @DecimalMin("0.00") BigDecimal valorUnitario,
        String observacao
) {
}
