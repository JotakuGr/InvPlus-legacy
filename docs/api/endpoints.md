# Endpoints da API

Base path:

```text
/api/v1
```

Todos os endpoints protegidos exigem JWT no header `Authorization: Bearer <token>`, exceto rotas públicas de autenticação e arquivos públicos liberados em desenvolvimento.

## Convenção por Módulo

Recursos CRUD usam, por padrão:

```text
POST   /api/v1/recurso
GET    /api/v1/recurso?page=0&size=20&sort=campo,asc
GET    /api/v1/recurso/{id}
PUT    /api/v1/recurso/{id}
DELETE /api/v1/recurso/{id}
```

Fluxos de negócio usam ações explícitas, como `/aprovar`, `/cancelar`, `/baixar`, `/faturar`, `/efetivar` e `/simular`.

## Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/auth/login` | Autentica usuário |
| POST | `/api/v1/auth/refresh` | Renova access token |
| POST | `/api/v1/auth/logout` | Revoga refresh token |
| GET | `/api/v1/auth/me` | Retorna usuário autenticado |

## Cadastros

| Recurso | Endpoint base |
|---|---|
| Países | `/api/v1/cadastros/paises` |
| Estados | `/api/v1/cadastros/estados` |
| Cidades | `/api/v1/cadastros/cidades` |
| Endereços | `/api/v1/cadastros/enderecos` |
| Filiais | `/api/v1/cadastros/filiais` |
| Empresas | `/api/v1/empresas` |
| Categorias | `/api/v1/categorias` |
| Marcas | `/api/v1/marcas` |
| Unidades de medida | `/api/v1/unidades-medida` |
| Arquivos | `/api/v1/arquivos` |
| Upload de arquivos | `/api/v1/arquivos/upload` |

## Pessoas

| Recurso | Endpoint base |
|---|---|
| Pessoas | `/api/v1/pessoas` |
| Clientes | `/api/v1/clientes` |
| Fornecedores | `/api/v1/fornecedores` |
| Funcionários | `/api/v1/funcionarios` |
| Transportadoras | `/api/v1/transportadoras` |
| Contatos | `/api/v1/contatos-pessoa` |
| Documentos | `/api/v1/documentos-pessoa` |

## Produtos

| Recurso | Endpoint base |
|---|---|
| Produtos | `/api/v1/produtos` |
| Produto completo | `/api/v1/produtos/{id}/completo` |
| ProdutoFornecedor | `/api/v1/produtos-fornecedores` |
| ProdutoFornecedorPreço | `/api/v1/produtos-fornecedores-precos` |
| ProdutoFiscal | `/api/v1/produtos-fiscais` |
| ProdutoImagem | `/api/v1/produtos-imagens` |
| ProdutoCodigoBarras | `/api/v1/produtos-codigos-barras` |
| ProdutoPreco | `/api/v1/produtos-precos` |
| ProdutoUnidade | `/api/v1/produtos-unidades` |

## Estoque

| Recurso | Endpoint base |
|---|---|
| Depósitos | `/api/v1/depositos` |
| Localizações | `/api/v1/localizacoes-estoque` |
| Estoques | `/api/v1/estoques` |
| Estoque completo | `/api/v1/estoques/{id}/completo` |
| Saldo | `/api/v1/estoques/saldo` |
| Disponível | `/api/v1/estoques/disponivel` |
| Movimentações | `/api/v1/movimentacoes-estoque` |
| Reservas | `/api/v1/reservas-estoque` |

Reservas usam endpoints de estado:

```text
POST /api/v1/reservas-estoque/{id}/cancelar
POST /api/v1/reservas-estoque/{id}/baixar
```

## Compras

| Recurso | Endpoint base |
|---|---|
| Pedidos de compra | `/api/v1/pedidos-compra` |
| Pedido completo | `/api/v1/pedidos-compra/{id}/completo` |
| Itens de pedido | `/api/v1/pedidos-compra-itens` |
| Entradas de mercadoria | `/api/v1/entradas-mercadoria` |
| Entrada completa | `/api/v1/entradas-mercadoria/{id}/completo` |
| Itens de entrada | `/api/v1/entradas-mercadoria-itens` |

