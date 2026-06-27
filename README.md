<div align="center">

<h1>
  <img src="https://img.shields.io/badge/inv-Plus-4F46E5?style=for-the-badge&labelColor=1E1B4B" alt="invPlus"/>
</h1>

<p><strong>Sistema ERP completo, modular e escalável para gestão empresarial</strong></p>

<p>
  <img src="https://img.shields.io/badge/status-em%20desenvolvimento-yellow?style=flat-square"/>
  <img src="https://img.shields.io/badge/versão-0.1.0--alpha-orange?style=flat-square"/>
  <img src="https://img.shields.io/badge/maturidade-pré--alpha-red?style=flat-square"/>
  <img src="https://img.shields.io/badge/licença-MIT-blue?style=flat-square"/>
</p>

<p>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Angular-20-DD0031?style=flat-square&logo=angular&logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16+-336791?style=flat-square&logo=postgresql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-compose-2496ED?style=flat-square&logo=docker&logoColor=white"/>
</p>

</div>

> ⚠️ **Aviso de maturidade:** Este projeto está em fase **pré-alpha**. A arquitetura está sendo definida e nenhuma funcionalidade está em produção. Não utilize em ambiente produtivo.

---

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Objetivos](#objetivos)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Princípios arquiteturais](#princípios-arquiteturais)
- [Stack tecnológica](#stack-tecnológica)
- [Módulos](#módulos)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Banco de dados](#banco-de-dados)
- [Como executar](#como-executar)
- [Ordem de desenvolvimento](#ordem-de-desenvolvimento)
- [Roadmap](#roadmap)
- [Padrão de desenvolvimento](#padrão-de-desenvolvimento)
- [Contribuindo](#contribuindo)
- [Licença](#licença)
- [Autor](#autor)

---

## Sobre o projeto

O **invPlus** é um sistema ERP em desenvolvimento, criado para centralizar a gestão empresarial em uma única plataforma. O sistema cobre desde o cadastro de produtos e controle de estoque até faturamento, financeiro, fiscal e CRM.

A arquitetura inicial é um **monólito modular**, organizado por domínios de negócio, com evolução planejada para microsserviços conforme o sistema amadurece.

---

## Objetivos

- ✦ Arquitetura modular, escalável e de fácil manutenção
- ✦ Aplicação rigorosa de boas práticas de engenharia de software
- ✦ Código limpo, testável e bem documentado
- ✦ Cobertura de testes automatizados desde o início
- ✦ Evolução planejada e controlada para microsserviços
- ✦ Documentação completa de domínio, API e banco de dados
- ✦ Base sólida para um ERP robusto e confiável

---

## Funcionalidades

| # | Funcionalidade | Status |
|---|---|---|
| 01 | Cadastro de produtos | ✅ Planejado |
| 02 | Controle de estoque | ✅ Planejado |
| 03 | Gestão de compras | ✅ Planejado |
| 04 | Gestão de vendas | ✅ Planejado |
| 05 | Faturamento | ✅ Planejado |
| 06 | Controle financeiro | ✅ Planejado |
| 07 | Emissão fiscal (NF-e) | ✅ Planejado |
| 08 | CRM | ✅ Planejado |
| 09 | Dashboard gerencial | ⬜ Futuro |
| 10 | Relatórios e BI | ⬜ Futuro |
| 11 | API Pública | ⬜ Futuro |
| 12 | Integração bancária | ⬜ Futuro |
| 13 | Integração com marketplace | ⬜ Futuro |
| 14 | Aplicativo mobile | ⬜ Futuro |

---

## Arquitetura

O invPlus adota um **monólito modular** como ponto de partida, garantindo organização e coesão interna antes de qualquer decisão de distribuição.

```
┌─────────────────────────────────────────────┐
│                  Usuário                    │
└─────────────────────┬───────────────────────┘
                      │ HTTPS
┌─────────────────────▼───────────────────────┐
│              Angular 20                     │
│         (SPA — TypeScript/SCSS)             │
└─────────────────────┬───────────────────────┘
                      │ REST / JSON
┌─────────────────────▼───────────────────────┐
│            Spring Boot 3.x                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │Controllers│  │ Services │  │  DTOs   │  │
│  └────┬─────┘  └────┬─────┘  └──────────┘  │
│       │              │                      │
│  ┌────▼──────────────▼──────────────────┐   │
│  │           Repositories (JPA)         │   │
│  └──────────────────────────────────────┘   │
│  ┌──────────┐  ┌──────────┐                 │
│  │ Security │  │  Flyway  │                 │
│  │   JWT    │  │Migrations│                 │
│  └──────────┘  └──────────┘                 │
└─────────────────────┬───────────────────────┘
                      │ JDBC
┌─────────────────────▼───────────────────────┐
│             PostgreSQL 16+                  │
└─────────────────────────────────────────────┘

        Roadmap de infraestrutura:
        ┌────────┐    ┌───────────┐
        │ Redis  │    │ RabbitMQ  │
        │(cache) │    │(mensageria│
        └────────┘    └───────────┘
```

### Fluxo de uma requisição

```
Request → Controller → Service → Repository → Database
                   ↑         ↑
                  DTO      Domain Entity
```

---

## Princípios arquiteturais

| Princípio | Aplicação no projeto |
|---|---|
| **Domain-Driven Design (DDD)** | Organização por domínios de negócio (módulos) com linguagem ubíqua |
| **Clean Architecture** | Separação clara entre Controller, Service, Repository e Domain |
| **SOLID** | Aplicado na criação de classes, interfaces e injeção de dependência |
| **Modular Monolith** | Módulos independentes dentro de uma única aplicação |
| **RESTful API** | Endpoints seguindo convenções REST (verbos, status codes, recursos) |
| **Repository Pattern** | Abstração da camada de persistência via Spring Data JPA |
| **DTO Pattern** | Separação entre entidades de domínio e objetos de transferência |
| **Service Layer** | Toda regra de negócio centralizada na camada de serviço |
| **JWT Authentication** | Autenticação stateless com tokens assinados |

---

## Stack tecnológica

| Camada | Tecnologia | Responsabilidade |
|---|---|---|
| Backend | Java 21 + Spring Boot 3.x | API REST e regras de negócio |
| Frontend | Angular 20 + TypeScript | Interface do usuário (SPA) |
| Banco de dados | PostgreSQL 16+ | Persistência relacional |
| ORM | Spring Data JPA + Hibernate | Mapeamento objeto-relacional |
| Segurança | Spring Security + JWT | Autenticação e autorização |
| Migrações | Flyway | Versionamento do schema do banco |
| Validação | Bean Validation | Validação de dados de entrada |
| Build | Maven | Gerenciamento de dependências |
| Containerização | Docker + Docker Compose | Ambiente isolado e reproduzível |
| Cache *(roadmap)* | Redis | Cache de sessão e dados frequentes |
| Mensageria *(roadmap)* | RabbitMQ | Comunicação assíncrona entre módulos |

---

## Módulos

| Módulo | Responsabilidade | Principais entidades |
|---|---|---|
| 🔐 **Segurança** | Autenticação, perfis e auditoria | Usuário, Perfil, Permissão, Auditoria |
| 🏢 **Cadastros** | Dados base utilizados por outros módulos | Empresa, Endereço, Categoria, Marca, UoM |
| 👥 **Pessoas** | Cadastro unificado de pessoas | Pessoa, Cliente, Fornecedor, Transportadora, Funcionário |
| 📦 **Produtos** | Catálogo e precificação | Produto, Tabela de Preço, Produto Fornecedor |
| 🏪 **Estoque** | Saldos, movimentações e inventário | Depósito, Estoque, Movimentação, Lote, Inventário |
| 🛒 **Compras** | Aquisição de produtos | Pedido de Compra, Itens, Recebimento, Histórico |
| 💼 **Vendas** | Processo comercial | Pedido de Venda, Itens, Comissão, Histórico |
| 💳 **Pagamentos** | Formas e condições de pagamento | Forma de Pagamento, Condição, Parcelas |
| 🧾 **Faturamento** | Geração de faturas | Fatura, Parcelas da Fatura |
| 💰 **Financeiro** | Fluxo de caixa e contas | Conta Receber/Pagar, Movimento Financeiro, Centro de Custo |
| 🗂️ **Fiscal** | Notas fiscais e tributação | Nota Fiscal, Itens, Impostos |
| 🤝 **CRM** | Relacionamento com clientes | Lead, Oportunidade, Atendimento, Ordem de Serviço |

---

## Estrutura do projeto

```
invPlus/
├── backend/
│   └── src/
│       ├── main/
│       │   ├── java/br/com/invplus/
│       │   │   ├── config/            # Configurações globais
│       │   │   ├── shared/            # Utilitários compartilhados
│       │   │   ├── security/          # JWT, filtros, autenticação
│       │   │   └── modules/
│       │   │       ├── cadastros/
│       │   │       ├── pessoas/
│       │   │       ├── produtos/
│       │   │       ├── estoque/
│       │   │       ├── compras/
│       │   │       ├── vendas/
│       │   │       ├── pagamentos/
│       │   │       ├── faturamento/
│       │   │       ├── financeiro/
│       │   │       ├── fiscal/
│       │   │       └── crm/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/      # Scripts Flyway
│       └── test/
├── frontend/
│   └── src/
│       ├── app/
│       │   └── modules/               # Espelho dos módulos backend
│       ├── assets/
│       └── environments/
├── database/
│   ├── migrations/                    # Referência das migrations
│   ├── seeds/                         # Dados iniciais
│   └── scripts/                       # Scripts utilitários
├── docker/
│   ├── postgres/
│   ├── redis/
│   ├── rabbitmq/
│   └── compose/
├── docs/
│   ├── arquitetura/
│   ├── api/
│   ├── der/                           # DER em PDF
│   ├── requisitos/
│   ├── regras-negocio/
│   └── sprint/
├── docker-compose.yml
└── README.md
```

### Estrutura interna de cada módulo

```
modules/
└── produtos/
    ├── controller/       # Endpoints REST
    ├── service/          # Regras de negócio
    ├── repository/       # Acesso ao banco
    ├── domain/           # Entidades JPA
    └── dto/              # Objetos de transferência
```

---

## Banco de dados

O PostgreSQL é o banco principal, evoluído com **Flyway** via migrations versionadas por módulo:

```
V1__schema_seguranca.sql
V2__schema_cadastros.sql
V3__schema_pessoas.sql
V4__schema_produtos.sql
V5__schema_estoque.sql
V6__schema_compras.sql
V7__schema_vendas.sql
V8__schema_pagamentos.sql
V9__schema_faturamento.sql
V10__schema_financeiro.sql
V11__schema_fiscal.sql
V12__schema_crm.sql
```

Convenções do banco:
- PKs do tipo `UUID` com `gen_random_uuid()`
- Campos de auditoria em todas as tabelas: `criado_em`, `atualizado_em`, `criado_por`, `atualizado_por`
- Soft delete via campo `ativo BOOLEAN`
- `NUMERIC(15,4)` para preços e `NUMERIC(14,3)` para quantidades
- `TIMESTAMPTZ` para todos os campos de data/hora

---

## Como executar

> ⚠️ Esta seção será atualizada após a configuração do Docker Compose e do backend.

### Pré-requisitos

- Docker e Docker Compose instalados
- Java 21+
- Maven 3.9+
- Node.js 20+ (para o frontend)

### Ambiente completo

```bash
# Clonar o repositório
git clone https://github.com/seu-usuario/invplus.git
cd invplus

# Subir os serviços de infraestrutura
docker compose up -d

# Backend
cd backend
mvn spring-boot:run

# Frontend (em outro terminal)
cd frontend
npm install
ng serve
```

### Acessos locais (após configuração)

| Serviço | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API REST | http://localhost:8080 |
| PostgreSQL | localhost:5432 |

---

## Ordem de desenvolvimento

```
 Fase 1 — Infraestrutura
 ├── [ ] 1. Preparação do ambiente
 ├── [ ] 2. Configuração do Git
 ├── [ ] 3. Configuração do Docker
 ├── [ ] 4. PostgreSQL
 ├── [ ] 5. Projeto Spring Boot
 └── [ ] 6. Flyway

 Fase 2 — Backend (módulos)
 ├── [ ] 7.  Segurança (JWT + Perfis)
 ├── [ ] 8.  Cadastros
 ├── [ ] 9.  Pessoas
 ├── [ ] 10. Produtos
 ├── [ ] 11. Estoque
 ├── [ ] 12. Compras
 ├── [ ] 13. Vendas
 ├── [ ] 14. Pagamentos
 ├── [ ] 15. Faturamento
 ├── [ ] 16. Financeiro
 ├── [ ] 17. Fiscal
 └── [ ] 18. CRM

 Fase 3 — Frontend e entrega
 ├── [ ] 19. Relatórios
 └── [ ] 20. Frontend Angular
```

---

## Roadmap

### ✅ Concluído
- [x] Definição da arquitetura
- [x] Modelagem do domínio (DDD)
- [x] DER completo com tipos PostgreSQL e restrições

### 🔄 Em andamento
- [ ] Configuração do ambiente Docker
- [ ] Estrutura inicial do projeto Spring Boot

### ⬜ Backend
- [ ] Módulo de Segurança (JWT)
- [ ] Módulo de Cadastros
- [ ] Módulo de Pessoas
- [ ] Módulo de Produtos
- [ ] Módulo de Estoque
- [ ] Módulo de Compras
- [ ] Módulo de Vendas
- [ ] Módulo Financeiro
- [ ] Módulo Fiscal
- [ ] Módulo CRM

### ⬜ Qualidade
- [ ] Testes unitários (JUnit 5 + Mockito)
- [ ] Testes de integração
- [ ] Documentação da API (OpenAPI/Swagger)
- [ ] Cobertura de testes ≥ 80%

### ⬜ Frontend e entrega
- [ ] Frontend Angular 20
- [ ] Dashboard gerencial
- [ ] Deploy (Docker + CI/CD)
- [ ] **Versão 1.0** 🎯

### ⬜ Futuro
- [ ] Redis (cache)
- [ ] RabbitMQ (mensageria)
- [ ] Integração bancária
- [ ] Integração com marketplace
- [ ] API Pública
- [ ] Aplicativo mobile
- [ ] Microsserviços

---

## Padrão de desenvolvimento

### Branches

```
main              → produção (protegida)
develop           → integração contínua
feature/nome      → nova funcionalidade
fix/nome          → correção de bug
refactor/nome     → refatoração
docs/nome         → documentação
```

**Exemplos:**
```
feature/cadastro-produtos
feature/autenticacao-jwt
fix/correcao-calculo-imposto
docs/atualiza-der
```

### Commits — Conventional Commits

```
<tipo>(<escopo>): <descrição curta>
```

| Tipo | Uso |
|---|---|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `docs` | Documentação |
| `style` | Formatação (sem mudança lógica) |
| `refactor` | Refatoração sem nova feature ou fix |
| `test` | Adição ou correção de testes |
| `chore` | Configuração, dependências, build |
| `perf` | Melhoria de performance |

**Exemplos:**
```
feat(produtos): adiciona endpoint de cadastro de produto
feat(seguranca): implementa autenticação com JWT
fix(estoque): corrige cálculo de saldo após movimentação
docs(der): atualiza diagrama com módulo fiscal
test(produtos): adiciona testes unitários para ProdutoService
chore(docker): configura docker-compose para ambiente local
```

### Padrão de endpoint REST

```
GET    /api/v1/produtos          → listar
GET    /api/v1/produtos/{id}     → buscar por id
POST   /api/v1/produtos          → criar
PUT    /api/v1/produtos/{id}     → atualizar
DELETE /api/v1/produtos/{id}     → remover (soft delete)
```

### Padrão de resposta da API

```json
{
  "success": true,
  "data": { },
  "message": "Operação realizada com sucesso",
  "timestamp": "2025-01-01T00:00:00Z"
}
```

---

## Contribuindo

Este é um projeto pessoal em desenvolvimento. Contribuições, sugestões e feedbacks são bem-vindos.

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/minha-feature`)
3. Commit suas mudanças seguindo o padrão de commits (`git commit -m 'feat: adiciona minha feature'`)
4. Push para a branch (`git push origin feature/minha-feature`)
5. Abra um Pull Request para a branch `develop`

### Diretrizes

- Siga os princípios arquiteturais do projeto (DDD, Clean Architecture, SOLID)
- Escreva testes para novas funcionalidades
- Mantenha a documentação atualizada
- Respeite a estrutura modular existente

---

## Licença

Este projeto está licenciado sob a **MIT License** — veja o arquivo [LICENSE](LICENSE) para detalhes.

```
MIT License — Copyright (c) 2025 Josiel Amorim
```

---

## Autor

<div align="center">

Desenvolvido com dedicação por **Josiel Amorim**

<br/>

*"A arquitetura atual prioriza organização, clareza e facilidade de manutenção,*
*deixando microsserviços e recursos avançados para versões futuras."*

</div>