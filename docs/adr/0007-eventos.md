# ADR 0007 - Eventos de Domínio

## Status

Preparado.

## Contexto

Fluxos como venda, compra, estoque e financeiro geram efeitos colaterais futuros.

## Decisão

Criar eventos de domínio internos antes de conectar mensageria externa.

## Consequências

- Auditoria, cache e notificações podem evoluir sem reescrever services.
- RabbitMQ pode ser conectado depois.
- Eventos documentam mudanças importantes de estado.