## Vendas

| Recurso | Endpoint base |
|---|---|
| Pedidos de venda | `/api/v1/pedidos-venda` |
| Pedido completo | `/api/v1/pedidos-venda/{id}/completo` |
| Itens de pedido | `/api/v1/pedidos-venda-itens` |
| Faturamentos | `/api/v1/vendas-faturamentos` |

Pedidos de venda usam endpoints de estado:

```text
POST /api/v1/pedidos-venda/{id}/aprovar
POST /api/v1/pedidos-venda/{id}/cancelar
POST /api/v1/vendas-faturamentos/faturar
```

O faturamento recebe `pedidoVendaId`, `condicaoPagamentoId`, `numero`, `descricaoFinanceira` e `observacao`.
Ao faturar, o backend baixa reservas, cria a ContaReceber e gera as ParcelasReceber pela condição informada.

## Pagamentos

| Recurso | Endpoint base |
|---|---|
| Formas de pagamento | `/api/v1/formas-pagamento` |
| Condições de pagamento | `/api/v1/condicoes-pagamento` |
| Simulação de parcelas | `/api/v1/condicoes-pagamento/{id}/simular` |

## Financeiro

| Recurso | Endpoint base |
|---|---|
| Contas a receber | `/api/v1/contas-receber` |
| Gerar parcelas | `/api/v1/contas-receber/{id}/gerar-parcelas` |
| Parcelas a receber | `/api/v1/parcelas-receber` |
| Baixar parcela | `/api/v1/parcelas-receber/{id}/baixar` |
| Cancelar parcela | `/api/v1/parcelas-receber/{id}/cancelar` |
| Recebimentos | `/api/v1/recebimentos` |
| Cancelar recebimento | `/api/v1/recebimentos/{id}/cancelar` |
| Contas a pagar | `/api/v1/contas-pagar` |
| Cancelar conta a pagar | `/api/v1/contas-pagar/{id}/cancelar` |
| Gerar parcelas a pagar | `/api/v1/contas-pagar/{id}/gerar-parcelas` |
| Parcelas a pagar | `/api/v1/parcelas-pagar` |
| Baixar parcela a pagar | `/api/v1/parcelas-pagar/{id}/baixar` |
| Cancelar parcela a pagar | `/api/v1/parcelas-pagar/{id}/cancelar` |
| Pagamentos financeiros | `/api/v1/pagamentos` |
| Cancelar pagamento | `/api/v1/pagamentos/{id}/cancelar` |
| Movimentos financeiros | `/api/v1/movimentos-financeiros` |
| Contas bancárias | `/api/v1/contas-bancarias` |
| Extratos bancários | `/api/v1/extratos-bancarios` |

As listagens de contas a receber, contas a pagar e movimentos financeiros aceitam
os filtros opcionais `centroCustoId` e `planoContaId`.

### Movimentos financeiros

| Método | Endpoint | Finalidade |
|---|---|---|
| POST | `/api/v1/movimentos-financeiros` | Criar ajuste, transferência ou estorno manual |
| GET | `/api/v1/movimentos-financeiros` | Listar movimentos com paginação e filtros |
| GET | `/api/v1/movimentos-financeiros/{id}` | Consultar um movimento |
| POST | `/api/v1/movimentos-financeiros/{id}/cancelar` | Cancelar sem apagar o histórico |

Movimentos com origem `RECEBIMENTO` ou `PAGAMENTO` são gerados exclusivamente pelos respectivos services.

### Contas bancárias

| Método | Endpoint | Finalidade |
|---|---|---|
| POST | `/api/v1/contas-bancarias` | Criar conta bancária ou caixa interno |
| GET | `/api/v1/contas-bancarias` | Listar com paginação e filtros |
| GET | `/api/v1/contas-bancarias/{id}` | Consultar uma conta |
| PUT | `/api/v1/contas-bancarias/{id}` | Atualizar dados cadastrais e saldo inicial |
| DELETE | `/api/v1/contas-bancarias/{id}` | Excluir logicamente |

