package br.com.invplus.modules.compras.dto.response;

import br.com.invplus.modules.compras.enums.StatusPedidoCompra;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Linha de historico de status do pedido de compra.
 */
public record PedidoCompraHistoricoResponse(
        UUID id,
        UUID pedidoCompraId,
        StatusPedidoCompra statusAnterior,
        StatusPedidoCompra novoStatus,
        UUID usuarioId,
        OffsetDateTime dataAlteracao,
        String observacao
) {
}
