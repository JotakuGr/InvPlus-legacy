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

- ExtratoBancario concluido em `/api/v1/extratos-bancarios`, com permissões `EXTRATO_BANCARIO:*`, migrations V112/V113, filtros por conta/origem/status/periodo, hash SHA-256 unico e cancelamento sem exclusao historica.
