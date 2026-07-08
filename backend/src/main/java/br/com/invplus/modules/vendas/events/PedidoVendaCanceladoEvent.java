package br.com.invplus.modules.vendas.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.util.UUID;

/**
 * Evento publicado quando o pedido de venda e cancelado.
 */
public class PedidoVendaCanceladoEvent extends AbstractDomainEvent {

    private final UUID pedidoVendaId;
    private final UUID empresaId;
    private final UUID clienteId;

    public PedidoVendaCanceladoEvent(UUID pedidoVendaId, UUID empresaId, UUID clienteId) {
        this.pedidoVendaId = pedidoVendaId;
        this.empresaId = empresaId;
        this.clienteId = clienteId;
    }

    public UUID pedidoVendaId() {
        return pedidoVendaId;
    }

    public UUID empresaId() {
        return empresaId;
    }

    public UUID clienteId() {
        return clienteId;
    }
}
