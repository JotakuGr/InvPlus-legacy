package br.com.invplus.modules.compras.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Visao agregada do pedido para telas de compra e conferencia.
 */
public record PedidoCompraCompletoResponse(
        PedidoCompraResponse pedido,
        List<PedidoCompraItemResponse> itens,
        BigDecimal quantidadeTotal,
        BigDecimal valorTotal,
        List<EntradaMercadoriaResponse> entradasVinculadas,
        List<PedidoCompraHistoricoResponse> historico
) {
}
