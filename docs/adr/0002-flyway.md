# ADR 0002 - Flyway

## Status

Aceito.

## Contexto

O banco precisa evoluir de forma controlada e auditável.

## Decisão

Usar Flyway para versionar schema, constraints, índices e seeds estruturais.

## Consequências

- Histórico de banco fica rastreável.
- Ambientes podem ser reproduzidos.
- Inserts em migrations devem ser idempotentes.
