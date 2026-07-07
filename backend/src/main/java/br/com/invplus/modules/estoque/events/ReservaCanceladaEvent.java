package br.com.invplus.modules.estoque.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento de dominio para cancelamento de reserva de estoque.
 */
public class ReservaCanceladaEvent extends AbstractDomainEvent {

    private final UUID reservaId;
    private final UUID estoqueId;
    private final BigDecimal quantidade;

    public ReservaCanceladaEvent(UUID reservaId, UUID estoqueId, BigDecimal quantidade) {
        this.reservaId = reservaId;
        this.estoqueId = estoqueId;
        this.quantidade = quantidade;
    }

    public UUID reservaId() {
        return reservaId;
    }

    public UUID estoqueId() {
        return estoqueId;
    }

    public BigDecimal quantidade() {
        return quantidade;
    }
}
