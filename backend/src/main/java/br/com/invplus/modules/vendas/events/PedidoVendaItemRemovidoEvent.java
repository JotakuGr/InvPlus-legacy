package br.com.invplus.modules.vendas.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.util.UUID;

/**
 * Evento publicado quando um item de pedido de venda e removido logicamente.
 */
public class PedidoVendaItemRemovidoEvent extends AbstractDomainEvent {

    private final UUID pedidoVendaItemId;
    private final UUID pedidoVendaId;
    private final UUID produtoId;

    public PedidoVendaItemRemovidoEvent(UUID pedidoVendaItemId, UUID pedidoVendaId, UUID produtoId) {
        this.pedidoVendaItemId = pedidoVendaItemId;
        this.pedidoVendaId = pedidoVendaId;
        this.produtoId = produtoId;
    }

    public UUID pedidoVendaItemId() {
        return pedidoVendaItemId;
    }

    public UUID pedidoVendaId() {
        return pedidoVendaId;
    }

    public UUID produtoId() {
        return produtoId;
    }
}
