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

Todos os endpoints de estoque usam a empresa do claim `empresa_id` do access token.
Requests e filtros de deposito, localizacao, saldo, movimentacao e reserva nao aceitam
`empresaId`. Consultas por UUID de outra empresa retornam `404`.

## Compras

| Recurso | Endpoint base |
|---|---|
| Pedidos de compra | `/api/v1/pedidos-compra` |
| Pedido completo | `/api/v1/pedidos-compra/{id}/completo` |
| Itens de pedido | `/api/v1/pedidos-compra-itens` |
| Entradas de mercadoria | `/api/v1/entradas-mercadoria` |
| Entrada completa | `/api/v1/entradas-mercadoria/{id}/completo` |
| Itens de entrada | `/api/v1/entradas-mercadoria-itens` |

Todos os endpoints de Compras usam a empresa do claim `empresa_id` do access token.
Pedidos, entradas e seus itens nao aceitam `empresaId`; consultas por UUID de outra
empresa retornam `404`.

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

Todos os endpoints de Vendas usam a empresa do claim `empresa_id` do access token.
Pedidos, itens, faturamentos e filtros publicos nao aceitam `empresaId`. Um UUID que
pertenca a outra empresa e tratado como recurso inexistente (`404`).

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
| Lançamentos de extrato bancário | `/api/v1/extratos-bancarios-lancamentos` |
| Conciliações bancárias | `/api/v1/conciliacoes-bancarias` |
| Importar extrato OFX | `/api/v1/extratos-bancarios/importar/ofx` |
| Histórico de importações | `/api/v1/importacoes-extratos` |

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

Os filtros disponíveis são `filialId`, `bancoCodigo`, `bancoNome`,
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

### Lançamentos de extrato bancário

| Método | Endpoint | Finalidade |
|---|---|---|
| POST | `/api/v1/extratos-bancarios-lancamentos` | Criar lançamento de crédito ou débito |
| GET | `/api/v1/extratos-bancarios-lancamentos` | Listar com paginação e filtros |
| GET | `/api/v1/extratos-bancarios-lancamentos/{id}` | Consultar um lançamento |
| PUT | `/api/v1/extratos-bancarios-lancamentos/{id}` | Atualizar lançamento não conciliado |
| DELETE | `/api/v1/extratos-bancarios-lancamentos/{id}` | Excluir logicamente lançamento não conciliado |

Os filtros disponíveis são `extratoBancarioId`, `contaBancariaId`, `tipo`,
`categoriaBancaria`, `conciliado`, `dataLancamentoDe`, `dataLancamentoAte` e
`ativo`. O identificador bancário é opcional, mas não pode duplicar dentro do
mesmo extrato ativo. Lançamentos conciliados ficam bloqueados para edição e
exclusão.

As operações exigem `EXTRATO_BANCARIO_LANCAMENTO:*` ou permissão granular de
criar, visualizar, editar ou excluir.

### Conciliações bancárias

| Método | Endpoint | Finalidade |
|---|---|---|
| POST | `/api/v1/conciliacoes-bancarias` | Conciliar lançamento bancário e movimento financeiro |
| GET | `/api/v1/conciliacoes-bancarias` | Listar o histórico com paginação e filtros |
| GET | `/api/v1/conciliacoes-bancarias/{id}` | Consultar uma conciliação |
| POST | `/api/v1/conciliacoes-bancarias/{id}/cancelar` | Cancelar e liberar o lançamento bancário |

Os filtros são `extratoBancarioLancamentoId`, `movimentoFinanceiroId`,
`contaBancariaId`, `tipoConciliacao`, `status`, `conciliadoEmDe` e
`conciliadoEmAte`. A primeira versão aceita apenas valores exatos e exige
`CREDITO` com `ENTRADA` ou `DEBITO` com `SAIDA`. Não existem rotas de edição ou
exclusão; cancelamentos preservam a trilha financeira.

As operações exigem `CONCILIACAO_BANCARIA:*` ou permissão granular de criar,
visualizar ou cancelar.

### Importação de extratos OFX

