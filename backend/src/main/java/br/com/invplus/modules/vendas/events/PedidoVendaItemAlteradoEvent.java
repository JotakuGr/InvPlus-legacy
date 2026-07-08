package br.com.invplus.modules.vendas.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando um item de pedido de venda e alterado.
 */
public class PedidoVendaItemAlteradoEvent extends AbstractDomainEvent {

    private final UUID pedidoVendaItemId;
    private final UUID pedidoVendaId;
    private final UUID produtoId;
    private final BigDecimal valorTotal;

    public PedidoVendaItemAlteradoEvent(UUID pedidoVendaItemId, UUID pedidoVendaId, UUID produtoId, BigDecimal valorTotal) {
        this.pedidoVendaItemId = pedidoVendaItemId;
        this.pedidoVendaId = pedidoVendaId;
        this.produtoId = produtoId;
        this.valorTotal = valorTotal;
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

    public BigDecimal valorTotal() {
        return valorTotal;
    }
}
