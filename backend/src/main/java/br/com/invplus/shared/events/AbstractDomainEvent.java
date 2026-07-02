package br.com.invplus.shared.events;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Implementacao base para eventos ainda publicados em memoria pela aplicacao.
 */
public abstract class AbstractDomainEvent implements DomainEvent {

    private final UUID eventId = UUID.randomUUID();
    private final OffsetDateTime occurredAt = OffsetDateTime.now();

    @Override
    public UUID eventId() {
        return eventId;
    }

    @Override
    public OffsetDateTime occurredAt() {
        return occurredAt;
    }
}
