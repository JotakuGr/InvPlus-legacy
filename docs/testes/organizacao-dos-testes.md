# Organização dos Testes

A estrutura de testes fica em:

```text
backend/src/test/java/br/com/invplus/
├── security/
├── integration/
│   ├── cadastros/
│   ├── pessoas/
│   ├── produtos/
│   ├── estoque/
│   ├── compras/
│   ├── vendas/
│   ├── pagamentos/
│   └── financeiro/
├── businessflow/
├── concurrency/
├── performance/
├── modules/
└── shared/
```

## Regras

- `businessflow/`: fluxos ponta a ponta entre vários módulos.
- `concurrency/`: optimistic lock e concorrência.
- `integration/<modulo>/`: testes de controller e integração focados em um módulo.
- `security/`: autenticação, JWT e permissões.
- `performance/`: carga local simples.
- `modules/<modulo>/service`: testes de service e regra de domínio.
- `shared/`: infraestrutura compartilhada, domínio comum e migrations.

## Convenção de Nomes

| Sufixo | Uso |
|---|---|
| `ServiceTest` | Regras de negócio |
| `ControllerTests` | Contrato HTTP |
| `BusinessFlowIntegrationTest` | Fluxo de negócio entre módulos |
| `ConcurrencyTest` | Concorrência |
| `OptimisticLockTest` | Versionamento otimista |
| `LoadTest` | Teste local de carga |

## Política

Novas entidades devem receber pelo menos:

- Teste de service.
- Teste de controller.
- Teste de fluxo quando a entidade conversar com mais de um módulo.
- Teste de segurança para `401` e `403` quando houver endpoint protegido.
