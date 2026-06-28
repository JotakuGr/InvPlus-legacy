
---

## 4. `convencoes.md`

```markdown
# Convenções do Projeto — invPlus ERP

## Nomenclatura de Branches

```text
main
develop
feature/nome-da-funcionalidade
fix/nome-da-correcao
docs/nome-da-documentacao
refactor/nome-da-refatoracao

<!-- Exemplos -->
feature/cadastro-produtos
feature/autenticacao-jwt
fix/correcao-login
docs/atualiza-readme

Padrão de Commits
O projeto seguirá Conventional Commits.

feat: nova funcionalidade
fix: correção de erro
docs: documentação
style: formatação
refactor: refatoração
test: testes
chore: configuração ou manutenção

<!-- Exemplos -->

feat: adiciona cadastro de produtos
fix: corrige validação de email
docs: adiciona documentação da arquitetura
chore: organiza estrutura inicial do projeto

<!-- Convenções Java -->

Classes: PascalCase
Métodos: camelCase
Variáveis: camelCase
Constantes: UPPER_CASE
Pacotes: letras minúsculas

<!-- Exemplos -->

ProdutoService
criarProduto()
produtoRepository
VALOR_MAXIMO_DESCONTO
br.com.invplus.modules.produtos

<!-- Convenções Banco de Dados -->

Tabelas em snake_case
Colunas em snake_case
Chaves primárias chamadas id
Chaves estrangeiras terminando com _id

<!-- Exemplos -->

produto
pedido_venda
cliente_id
criado_em
atualizado_em

