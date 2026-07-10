<h1 align="center">
  invPlus ERP
</h1>

<p align="center">
  ERP moderno, modular e escalável para gestão empresarial.
</p>

<p align="center">
  Desenvolvido com Java, Spring Boot, Angular, PostgreSQL e Docker.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/status-em%20desenvolvimento-yellow?style=flat-square"/>
  <img src="https://img.shields.io/badge/versão-0.1.0--alpha-orange?style=flat-square"/>
  <img src="https://img.shields.io/badge/licença-MIT-blue?style=flat-square"/>
</p>

> **Aviso de maturidade:** o invPlus está em desenvolvimento ativo. O backend já possui uma base avançada, mas o produto ainda não deve ser usado em produção.

## Sobre o projeto

O **invPlus ERP** é um sistema de gestão empresarial construído como monólito modular, com separação por domínios de negócio e uma API REST preparada para evoluir com segurança.

A proposta é cobrir o fluxo operacional de um ERP real: cadastros, pessoas, produtos, estoque, compras, vendas, faturamento, pagamentos e financeiro.

## Diferenciais

- Arquitetura modular
- DDD
- Clean Architecture
- SOLID
- Soft delete
- Auditoria
- Optimistic lock
- JWT
- Flyway
- Docker
- Testes automatizados
- Redis preparado
- RabbitMQ preparado
- Cloudflare R2 preparado

## Demonstração

Os GIFs da aplicação serão adicionados quando o frontend Angular estiver pronto.

Arquivos previstos:

```text
docs/images/login.gif
docs/images/dashboard.gif
docs/images/produtos.gif
docs/images/vendas.gif
```

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Backend | Java 25, Spring Boot |
| Frontend | Angular |
| Banco de dados | PostgreSQL |
| Migrações | Flyway |
| Segurança | Spring Security, JWT |
| Persistência | Spring Data JPA, Hibernate |
| Build | Maven |
| Infraestrutura | Docker, Docker Compose |
| Storage | Local, Cloudflare R2 preparado |
| Cache e mensageria | Redis e RabbitMQ preparados |

## Status do projeto

| Área | Status |
|---|---|
| Backend | ✅ Em desenvolvimento avançado |
| Frontend | 🚧 Em desenvolvimento |
| API REST | ✅ Implementada |
| Docker | ✅ Configurado |
| PostgreSQL | ✅ Configurado |
| Flyway | ✅ Configurado |
| Testes | ✅ 626 testes |
| Documentação | 🚧 Em organização |
| CI/CD | ⬜ Futuro |

## Métricas

| Métrica | Valor |
|---|---|
| Migrations | 115 |
| Testes automatizados | 626 |
| Módulos backend | 12+ |
| Arquitetura | Monólito modular |
| API | REST JSON |
| Autenticação | JWT |

## Arquitetura em Números

| Métrica | Valor |
|---|---|
| Migrations Flyway | 115 |
| Testes automatizados | 626 |
| Módulos funcionais | 10+ |
| Endpoints REST | 150+ |
| Entidades de domínio | 60+ |
| Controllers | 40+ |
| Services | 50+ |
| Specifications | 30+ |

## Arquitetura Resumida

```text
Angular
   ↓ REST/JSON
Spring Boot
   ↓ JPA/Flyway
PostgreSQL

Infraestrutura preparada:
Redis, RabbitMQ, Docker, Cloudflare R2
```

Fluxo padrão de implementação por entidade:

```text
Migration → Entity → Repository → DTOs → Mapper → Validator → Service → Controller → Testes → Documentação
```

## Módulos

| Módulo | Status |
|---|---|
| Segurança | ✅ Implementado |
| Cadastros | ✅ Implementado |
| Pessoas | ✅ Implementado |
| Produtos | ✅ Implementado |
| Estoque | ✅ Implementado |
| Compras | ✅ Implementado |
| Vendas | ✅ Implementado |
| Pagamentos | ✅ Implementado |
| Contas a receber | ✅ Implementado |
| Contas a pagar | ✅ Implementado |
| Fiscal | ⬜ Futuro |
| CRM | ⬜ Futuro |

## Funcionalidades

