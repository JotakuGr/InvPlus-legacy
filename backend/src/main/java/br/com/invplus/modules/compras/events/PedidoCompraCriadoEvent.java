package br.com.invplus.modules.compras.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando um pedido de compra nasce no sistema.
 */
public class PedidoCompraCriadoEvent extends AbstractDomainEvent {

    private final UUID pedidoCompraId;
    private final UUID empresaId;
    private final UUID fornecedorId;
    private final BigDecimal valorTotal;

    public PedidoCompraCriadoEvent(UUID pedidoCompraId, UUID empresaId, UUID fornecedorId, BigDecimal valorTotal) {
        this.pedidoCompraId = pedidoCompraId;
        this.empresaId = empresaId;
        this.fornecedorId = fornecedorId;
        this.valorTotal = valorTotal;
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

    public BigDecimal valorTotal() {
        return valorTotal;
    }
}
