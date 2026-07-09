# Permissões

As permissões seguem o padrão:

```text
MODULO:ACAO
MODULO:*
```

Exemplos:

```text
PRODUTO:CRIAR
PRODUTO:VISUALIZAR
PRODUTO:EDITAR
PRODUTO:EXCLUIR
PRODUTO:*
```

## Wildcards

Permissões `*: *` por módulo liberam ações daquele domínio.

Exemplos:

- `CADASTROS:*`
- `PESSOAS:*`
- `PRODUTO:*`
- `ESTOQUE:*`
- `PEDIDO_VENDA:*`
- `CONTA_RECEBER:*`

## Status HTTP

| Situação | Status |
|---|---|
| Usuário não autenticado | 401 |
| Usuário autenticado sem permissão | 403 |

## Perfil ADMIN

As migrations de permissões vinculam permissões administrativas ao perfil `ADMIN`, mantendo o ambiente inicial pronto para testes e desenvolvimento.

## Convenção em Controllers

Controllers usam `@PreAuthorize` com a permissão específica e o wildcard do módulo.

Exemplo:

```java
@PreAuthorize("hasAuthority('EMPRESA:CRIAR') or hasAuthority('EMPRESA:*') or hasAuthority('CADASTROS:*')")
```
