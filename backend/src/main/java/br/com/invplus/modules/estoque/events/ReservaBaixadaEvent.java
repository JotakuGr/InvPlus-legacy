package br.com.invplus.modules.estoque.events;

import br.com.invplus.shared.events.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento de dominio para baixa de reserva de estoque.
 */
public class ReservaBaixadaEvent extends AbstractDomainEvent {

    private final UUID reservaId;
    private final UUID estoqueId;
    private final BigDecimal quantidade;

    public ReservaBaixadaEvent(UUID reservaId, UUID estoqueId, BigDecimal quantidade) {
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
