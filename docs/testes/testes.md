# Testes Automatizados

Resultado atual:

```text
BUILD SUCCESS
Tests run: 692, Failures: 0, Errors: 0, Skipped: 0
```

Validacao final executada em 13/07/2026: `147` suites. O bloco adicional de concorrencia
executou `2` testes com sucesso.

## Cobertura

O projeto possui testes para:

- Services.
- Controllers.
- Segurança.
- Integração por módulo.
- Business flows entre módulos.
- Concorrência e optimistic lock.
- Performance local simples.
- Idempotência de migrations.

## Principais Grupos

| Grupo | Objetivo |
|---|---|
| `modules/**/service` | Regras de negócio e validações |
| `integration/**` | Controllers, autenticação, permissões e contrato HTTP |
| `businessflow/**` | Fluxos ponta a ponta entre módulos |
| `concurrency/**` | Concorrência e conflito otimista |
| `performance/**` | Carga local simples |
| `security/**` | JWT, autenticação e seed de segurança |
| `shared/**` | Infraestrutura compartilhada e migrations |

## Fluxos Cobertos

- Cadastros base.
- Pessoa e seus papéis.
- Produto completo.
- Produto fornecedor, preço, imagem, código de barras e unidade.
- Estoque com saldo, movimentação e reserva.
- Compras integradas ao estoque.
- Vendas com item, reserva automática e faturamento.
- Pagamentos com simulação de parcelas.
- Financeiro com contas, parcelas, recebimentos, pagamentos, movimentos, centros de custo, plano de contas, DRE, contas bancárias, extratos, importação OFX, lançamentos e conciliação bancária concorrente.

## Execução

```powershell
mvn test
```

O `mvnw.cmd` deve ser mantido funcional para permitir execução sem Maven global instalado.
# Isolamento empresarial

Os testes de seguranca cobrem o contrato de `EmpresaContext`, validacao de empresa/filial e tokens
contextualizados. Os testes bancarios focados validam consultas por empresa, importacao OFX e
agregacoes da central e dos relatorios.

O bloco de Estoque cobre contratos sem `empresaId`, isolamento por empresa em controllers
e services, optimistic lock e carga concorrente. Testes multithread usam `TestEmpresaContext`
por thread para nao compartilhar contexto autenticado.

O business flow de Compras cobre pedido, entrada, estoque, movimento, optimistic lock,
entradas simultaneas, isolamento A/B, token sem empresa e acesso sem permissao.

O bloco de Vendas cobre pedido e item sem `empresaId`, reserva automatica, aprovacao
concorrente, faturamento, baixa fisica, ContaReceber e isolamento A/B. O teste
`VendaContextoEmpresarialBusinessFlowIntegrationTest` troca o contexto e valida 404/listas vazias.
