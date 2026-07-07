package br.com.invplus.modules.compras.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.util.UUID;

/**
 * Evento publicado apos aprovacao de pedido de compra.
 */
public class PedidoCompraAprovadoEvent extends AbstractDomainEvent {

    private final UUID pedidoCompraId;
    private final UUID empresaId;
    private final UUID fornecedorId;
    private final UUID aprovadoPor;

    public PedidoCompraAprovadoEvent(UUID pedidoCompraId, UUID empresaId, UUID fornecedorId, UUID aprovadoPor) {
        this.pedidoCompraId = pedidoCompraId;
        this.empresaId = empresaId;
        this.fornecedorId = fornecedorId;
        this.aprovadoPor = aprovadoPor;
    }

    public UUID pedidoCompraId() {
        return pedidoCompraId;
    }

    public UUID empresaId() {
        return empresaId;
    }

    public UUID fornecedorId() {
        return fornecedorId;
    }

    public UUID aprovadoPor() {
        return aprovadoPor;
    }
}
