package br.com.invplus.modules.vendas.dto.response;

import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Item de historico de transicao de status do pedido de venda.
 */
public record PedidoVendaHistoricoResponse(
        UUID id,
        UUID pedidoVendaId,
        StatusPedidoVenda statusAnterior,
        StatusPedidoVenda statusNovo,
        String observacao,
        UUID criadoPor,
        OffsetDateTime criadoEm
) {
}