| Método | Endpoint | Finalidade |
|---|---|---|
| POST | `/api/v1/extratos-bancarios/importar/ofx` | Enviar OFX por `multipart/form-data` |
| GET | `/api/v1/importacoes-extratos` | Listar processos de importação |
| GET | `/api/v1/importacoes-extratos/{id}` | Consultar relatório da importação |

O multipart recebe `arquivo`, `contaBancariaId` opcional e `observacao` opcional.
Sem conta informada, banco, agência e número do OFX são normalizados e usados para
localizar uma única conta ativa. O arquivo é limitado a 10 MB, processado somente
em memória e identificado por SHA-256. Reenvio do mesmo conteúdo retorna `409`.

Os filtros de acompanhamento são `contaBancariaId`, `formato`, `status`,
`arquivoHash`, `iniciadaEmDe` e `iniciadaEmAte`. As operações exigem
`IMPORTACAO_EXTRATO:*`, `IMPORTACAO_EXTRATO:IMPORTAR` ou
`IMPORTACAO_EXTRATO:VISUALIZAR`.

### Relatórios financeiros

| Método | Endpoint | Finalidade |
|---|---|---|
| GET | `/api/v1/financeiro/relatorios/fluxo-caixa` | Consolidar entradas, saídas e saldo por período |
| GET | `/api/v1/financeiro/relatorios/dashboard` | Consultar caixa, títulos abertos e vencidos |
| GET | `/api/v1/financeiro/relatorios/dre` | Gerar DRE financeira por período agrupada por plano de conta |

As rotas recebem `dataInicio`, `dataFim` no formato `YYYY-MM-DD` e filtros opcionais.
`fluxo-caixa` e `dashboard` aceitam `centroCustoId` e `planoContaId`.
`dre` aceita `filialId`, `centroCustoId` e `planoContaId`.
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

Os filtros disponíveis são pai, código, nome, tipo e ativo.

### Plano de contas

| Método | Endpoint | Finalidade |
|---|---|---|
| POST | `/api/v1/planos-contas` | Criar conta sintética ou analítica |
| GET | `/api/v1/planos-contas` | Listar com paginação e filtros |
| GET | `/api/v1/planos-contas/{id}` | Consultar conta |
| PUT | `/api/v1/planos-contas/{id}` | Atualizar dados e hierarquia |
| DELETE | `/api/v1/planos-contas/{id}` | Excluir logicamente |

Os filtros disponíveis são `planoContaPaiId`, `codigo`, `nome`,
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

## Sugestoes de conciliacao bancaria

- `GET /api/v1/conciliacoes-bancarias/sugestoes?lancamentoBancarioId={id}&limite=10`
- Permissao: `CONCILIACAO_BANCARIA:SUGERIR` ou `CONCILIACAO_BANCARIA:*`.
- A consulta nao persiste sugestoes nem reserva movimentos.
- `GET /api/v1/conciliacoes-bancarias/painel` - indicadores e extratos recentes.
- `GET /api/v1/conciliacoes-bancarias/extratos-pendentes` - extratos paginados.
- `GET /api/v1/conciliacoes-bancarias/pendencias` - lancamentos pendentes com melhor sugestao.
- `POST /api/v1/conciliacoes-bancarias/confirmar-lote` - confirmacao parcial de ate 100 pares.

## Contexto empresarial

- `POST /api/v1/auth/contexto/selecionar` - troca selection token por tokens contextualizados.
- `POST /api/v1/auth/contexto/trocar` - emite tokens para outra empresa/filial permitida.
- `GET /api/v1/auth/contexto` - retorna o contexto atual.
- `GET /api/v1/auth/contexto/empresas` - lista vinculos empresariais ativos.

Operações de fluxo usam verbos de negócio, como `/aprovar`, `/cancelar`, `/baixar`, `/faturar` e `/efetivar`.
# Contexto empresarial nos endpoints financeiros

Contas bancarias, extratos, lancamentos, importacoes OFX, conciliacoes, central de conciliacao,
movimentos e relatorios financeiros usam o claim `empresa_id` do access token. O frontend nao
deve escolher nem enviar a empresa da operacao. O escopo empresarial e aplicado obrigatoriamente
pelos services, repositories, specifications e consultas agregadas.