- Autenticação JWT com refresh token e logout.
- Controle de permissões granulares por módulo.
- Cadastros base com paginação, filtros, auditoria e soft delete.
- Pessoa como entidade central com papéis comerciais e operacionais.
- Produto separado em preço, fiscal, fornecedor, imagem, código de barras e unidade.
- Estoque com saldo, reserva, movimentação e consulta rápida de disponibilidade.
- Compras com pedido, entrada de mercadoria e integração com estoque.
- Vendas com pedido, itens, reserva automática e faturamento.
- Pagamentos com formas e condições de parcelamento.
- Financeiro com contas e parcelas, contas bancárias, extratos e lançamentos bancários, movimentos rastreáveis, dashboard, DRE, centros de custo e plano de contas integrados.

## Fluxos de Negócio

```text
Cliente
  ↓
Pedido de Venda
  ↓
Reserva de Estoque
  ↓
Faturamento
  ↓
Financeiro
  ↓
Fiscal
```

```text
Fornecedor
  ↓
Pedido de Compra
  ↓
Entrada de Mercadoria
  ↓
Estoque
  ↓
Financeiro
```

## Como Executar

Pré-requisitos:

- Java 25
- Maven
- Docker e Docker Compose
- PostgreSQL, quando não usar Docker

Subir infraestrutura:

```powershell
docker compose -f docker/compose/docker-compose.yml up -d
```

Executar backend:

```powershell
mvn test
mvn spring-boot:run
```

> O wrapper Maven (`mvnw.cmd`) deve ser mantido funcional para garantir portabilidade entre desenvolvedores.

## Testes

O projeto possui testes para:

- Services
- Controllers
- Segurança
- Integração
- Business Flow
- Concorrência
- Performance
- Idempotência de migrations

Resultado atual:

```text
BUILD SUCCESS
Tests run: 626, Failures: 0, Errors: 0
```

Mais detalhes:

- `docs/testes/testes.md`
- `docs/testes/organizacao-dos-testes.md`

## Banco de Dados

O banco é versionado com Flyway.

Atualmente existem migrations para:

- Segurança
- Cadastros
- Pessoas
- Produtos
- Estoque
- Compras
- Vendas
- Pagamentos
- Financeiro

Mais detalhes em:

- `docs/banco/banco-de-dados.md`
- `docs/banco/migrations.md`

## Documentação

| Documento | Descrição |
|---|---|
| `docs/arquitetura/arquitetura-geral.md` | Arquitetura do sistema |
| `docs/arquitetura/estrutura-do-projeto.md` | Estrutura de pastas |
| `docs/api/endpoints.md` | Endpoints da API |
| `docs/banco/banco-de-dados.md` | Convenções do banco |
| `docs/banco/migrations.md` | Lista das migrations |
| `docs/diagramas/README.md` | Diagramas Mermaid |
| `docs/testes/testes.md` | Testes automatizados |
| `docs/testes/organizacao-dos-testes.md` | Organização dos testes |
| `docs/seguranca/jwt.md` | Autenticação JWT |
| `docs/seguranca/permissoes.md` | Permissões |
| `docs/roadmap/roadmap.md` | Roadmap do produto |
| `docs/regras-negocio/regras-negocio.md` | Regras de negócio |
| `docs/adr/README.md` | Decisões arquiteturais |
| `docs/padroes/modulo.md` | Padrão de implementação de módulos |

## Roadmap

### Concluído

- ✅ Segurança
- ✅ Cadastros
- ✅ Pessoas
- ✅ Produtos
- ✅ Estoque
- ✅ Compras
- ✅ Vendas
- ✅ Pagamentos
- ✅ Contas a receber
- ✅ Contas a pagar
- ✅ Financeiro com centros de custo, plano de contas, DRE, contas bancárias, extratos e lançamentos bancários

### Em desenvolvimento

- 🚧 Financeiro completo
- 🚧 Frontend Angular

### Futuro

- ⬜ Fiscal
- ⬜ CRM
- ⬜ Dashboard
- ⬜ BI
- ⬜ Mobile
- ⬜ Marketplace
- ⬜ Integração bancária

## Contribuição

Contribuições, sugestões e revisões são bem-vindas. Para novas funcionalidades, mantenha o padrão modular, crie testes automatizados e atualize a documentação relacionada.

## Licença

Este projeto está licenciado sob a licença MIT.

## Autor

Desenvolvido por **Josiel Amorim**.
