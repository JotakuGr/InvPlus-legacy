package br.com.invplus.modules.compras.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Item do pedido de compra com informacoes resumidas do produto.
 */
public record PedidoCompraItemResponse(
        UUID id,
        UUID pedidoCompraId,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        UUID produtoUnidadeId,
        String unidadeSigla,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
