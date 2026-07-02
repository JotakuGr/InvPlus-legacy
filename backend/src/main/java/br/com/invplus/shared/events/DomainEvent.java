package br.com.invplus.shared.events;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Contrato base para eventos de dominio publicados entre modulos.
 */
public interface DomainEvent {

    /**
     * Identificador unico do evento para rastreio e idempotencia futura.
     */
    UUID eventId();

    /**
     * Momento em que o evento foi criado no dominio.
     */
    OffsetDateTime occurredAt();
}
