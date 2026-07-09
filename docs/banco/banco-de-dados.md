# Banco de Dados

O invPlus usa PostgreSQL com schema versionado por Flyway.

## Convenções

- Nomes de tabelas e colunas em `snake_case`.
- Identificadores em UUID.
- Soft delete por `ativo` e `deleted_at`.
- Auditoria por `criado_em`, `criado_por`, `atualizado_em` e `atualizado_por`.
- Concorrência otimista por `version`.
- Índices parciais para unicidade apenas entre registros ativos.
- Consultas padrão não devem retornar registros inativos ou deletados.

## Campos comuns

Entidades de negócio usam a base auditável:

```text
id
ativo
deleted_at
version
criado_em
criado_por
atualizado_em
atualizado_por
```

## Índices e unicidade

O projeto prioriza índices para:

- Registros ativos.
- Chaves de relacionamento.
- Campos de busca frequente.
- Regras de unicidade por empresa, por entidade ou por vínculo.

Exemplos:

- CNPJ único apenas entre empresas ativas.
- CPF/CNPJ único apenas entre pessoas ativas.
- Produto único por código ativo.
- Estoque único por produto, empresa, depósito e localização.
- Apenas uma imagem principal ativa por produto.
- Apenas uma unidade padrão ativa por produto.

## Migrations e seeds

Migrations de schema devem focar em:

- Tabelas.
- Colunas.
- Índices.
- Constraints.
- Comentários.
- Ajustes evolutivos.

Migrations de permissões podem inserir permissões e vincular ao perfil `ADMIN`. Inserts de dados de domínio devem ser evitados quando não forem necessários para o funcionamento da aplicação.

## Extensões

A migration inicial prepara extensões úteis:

- `pgcrypto`, para UUID e recursos criptográficos.
- `citext`, para comparações case-insensitive onde aplicável.

## Módulos versionados

Atualmente há migrations cobrindo:

- Segurança.
- Cadastros.
- Pessoas.
- Produtos.
- Estoque.
- Compras.
- Vendas.
- Pagamentos.
- Financeiro.

Veja a lista completa em `docs/banco/migrations.md`.
