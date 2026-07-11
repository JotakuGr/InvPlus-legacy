# ADR 0011 - Contexto empresarial no JWT

## Decisao

- Usuarios podem possuir vinculos ativos com multiplas empresas.
- Cada access token carrega uma unica `empresa_id` e uma `filial_id` opcional.
- Trocar empresa ou filial emite novos access e refresh tokens.
- Tokens possuem `token_use`: `ACCESS` ou `COMPANY_SELECTION`; refresh tokens opacos preservam o contexto no banco.
- O vinculo empresarial e revalidado em cada requisicao autenticada com contexto.
- `EmpresaContext` fornece identificadores simples aos services e nao carrega entidades JPA.
- O contexto complementa, mas nao substitui, perfis e permissoes.
- A migracao dos modulos sera gradual; durante a compatibilidade, `empresaId` recebido deve coincidir com o JWT.

## Modelo anterior

`Usuario -> Perfis -> Permissoes` e `Empresa -> Filiais` nao possuíam relacionamento entre si.

## Consequencias

Tokens de selecao nao acessam APIs operacionais. Revogar um vinculo bloqueia imediatamente novos requests e refreshes
contextualizados, sem aguardar a expiracao do token.
