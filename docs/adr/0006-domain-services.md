# ADR 0006 - Domain Services

## Status

Aceito.

## Contexto

Algumas regras pertencem ao domínio e serão chamadas por vários módulos.

## Decisão

Criar services de domínio, como `EstoqueDomainService`, para centralizar regras críticas.

## Consequências

- Módulos externos não alteram saldo diretamente.
- Movimentação, reserva e disponibilidade ficam sincronizadas.
- Reduz duplicação em Compras, Vendas e Inventário.
