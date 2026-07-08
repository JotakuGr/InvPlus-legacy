package br.com.invplus.modules.vendas.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando um pedido de venda nasce no sistema.
 */
public class PedidoVendaCriadoEvent extends AbstractDomainEvent {

    private final UUID pedidoVendaId;
    private final UUID empresaId;
    private final UUID clienteId;
    private final BigDecimal valorTotal;

    public PedidoVendaCriadoEvent(UUID pedidoVendaId, UUID empresaId, UUID clienteId, BigDecimal valorTotal) {
        this.pedidoVendaId = pedidoVendaId;
        this.empresaId = empresaId;
        this.clienteId = clienteId;
        this.valorTotal = valorTotal;
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

    public BigDecimal valorTotal() {
        return valorTotal;
    }
}
