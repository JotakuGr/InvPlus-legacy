# Convencoes do Projeto - invPlus ERP

## Branches

```text
main
develop
feature/nome-da-funcionalidade
fix/nome-da-correcao
docs/nome-da-documentacao
refactor/nome-da-refatoracao
```

Exemplos:

```text
feature/cadastro-produtos
feature/autenticacao-jwt
fix/correcao-login
docs/atualiza-readme
```

## Commits

O projeto segue Conventional Commits.

```text
<tipo>(<escopo>): <descricao curta>
```

Tipos:

| Tipo | Uso |
|---|---|
| `feat` | Nova funcionalidade |
| `fix` | Correcao de erro |
| `docs` | Documentacao |
| `style` | Formatacao sem mudanca funcional |
| `refactor` | Refatoracao |
| `test` | Testes |
| `chore` | Configuracao ou manutencao |
| `perf` | Performance |

Exemplos:

```text
feat(seguranca): implementa login com jwt
fix(seguranca): corrige validacao de token expirado
docs(arquitetura): atualiza estrutura do projeto
test(seguranca): adiciona teste de login
chore(docker): parametriza credenciais do compose
```

## Java

| Item | Convencao |
|---|---|
| Classes | PascalCase |
| Metodos | camelCase |
| Variaveis | camelCase |
| Constantes | UPPER_CASE |
| Pacotes | letras minusculas |

Exemplos:

```text
ProdutoService
criarProduto()
produtoRepository
VALOR_MAXIMO_DESCONTO
br.com.invplus.modules.produtos
```

## Entidades JPA

- Usar `UUID` como chave primaria.
- Usar `BaseEntity` quando a tabela possuir auditoria completa.
- Usar `SoftDeleteEntity` quando a tabela possuir `ativo` e `deleted_at`.
- Implementar igualdade baseada na identidade da entidade.
- Evitar `@Data` do Lombok em entidades JPA.
- Preferir `@Getter` e `@Setter`.
- Usar construtor protegido sem argumentos para JPA.

## Banco de Dados

| Item | Convencao |
|---|---|
| Tabelas | snake_case |
| Colunas | snake_case |
| Chave primaria | `id` |
| Chave estrangeira | `<entidade>_id` |
| Datas | `TIMESTAMPTZ` |
| Email case-insensitive | `CITEXT` |

Exemplos:

```text
usuario
pedido_venda
cliente_id
criado_em
atualizado_em
deleted_at
```

## Migrations Flyway

```text
V<numero>__descricao.sql
```

Exemplos:

```text
V1__init_extensions.sql
V2__schema_seguranca.sql
V3__seed_seguranca_inicial.sql
```

## Configuracao e Segredos

- Nao versionar senhas reais.
- Nao versionar segredos JWT reais.
- Usar variaveis de ambiente no profile principal.
- Usar `.env.example` apenas como modelo.
- Profiles de teste podem conter valores locais controlados.

## REST

Padrao de endpoints:

```text
GET    /api/v1/recursos
GET    /api/v1/recursos/{id}
POST   /api/v1/recursos
PUT    /api/v1/recursos/{id}
DELETE /api/v1/recursos/{id}
```

Endpoint de login atual:

```text
POST /api/v1/auth/login
```