Os filtros disponíveis são `empresaId`, `filialId`, `bancoCodigo`, `bancoNome`,
`tipoConta`, `principal` e `ativo`. As operações exigem `CONTA_BANCARIA:*`
ou a permissão granular correspondente.

### Extratos bancários

| Método | Endpoint | Finalidade |
|---|---|---|
| POST | `/api/v1/extratos-bancarios` | Importar metadados de extrato bancário |
| GET | `/api/v1/extratos-bancarios` | Listar com paginação e filtros |
| GET | `/api/v1/extratos-bancarios/{id}` | Consultar uma importação |
| POST | `/api/v1/extratos-bancarios/{id}/cancelar` | Cancelar mantendo histórico |

Os filtros disponíveis são `contaBancariaId`, `origem`, `status`, `dataInicioDe`,
`dataInicioAte`, `dataFimDe`, `dataFimAte` e `ativo`. O hash do arquivo deve ser
SHA-256 quando informado e não pode duplicar outro extrato ativo.
As operações exigem `EXTRATO_BANCARIO:*`, `EXTRATO_BANCARIO:IMPORTAR`,
`EXTRATO_BANCARIO:VISUALIZAR` ou `EXTRATO_BANCARIO:CANCELAR`.

### Relatórios financeiros

| Método | Endpoint | Finalidade |
|---|---|---|
| GET | `/api/v1/financeiro/relatorios/fluxo-caixa` | Consolidar entradas, saídas e saldo por período |
| GET | `/api/v1/financeiro/relatorios/dashboard` | Consultar caixa, títulos abertos e vencidos |
| GET | `/api/v1/financeiro/relatorios/dre` | Gerar DRE financeira por período agrupada por plano de conta |

As rotas recebem `dataInicio`, `dataFim` no formato `YYYY-MM-DD` e filtros opcionais.
`fluxo-caixa` e `dashboard` aceitam `centroCustoId` e `planoContaId`.
`dre` aceita `empresaId`, `filialId`, `centroCustoId` e `planoContaId`.
Elas exigem
`RELATORIO_FINANCEIRO:VISUALIZAR` ou `RELATORIO_FINANCEIRO:*`.

### Centros de custo

| Método | Endpoint | Finalidade |
|---|---|---|
| POST | `/api/v1/centros-custo` | Criar centro de custo |
| GET | `/api/v1/centros-custo` | Listar com paginação e filtros |
| GET | `/api/v1/centros-custo/{id}` | Consultar centro de custo |
| PUT | `/api/v1/centros-custo/{id}` | Atualizar dados e hierarquia |
| DELETE | `/api/v1/centros-custo/{id}` | Excluir logicamente |

Os filtros disponíveis são empresa, pai, código, nome, tipo e ativo.

### Plano de contas

| Método | Endpoint | Finalidade |
|---|---|---|
| POST | `/api/v1/planos-contas` | Criar conta sintética ou analítica |
| GET | `/api/v1/planos-contas` | Listar com paginação e filtros |
| GET | `/api/v1/planos-contas/{id}` | Consultar conta |
| PUT | `/api/v1/planos-contas/{id}` | Atualizar dados e hierarquia |
| DELETE | `/api/v1/planos-contas/{id}` | Excluir logicamente |

Os filtros disponíveis são `empresaId`, `planoContaPaiId`, `codigo`, `nome`,
`tipo`, `natureza` e `ativo`. As operações exigem permissões `PLANO_CONTA:*`
ou a permissão granular correspondente.

Contas a receber aceitam apenas planos `ANALITICA` de natureza `RECEITA`.
Contas a pagar aceitam apenas planos `ANALITICA` de natureza `DESPESA`.
Movimentos financeiros propagam automaticamente o plano da conta quando nascem de
Recebimento ou Pagamento.

## Convenções

CRUD padrão:

```text
POST   /recurso
GET    /recurso?page=0&size=20&sort=campo,asc
GET    /recurso/{id}
PUT    /recurso/{id}
DELETE /recurso/{id}
```

Operações de fluxo usam verbos de negócio, como `/aprovar`, `/cancelar`, `/baixar`, `/faturar` e `/efetivar`.
