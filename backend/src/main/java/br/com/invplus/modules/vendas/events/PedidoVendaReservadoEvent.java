package br.com.invplus.modules.vendas.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.util.UUID;

/**
 * Evento emitido quando o pedido de venda conclui a reserva automatica de estoque.
 */
public class PedidoVendaReservadoEvent extends AbstractDomainEvent {

    private final UUID pedidoVendaId;
    private final UUID empresaId;
    private final UUID clienteId;

    public PedidoVendaReservadoEvent(UUID pedidoVendaId, UUID empresaId, UUID clienteId) {
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
