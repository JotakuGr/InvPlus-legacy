package br.com.invplus.modules.vendas.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dados para criar ou atualizar item de venda; o preco unitario e resolvido pelo backend.
 */
public record PedidoVendaItemRequest(
        @NotNull UUID pedidoVendaId,
        @NotNull UUID produtoId,
        @NotNull UUID produtoPrecoId,
        @NotNull UUID produtoUnidadeId,
        @NotNull @DecimalMin(value = "0.000001") BigDecimal quantidade,
        @PositiveOrZero BigDecimal desconto,
        @PositiveOrZero BigDecimal acrescimo,
        String observacao
) {
}
