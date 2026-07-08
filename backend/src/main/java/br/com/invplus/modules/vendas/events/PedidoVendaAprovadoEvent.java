package br.com.invplus.modules.vendas.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.util.UUID;

/**
 * Evento publicado quando o pedido de venda e aprovado.
 */
public class PedidoVendaAprovadoEvent extends AbstractDomainEvent {

    private final UUID pedidoVendaId;
    private final UUID empresaId;
    private final UUID clienteId;
    private final UUID aprovadoPor;

    public PedidoVendaAprovadoEvent(UUID pedidoVendaId, UUID empresaId, UUID clienteId, UUID aprovadoPor) {
        this.pedidoVendaId = pedidoVendaId;
        this.empresaId = empresaId;
        this.clienteId = clienteId;
        this.aprovadoPor = aprovadoPor;
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

    public UUID aprovadoPor() {
        return aprovadoPor;
    }
}
