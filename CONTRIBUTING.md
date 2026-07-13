# Contributing to invPlus ERP

Obrigado pelo interesse em contribuir com o **invPlus ERP**!

Este documento descreve os padrões adotados pelo projeto para garantir qualidade, consistência e facilidade de manutenção.

---

# Índice

- Objetivo
- Tecnologias
- Configuração do Ambiente
- Executando o Projeto
- Executando os Testes
- Convenções de Código
- Estrutura dos Módulos
- Banco de Dados
- Segurança
- Testes
- Documentação
- Git Flow
- Commits
- Pull Requests
- Checklist antes do Merge

---

# Objetivo

O invPlus é um ERP modular desenvolvido em Java utilizando Spring Boot.

O objetivo do projeto é fornecer uma base sólida, escalável e de fácil manutenção para gestão empresarial.

As contribuições devem seguir os padrões definidos neste documento.

---

# Tecnologias

Backend

- Java 25
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- Flyway
- PostgreSQL
- Redis
- RabbitMQ
- Maven

Qualidade

- JUnit 5
- Mockito
- MockMvc

Infraestrutura

- Docker
- Docker Compose
- GitHub Actions
- CodeQL

---

# Configuração do Ambiente

## Requisitos

- Java 25
- Docker Desktop
- Git
- Maven Wrapper

Clone o projeto

```bash
git clone https://github.com/JotakuGr/invPlus.git
```

Entre na pasta

```bash
cd invPlus
```

---

# Executando o Backend

Na pasta backend:

```bash
./mvnw spring-boot:run
```

ou

```bash
mvn spring-boot:run
```

---

# Executando os Testes

Executar toda a suíte

```bash
./mvnw test
```

Executar apenas um teste

```bash
./mvnw -Dtest=NomeDoTeste test
```

Todos os Pull Requests devem manter a suíte passando.

---

# Convenções de Código

## Nome de classes

Use PascalCase.

Exemplo:

```
ProdutoService
ContaReceber
MovimentoFinanceiro
```

---

## Métodos

Use camelCase.

```
buscarPorId()

cancelar()

gerarParcelas()

conciliar()
```

---

## Pacotes

Sempre em minúsculo.

```
modules

financeiro

estoque

vendas
```

---

## DTOs

Sempre separados entre:

```
request

response
```

---

## Controllers

Responsáveis apenas pela camada HTTP.

Nunca colocar regra de negócio.

---

## Services

Responsáveis pelas regras de negócio da entidade.

---

## Validators

Toda validação complexa deve ficar em Validator.

Evite colocar regras diretamente no Controller.

---

## Specifications

Filtros de pesquisa devem ficar em Specification.

---

## Domain Services

Quando uma regra envolver mais de uma entidade.

Exemplo:

```
EstoqueDomainService

FinanceiroDomainService
```

---

## Aggregate Services

Responsáveis por montar respostas compostas para leitura.

Nunca devem alterar dados.

---

## Facades

Responsáveis pela comunicação entre módulos.

Exemplo:

```
VendaFacade

CompraFacade

EstoqueFacade
```

---

# Banco de Dados

Utilizamos Flyway.

Regras:

- Nunca alterar migrations antigas.
- Sempre criar uma nova migration.
- Nomear seguindo o padrão:

```
V001__descricao.sql
```

Exemplo:

```
V116__schema_conciliacao_bancaria.sql
```

---

# Segurança

Todas as novas funcionalidades devem possuir permissões.

Exemplo:

```
MODULO:*

MODULO:CRIAR

MODULO:VISUALIZAR

MODULO:EDITAR

MODULO:EXCLUIR
```

Quando necessário:

```
CANCELAR

BAIXAR

FATURAR

IMPORTAR

CONFERIR
```

---

# Testes

Toda funcionalidade nova deve possuir testes.

Obrigatórios:

- ServiceTest
- ControllerTests
- BusinessFlowIntegrationTest

Quando aplicável:

- ConcurrencyTest
- SecurityTest
- PerformanceTest

Nenhum Pull Request deve reduzir a cobertura existente.

---

# Documentação

Sempre atualizar quando necessário:

```
README.md

docs/api/endpoints.md

docs/banco/migrations.md

docs/regras-negocio/

docs/testes/

docs/roadmap/

docs/arquitetura/
```

---

# Git Flow

Fluxo adotado:

```
main

↓

feature/*
```

Exemplos:

```
feature/produto-preco

feature/financeiro

feature/conciliacao-bancaria
```

---

# Commits

Utilize mensagens claras.

Preferencialmente seguindo Conventional Commits.

Exemplos:

```
feat(financeiro): adiciona conciliação bancária

fix(estoque): corrige cálculo de saldo disponível

docs(readme): atualiza documentação

test(vendas): adiciona testes de faturamento

refactor(produtos): simplifica validator
```

---

# Pull Requests

Antes de abrir uma Pull Request:

- Atualize sua branch
- Resolva conflitos
- Execute os testes
- Atualize a documentação
- Preencha o template de PR

---

# Checklist antes do Merge

## Código

- [ ] Código segue o padrão do projeto
- [ ] Não possui código comentado
- [ ] Não possui TODO pendente
- [ ] Sem warnings relevantes

## Banco

- [ ] Migration criada
- [ ] Flyway validado

## Testes

- [ ] Testes unitários
- [ ] Testes REST
- [ ] Testes de integração
- [ ] Todos passando

## Segurança

- [ ] Permissões adicionadas
- [ ] Endpoints protegidos

## Documentação

- [ ] README atualizado
- [ ] Endpoints documentados
- [ ] Roadmap atualizado
- [ ] Regras de negócio atualizadas

---

# Padrão Arquitetural

Cada módulo deve seguir a estrutura abaixo:

```
entity

repository

dto
├── request
└── response

mapper

validator

specification

service

controller
```

Quando necessário:

```
domain

aggregate

facade

events
```

---

# Qualidade

Toda contribuição deve priorizar:

- Legibilidade
- Baixo acoplamento
- Alta coesão
- Código limpo
- Reutilização
- Cobertura de testes
- Documentação atualizada

---

Obrigado por contribuir com o invPlus ERP!