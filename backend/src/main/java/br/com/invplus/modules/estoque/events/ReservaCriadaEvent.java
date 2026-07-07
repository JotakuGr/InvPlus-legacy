package br.com.invplus.modules.estoque.events;

import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.shared.events.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento de dominio para criacao de reserva de estoque.
 */
public class ReservaCriadaEvent extends AbstractDomainEvent {

    private final UUID reservaId;
    private final UUID estoqueId;
    private final BigDecimal quantidade;
    private final MovimentacaoOrigem origem;
    private final UUID origemId;

    public ReservaCriadaEvent(UUID reservaId, UUID estoqueId, BigDecimal quantidade, MovimentacaoOrigem origem, UUID origemId) {
        this.reservaId = reservaId;
        this.estoqueId = estoqueId;
        this.quantidade = quantidade;
        this.origem = origem;
        this.origemId = origemId;
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

    public MovimentacaoOrigem origem() {
        return origem;
    }

    public UUID origemId() {
        return origemId;
    }
}
