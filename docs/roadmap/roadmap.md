# Roadmap

## Concluído

- ✅ Segurança
- ✅ Cadastros
- ✅ Pessoas
- ✅ Produtos
- ✅ Estoque
- ✅ Compras
- ✅ Vendas
- ✅ Pagamentos
- ✅ Contas a receber
- ✅ Recebimentos
- ✅ Contas a pagar
- ✅ Parcelas a pagar
- ✅ Pagamentos financeiros
- ✅ Movimentos financeiros integrados a recebimentos e pagamentos
- ✅ Resumo de fluxo de caixa e dashboard financeiro
- ✅ Centros de custo hierárquicos por empresa
- ✅ Centros de custo integrados a contas, movimentos e relatórios financeiros
- ✅ Plano de contas hierárquico com contas sintéticas e analíticas
- ✅ Plano de contas integrado a contas, movimentos e relatórios financeiros
- ✅ DRE financeira agrupada por plano de conta
- ✅ Contas bancárias por empresa e filial
- ✅ Extratos bancários com importação, filtros, hash SHA-256 e cancelamento histórico
- ✅ Lançamentos de extrato bancário com crédito, débito, categorias e bloqueio após conciliação
- ✅ Conciliação bancária exata com cancelamento histórico e proteção concorrente
- ✅ Importação OFX com parser intermediário, hash SHA-256 e relatório de processamento

## Em Desenvolvimento

- 🚧 Financeiro completo
- 🚧 Frontend Angular
- 🚧 Organização contínua da documentação

## Futuro

- ⬜ Fiscal
- ⬜ CRM
- ⬜ Dashboard
- ⬜ BI
- ⬜ Mobile
- ⬜ Marketplace
- ⬜ Integração bancária
- ⬜ Métricas e observabilidade
- ⬜ CI/CD

## Frontend

Quando o Angular estiver pronto, adicionar screenshots em:

```text
docs/images/
```

Sugestões:

- Dashboard.
- Produtos.
- Pedidos.
- PDV.
- Financeiro.

## Próximos Marcos Técnicos

- Consolidar Contas a pagar.
- Fechar Financeiro v1.
- Iniciar Fiscal.
- Preparar dashboards.
- Evoluir observabilidade com logs centralizados e tracing.

## Atualizacao Recente

- Sugestoes explicaveis de conciliacao concluidas com migrations V120/V121 e filtro no banco.
- Central operacional e confirmacao parcial em lote concluidas com migrations V122/V123.
- Contexto empresarial no JWT iniciado com migrations V124/V125/V126 e migracao bancaria compativel.

- ImportacaoExtratoBancario concluida com upload em `/api/v1/extratos-bancarios/importar/ofx`, consultas em `/api/v1/importacoes-extratos`, permissões `IMPORTACAO_EXTRATO:*`, migrations V118/V119, parser OFX extensível, hash idempotente e integração com conciliação.
- [x] Contexto empresarial no JWT e isolamento do modulo bancario
- [x] Integrar login e selecao de contexto no frontend
- [x] Remover parametros `empresaId` legados dos contratos operacionais
- [x] Remover `empresaId` dos contratos operacionais de Estoque
- [x] Isolar Deposito, LocalizacaoEstoque, Estoque, MovimentacaoEstoque e ReservaEstoque
- [x] Migrar Compras para `EmpresaContext`
- [x] Isolar PedidoCompra, EntradaMercadoria e respectivos itens
- [x] Migrar Vendas para `EmpresaContext`
- [x] Isolar PedidoVenda, itens, faturamento, estoque e ContaReceber por empresa
