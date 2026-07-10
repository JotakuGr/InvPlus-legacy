# Testes Automatizados

Resultado atual:

```text
BUILD SUCCESS
Tests run: 626, Failures: 0, Errors: 0
```

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
- Financeiro com contas, parcelas, recebimentos, pagamentos, movimentos, centros de custo, plano de contas, DRE, contas bancárias, extratos e lançamentos bancários.

## Execução

```powershell
mvn test
```

O `mvnw.cmd` deve ser mantido funcional para permitir execução sem Maven global instalado.
