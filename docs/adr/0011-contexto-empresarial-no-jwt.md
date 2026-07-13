# ADR 0011 - Contexto empresarial no JWT

## Decisao

- Usuarios podem possuir vinculos ativos com multiplas empresas.
- Cada access token carrega uma unica `empresa_id` e uma `filial_id` opcional.
- Trocar empresa ou filial emite novos access e refresh tokens.
- Tokens possuem `token_use`: `ACCESS` ou `COMPANY_SELECTION`; refresh tokens opacos preservam o contexto no banco.
- O vinculo empresarial e revalidado em cada requisicao autenticada com contexto.
- `EmpresaContext` fornece identificadores simples aos services e nao carrega entidades JPA.
- O contexto complementa, mas nao substitui, perfis e permissoes.
- Endpoints operacionais nao recebem `empresaId`; a empresa ativa vem exclusivamente do access token.

## Modelo anterior

`Usuario -> Perfis -> Permissoes` e `Empresa -> Filiais` nao possuíam relacionamento entre si.

## Consequencias

Tokens de selecao nao acessam APIs operacionais. Revogar um vinculo bloqueia imediatamente novos requests e refreshes
contextualizados, sem aguardar a expiracao do token.
### Isolamento do modulo bancario

O modulo bancario passou a resolver a empresa ativa por `EmpresaContext`. Buscas de conta,
extrato, lancamento, importacao, conciliacao e movimento incorporam a empresa na consulta ao
repository. Consultas agregadas da central e dos relatorios financeiros tambem exigem a empresa
ativa, evitando vazamento por totais ou contagens.

Recursos de outra empresa consultados por UUID sao tratados como inexistentes (`404`). Tentativas
de selecionar ou trocar para um contexto sem vinculo ativo retornam `403`.

### Remocao da compatibilidade legada

Depois da migracao do nucleo empresarial, os parametros `empresaId` foram removidos dos endpoints,
requests e filtros operacionais. A empresa ativa passou a ser determinada exclusivamente pelo
`EmpresaContext`, preenchido pelo access token contextualizado. Para mudar de empresa, o cliente
deve utilizar o endpoint proprio de troca de contexto.

### Remocao do legado no Estoque

Depositos, localizacoes, saldos, movimentacoes e reservas passaram a consumir exclusivamente
o `EmpresaContext` em suas APIs publicas. `empresaId` foi removido dos requests, filtros e
controllers do modulo; valores empresariais internos continuam presentes para persistencia,
auditoria e integracao entre services. Nenhuma migration foi necessaria.

### Remocao do legado em Compras

PedidoCompra, EntradaMercadoria e seus itens passaram a derivar a empresa do token.
Consultas, aggregates e integracoes com Estoque sao filtrados pela empresa ativa. Fornecedor
e Produto permanecem globais no schema atual e serao tratados em decisao separada.

### Remocao do legado em Vendas

PedidoVenda, itens, faturamento e leitura agregada passaram a obter a empresa exclusivamente
do `EmpresaContext`. Os contratos publicos nao recebem `empresaId`; repositories e
Specifications aplicam a empresa ativa obrigatoriamente. Reserva, movimentacao de estoque e
ContaReceber preservam o contexto do pedido. Cadastros globais sem relacionamento empresarial
continuam fora deste escopo e exigirao evolucao de schema propria.
