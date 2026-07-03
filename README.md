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
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Angular-20-DD0031?style=flat-square&logo=angular&logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-18-336791?style=flat-square&logo=postgresql&logoColor=white"/>
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
- [Segurança e autenticação](#segurança-e-autenticação)
- [Testes](#testes)
- [Mudanças locais ainda não commitadas](#mudanças-locais-ainda-não-commitadas)
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
│            Spring Boot 4.1                  │
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
│             PostgreSQL 18                   │
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
| Backend | Java 25 + Spring Boot 4.1 | API REST e regras de negócio |
| Frontend | Angular 20 + TypeScript | Interface do usuário (SPA) |
| Banco de dados | PostgreSQL 18 | Persistência relacional |
| ORM | Spring Data JPA + Hibernate | Mapeamento objeto-relacional |
| Segurança | Spring Security + JWT | Login stateless, autenticação e autorização |
| Migrações | Flyway | Versionamento do schema e seeds iniciais |
| Validação | Bean Validation | Validação de dados de entrada |
| Cache | Spring Cache | Cache local inicial para cadastros de baixa alteração |
| Logs | SLF4J + Logback | Logging padronizado da API |
| Build | Maven | Gerenciamento de dependências |
| Containerização | Docker + Docker Compose | Ambiente isolado e reproduzível |
| Cache distribuído *(roadmap)* | Redis | Cache compartilhado entre instâncias |
| Mensageria *(roadmap)* | RabbitMQ | Comunicação assíncrona entre módulos |

---

## Módulos

| Módulo | Responsabilidade | Principais entidades |
|---|---|---|
| 🔐 **Segurança** | Autenticação, perfis e auditoria | Usuário, Perfil, Permissão, Auditoria |
| 🏢 **Cadastros** | Dados base utilizados por outros módulos | País, Estado, Cidade, Endereço, Empresa, Filial, Categoria, Marca, UoM, Arquivo |
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
│       │   │   ├── common/            # Constantes e enums comuns
│       │   │   ├── shared/            # BaseEntity, respostas e exceções
│       │   │   ├── security/          # JWT, filtros, entidades e login
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
│       │       └── META-INF/
│       └── test/
│           ├── java/                  # Testes de contexto, auth e repositories
│           └── resources/
├── frontend/
│   └── src/
│       ├── app/
│       │   └── modules/               # Espelho dos módulos backend
│       ├── assets/
│       └── environments/
├── database/
│   ├── migrations/database/           # Migrations Flyway executadas pelo backend
│   ├── seeds/                         # Reservado para seeds auxiliares
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
├── docker/compose/docker-compose.yml
└── README.md
```

### Estrutura interna padrão de cada módulo

```
modules/
├── cadastros/
├── pessoas/
├── produtos/
├── estoque/
├── compras/
├── vendas/
├── pagamentos/
├── faturamento/
├── financeiro/
├── fiscal/
└── crm/
```

Cada módulo segue o mesmo contrato:

```text
controller/       # Endpoints REST
dto/request/      # Payloads de entrada
dto/response/     # Payloads de saída
entity/           # Entidades JPA
mapper/           # Conversões entre entity/dto
repository/       # Repositories Spring Data
service/          # Regras de negócio
specification/    # Filtros dinâmicos e consultas compostas
validator/        # Validações específicas do módulo
exception/        # Exceções específicas do módulo
```

### Estrutura atual do módulo de segurança

```
security/
├── config/               # SecurityConfig
├── controller/           # AuthController
├── dto/                  # LoginRequest e LoginResponse
├── entity/               # Usuario, Perfil, Permissao e Auditoria
├── enums/                # Acoes de permissao e auditoria
├── filter/               # JwtAuthenticationFilter
├── repository/           # Repositories Spring Data JPA
└── service/              # Login, JWT e UserDetailsService
```

---

## Banco de dados

O PostgreSQL é o banco principal, evoluído com **Flyway** via migrations versionadas em `database/migrations/database`.

```
V1__init_extensions.sql
V2__schema_seguranca.sql
V3__seed_seguranca_inicial.sql
V4__refresh_token.sql
V5__auditoria_refresh_token.sql
V6__refresh_token_ip_varchar.sql
V7__schema_cadastros.sql
V8__endereco_uf_varchar.sql
V9__cadastros_fundamentais.sql
V10__cadastros_fase_2.sql
V11__permissoes_cadastros_fase_2.sql
V12__categoria_hierarquia_nome_unico.sql
V13__permissao_categoria_wildcard.sql
V14__categoria_ordem_marca_campos.sql
V15__permissao_marca_wildcard.sql
V16__empresa_melhorias.sql
V17__permissao_empresa_wildcard.sql
V18__arquivo_melhorias.sql
V19__permissao_arquivo_wildcard.sql
V20__arquivo_colunas_legadas_opcionais.sql
V21__schema_pessoa.sql
V22__permissoes_pessoa.sql
```

### Migration V1

Habilita extensões PostgreSQL usadas pelo módulo de segurança:

- `pgcrypto`, para geração de UUID e hash BCrypt no seed inicial.
- `citext`, para email case-insensitive em `usuario.email`.

### Migration V2

Cria o schema inicial de segurança:

- `usuario`
- `perfil`
- `permissao`
- `usuario_perfil`
- `perfil_permissao`
- `auditoria`

Também cria índices, constraints, comentários e trigger para atualização automática de `usuario.atualizado_em`.

### Migration V3

Cria o seed inicial de segurança:

- perfil `ADMIN`;
- permissões básicas para `SEGURANCA`, `USUARIO`, `PERFIL` e `PERMISSAO`;
- usuário inicial `admin@invplus.com`;
- vínculos entre usuário, perfil e permissões.

> O seed usa a senha inicial `admin123`, gravada como hash BCrypt no banco. Antes de produção, a estratégia recomendada é exigir troca de senha no primeiro login ou substituir esse bootstrap por variável de ambiente segura.

### Migrations V4, V5 e V6

Evoluem o fluxo de autenticação com refresh token:

- `refresh_token`, persistindo apenas hash SHA-256 do token opaco;
- auditoria de eventos de refresh token;
- ajuste de compatibilidade do campo `ip`.

### Migration V7

Cria o schema inicial do módulo Cadastros:

- `empresa`
- `endereco`
- `categoria`
- `marca`
- `unidade_medida`
- `arquivo`

Também cria índices, constraints, triggers de `atualizado_em`, comentários SQL e permissões `CADASTROS` vinculadas ao perfil `ADMIN`.

### Migration V8

Ajusta `endereco.uf` para `VARCHAR(2)`, alinhando o tipo do banco ao mapeamento JPA validado pelo Hibernate.

### Migration V9

Consolida a primeira fase do Sprint 4 - Cadastros Fundamentais:

- adiciona `version` nas tabelas auditadas para controle de concorrência otimista com `@Version`;
- cria `pais`, `estado`, `cidade` e `filial`;
- relaciona `endereco` com `cidade`;
- cria índices simples e compostos para consultas por hierarquia e registros ativos;
- adiciona triggers de `atualizado_em` nas novas tabelas.

### Migration V10

Consolida a base da fase 2 de Cadastros com tabelas idempotentes para:

- `unidade_medida`
- `categoria`
- `marca`
- `empresa`
- `arquivo`

No fluxo atual, `UnidadeMedida` foi a primeira entidade concluída com checklist completa.

### Migration V11

Adiciona permissões específicas por entidade da fase 2:

- `UNIDADE_MEDIDA:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`
- `CATEGORIA:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`
- `MARCA:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`
- `EMPRESA:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`
- `ARQUIVO:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`

### Migration V12

Ajusta `categoria` para as regras finais da fase 2:

- adiciona `categoria_pai_id` para hierarquia opcional;
- remove a unicidade antiga por `nome/tipo`;
- cria índice único parcial por `LOWER(nome)` apenas para registros ativos e não excluídos;
- cria índice para consultas por categoria pai;
- adiciona FK para a própria tabela e check para impedir auto-relacionamento direto.

### Migration V13

Adiciona a permissão wildcard `CATEGORIA:*` e vincula ao perfil `ADMIN`, mantendo também as permissões granulares de criar, visualizar, editar e excluir.

### Migration V14

Refina `categoria` e `marca`:

- adiciona `categoria.ordem` para ordenação previsível da árvore;
- cria índice parcial para listar categorias por pai, ordem e nome;
- adiciona `marca.site` e `marca.fabricante`;
- troca a unicidade antiga de `marca.nome` por índice único parcial em `LOWER(nome)` apenas para marcas ativas e não excluídas.

### Migration V15

Adiciona a permissão wildcard `MARCA:*` e vincula ao perfil `ADMIN`.

### Migration V16

Refina `empresa` para a fase 2:

- adiciona `inscricao_municipal`, `regime_tributario` e `crt`;
- garante `version` para concorrência otimista;
- troca a unicidade antiga de `cnpj` por índice único parcial apenas para empresas ativas e não excluídas;
- adiciona índices para `cnpj` e registros ativos;
- adiciona constraint para CRT válido.

### Migration V17

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `EMPRESA:*`
- `EMPRESA:CRIAR`
- `EMPRESA:VISUALIZAR`
- `EMPRESA:EDITAR`
- `EMPRESA:EXCLUIR`

### Migration V18

Refina `arquivo` para a fase 2:

- adiciona `tipo_mime`, `extensao`, `hash_sha256`, `url` e `storage`;
- prepara armazenamento em `LOCAL`, `S3`, `MINIO`, `AZURE` e `GOOGLE_CLOUD`;
- adiciona índices para registros ativos, hash SHA-256 e vínculo `entidade/entidade_id`;
- adiciona checks para tamanho positivo, storage válido e formato do hash SHA-256.

### Migration V19

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `ARQUIVO:*`
- `ARQUIVO:CRIAR`
- `ARQUIVO:VISUALIZAR`
- `ARQUIVO:EDITAR`
- `ARQUIVO:EXCLUIR`

### Migration V20

Torna opcionais as colunas legadas `content_type` e `caminho`, preservando compatibilidade com bases antigas e permitindo o contrato novo com `tipo_mime` e `url`.

### Migration V21

Cria a tabela `pessoa`, base do módulo Pessoas:

- `tipo_pessoa` obrigatório com valores `FISICA` ou `JURIDICA`;
- `cpf_cnpj` obrigatório, normalizado apenas com números;
- unicidade parcial de `cpf_cnpj` apenas entre registros ativos;
- índices para registros ativos e busca por documento;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V22

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `PESSOAS:*`
- `PESSOA:*`
- `PESSOA:CRIAR`
- `PESSOA:VISUALIZAR`
- `PESSOA:EDITAR`
- `PESSOA:EXCLUIR`

Convenções do banco:
- PKs do tipo `UUID` com `gen_random_uuid()`
- Campos de auditoria em todas as tabelas: `criado_em`, `atualizado_em`, `criado_por`, `atualizado_por`
- Controle de concorrência otimista com `version` nas entidades que herdam `BaseEntity`
- Auditoria automática via Spring Data JPA Auditing, usando o UUID do usuário autenticado
- Soft delete em entidades que exigem exclusão lógica, com `ativo BOOLEAN` e/ou `deleted_at`
- Consultas padrão de entidades com soft delete retornam apenas `ativo = true` e `deleted_at IS NULL`
- Listagens REST retornam `Page<T>` e aceitam `page`, `size` e `sort`
- `NUMERIC(15,4)` para preços e `NUMERIC(14,3)` para quantidades
- `TIMESTAMPTZ` para todos os campos de data/hora

### Infraestrutura compartilhada de domínio

O backend possui bases reutilizáveis para evitar repetição nos próximos módulos:

- `BaseRepository<T>`: herda `JpaRepository` e `JpaSpecificationExecutor`, além de métodos padrão para registros ativos.
- `BaseCrudService<T, REQ, RES>`: concentra listar, buscar, criar, atualizar e excluir logicamente.
- `BaseCrudController`: centraliza helpers de resposta REST.
- `CrudMapper<T, REQ, RES>`: contrato para mapeamento entre request, entidade e response.
- `shared/events`: ponto inicial para eventos de domínio.
- `shared/domain/valueobject`: objetos de valor iniciais como `Money`, `Email`, `Cnpj` e `Cep`.

---

## Como executar

### Pré-requisitos

- Docker e Docker Compose instalados
- Java 25
- Maven 3.9+
- Node.js 20+ (para o frontend)

### Ambiente completo

1. Prepare as variáveis de infraestrutura local:

```bash
cd docker/compose
cp .env.example .env
```

Edite `docker/compose/.env` e substitua os valores `change-me`.

2. Suba os serviços de infraestrutura:

```bash
docker compose --env-file docker/compose/.env -f docker/compose/docker-compose.yml up -d
```

3. Configure as variáveis usadas pelo backend:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/invplus
SPRING_DATASOURCE_USERNAME=invplus_user
SPRING_DATASOURCE_PASSWORD=<senha_do_postgres>
INVPLUS_JWT_SECRET=<segredo_forte_para_jwt>
INVPLUS_JWT_EXPIRATION_MINUTES=60
INVPLUS_JWT_REFRESH_EXPIRATION_DAYS=7
```

No PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/invplus"
$env:SPRING_DATASOURCE_USERNAME="invplus_user"
$env:SPRING_DATASOURCE_PASSWORD="<senha_do_postgres>"
$env:INVPLUS_JWT_SECRET="<segredo_forte_para_jwt>"
$env:INVPLUS_JWT_EXPIRATION_MINUTES="60"
$env:INVPLUS_JWT_REFRESH_EXPIRATION_DAYS="7"
```

4. Execute o backend:

```bash
# Clonar o repositório
git clone https://github.com/seu-usuario/invplus.git
cd invplus

# Backend
cd backend
./mvnw spring-boot:run
```

5. Execute o frontend quando ele estiver configurado:

```bash
cd frontend
npm install
ng serve
```

### Acessos locais (após configuração)

| Serviço | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API REST | http://localhost:8080 |
| PostgreSQL | localhost:5433 |
| pgAdmin | http://localhost:8081 |
| RabbitMQ Management | http://localhost:15672 |

---

## Segurança e autenticação

O backend já possui um fluxo inicial de autenticação real com Spring Security + JWT.

### Endpoint de login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Payload:

```json
{
  "email": "admin@invplus.com",
  "senha": "admin123"
}
```

Resposta esperada:

```json
{
  "tokenType": "Bearer",
  "accessToken": "...",
  "refreshToken": "...",
  "expiresAt": "2026-07-01T00:00:00Z",
  "usuario": {
    "id": "...",
    "nome": "Administrador",
    "email": "admin@invplus.com",
    "perfis": ["ADMIN"],
    "permissoes": ["SEGURANCA:*"]
  }
}
```

Para acessar rotas protegidas:

```http
Authorization: Bearer <accessToken>
```

### Refresh token

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "..."
}
```

O refresh token e salvo no banco apenas como hash SHA-256. O token puro aparece somente na resposta da API.

### Logout

```http
POST /api/v1/auth/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "..."
}
```

O logout revoga o refresh token informado.

### Usuario autenticado

```http
GET /api/v1/auth/me
Authorization: Bearer <accessToken>
```

Esse endpoint exige a permissao `SEGURANCA:*` e serve como primeira validacao real de `@PreAuthorize`.

### Componentes implementados

- `SecurityConfig`: configuração stateless, rotas públicas e rotas autenticadas.
- `AuthController`: endpoints `/login`, `/refresh`, `/logout` e `/me`.
- `SecurityService`: autenticação, refresh token, logout e atualização de `ultimoLogin`.
- `RefreshTokenService`: cria, valida e revoga refresh tokens persistidos como hash.
- `UsuarioDetailsService`: carrega usuários reais do banco.
- `JwtService`: geração e validação de JWT HMAC SHA-256.
- `JwtAuthenticationFilter`: autenticação por header `Authorization`.
- `GlobalExceptionHandler`: trata credenciais inválidas com `401`, acesso negado com `403`, duplicidades com `409` e validações com `400`.

### Tratamento de erros

O backend centraliza erros em `GlobalExceptionHandler` e responde sempre no formato `ErrorResponse`.

- `400 Bad Request`: validações de entrada, `BusinessException` e `InvalidOperationException`.
- `409 Conflict`: duplicidades e conflitos de estado via `ConflictException`.
- `409 Conflict`: conflito de concorrência otimista via `OptimisticLockException` ou `ObjectOptimisticLockingFailureException`.
- `404 Not Found`: registros não encontrados ou excluídos logicamente.
- `401 Unauthorized`: token ausente/inválido ou credenciais inválidas.
- `403 Forbidden`: usuário autenticado sem permissão.

### Observações de segurança

- `INVPLUS_JWT_SECRET` é obrigatório no profile principal.
- `INVPLUS_JWT_REFRESH_EXPIRATION_DAYS` controla a validade do refresh token.
- O seed inicial usa `admin123` apenas para bootstrap.
- O `docker-compose.yml` não contém mais senhas fixas; use `docker/compose/.env`.

---

## Testes

Testes existentes:

- `BackendApplicationTests`: valida o carregamento do contexto Spring.
- `SecuritySeedRepositoryTests`: consulta o seed inicial via repositories.
- `AuthControllerTests`: valida login, senha inválida, usuário inexistente, refresh token, logout, token inválido, token expirado, rota protegida e permissão negada.
- `CadastrosRepositoryTests`: valida persistência das entidades base, permissão `CADASTROS:*` e ocultação de registros excluídos logicamente.
- `CadastrosControllerTests`: valida criação/consulta de empresa via API, bloqueio sem token, ocultação de empresa excluída logicamente e criação da hierarquia País → Estado → Cidade → Endereço → Filial.
- `UnidadeMedidaServiceTest`: valida regras unitárias de criação, duplicidade, paginação e exclusão lógica.
- `UnidadeMedidaControllerTests`: valida CRUD completo, sigla duplicada, paginação, soft delete, `401` sem token e `403` sem permissão.
- `CategoriaServiceTest`: valida criação com pai opcional, ordem, caminho completo, nome único ativo, paginação, soft delete, bloqueio de auto-relacionamento e ciclo hierárquico.
- `CategoriaControllerTests`: valida CRUD completo, hierarquia, nome duplicado, paginação, soft delete, `401` sem token e `403` sem permissão.
- `MarcaServiceTest`: valida criação, campos opcionais, nome único ativo, paginação e exclusão lógica.
- `MarcaControllerTests`: valida CRUD completo, nome duplicado, paginação, soft delete, `401` sem token e `403` sem permissão.
- `EmpresaServiceTest`: valida criação, CNPJ único ativo, CNPJ inválido, email inválido, paginação e exclusão lógica.
- `EmpresaControllerTests`: valida CRUD completo, CNPJ duplicado, paginação, soft delete, `401` sem token, `403` sem permissão, email inválido e CNPJ inválido.
- `ArquivoServiceTest`: valida criação, hash SHA-256 único ativo, hash inválido, paginação e exclusão lógica.
- `ArquivoControllerTests`: valida CRUD completo, hash duplicado, paginação, filtro por entidade, soft delete, `401` sem token, `403` sem permissão e hash inválido.
- `PessoaServiceTest`: valida pessoa física, pessoa jurídica, CPF/CNPJ único ativo, documento incompatível com tipo, paginação e exclusão lógica.
- `PessoaControllerTests`: valida CRUD completo, pessoa jurídica, CPF/CNPJ duplicado, paginação, soft delete, `401` sem token, `403` sem permissão e documentos incompatíveis com o tipo.

Para executar:

```powershell
cd backend
.\mvnw.cmd test
```

Resultado esperado no estado atual:

```text
BUILD SUCCESS
Tests run: 82, Failures: 0, Errors: 0
```

Os testes usam o profile `test`, configurado em `backend/src/test/resources/application-test.yml`.

---

## Mudanças locais ainda não commitadas

Esta seção documenta o estado atual do workspace antes do próximo commit.

### Backend

- Adicionada arquitetura base em `br.com.invplus`:
  - `common`
  - `shared`
  - `security`
  - `modules`
- Adicionadas entidades compartilhadas:
  - `BaseEntity`
  - `SoftDeleteEntity`
- Adicionadas respostas e exceções compartilhadas:
  - `ApiResponse`
  - `ErrorResponse`
  - `BusinessException`
  - `ConflictException`
  - `InvalidOperationException`
  - `ResourceNotFoundException`
  - `GlobalExceptionHandler`
- Implementado módulo inicial de segurança:
  - `Usuario`
  - `Perfil`
  - `Permissao`
  - `Auditoria`
  - repositories JPA
  - DTOs de login
  - serviço de autenticação
  - filtro JWT
  - configuração Spring Security
- Implementado módulo inicial de Cadastros:
  - entidades JPA `Pais`, `Estado`, `Cidade`, `Endereco`, `Empresa`, `Filial`, `Categoria`, `Marca`, `UnidadeMedida` e `Arquivo`;
  - repositories Spring Data herdando `BaseRepository` e `JpaSpecificationExecutor`;
  - DTOs de request/response para cada recurso;
  - mappers, specifications e validators para a fase fundamental;
  - services com criação, listagem paginada, busca, atualização e exclusão lógica;
  - controllers REST protegidos por permissões `CADASTROS`, retornando `Page<T>` nos GETs;
  - cache inicial com `@Cacheable` para Categoria, Marca, Arquivo, UnidadeMedida, Pais, Estado e Cidade;
  - comentários em classes e métodos para facilitar leitura do código.
- Adicionadas bases compartilhadas `BaseRepository`, `BaseCrudService`, `BaseCrudController` e `CrudMapper`.
- Adicionada auditoria automática com `AuditorAware<UUID>` e principal autenticado com UUID.
- Adicionadas bases para eventos de domínio em `shared/events`.
- Adicionados objetos de valor iniciais `Money`, `Email`, `Cnpj` e `Cep`.
- Adicionado `logback-spring.xml` para padronizar logs via SLF4J/Logback.
- Adicionado `spring-boot-starter-json` ao `pom.xml`, necessário para JSON/Jackson no Spring Boot 4.
- Adicionado `spring-boot-starter-cache` ao `pom.xml`.
- Atualizado `application.yml` para:
  - remover credenciais fixas;
  - exigir variáveis de ambiente;
  - configurar JWT;
  - desligar `open-in-view`;
  - desligar SQL formatado/verboso no profile principal;
  - configurar pool Hikari.
- Adicionado metadata de configuração em `META-INF/additional-spring-configuration-metadata.json`.

### Banco de dados

- `V1__init_extensions.sql` agora habilita `pgcrypto` e `citext`.
- `V2__schema_seguranca.sql` foi expandida para criar o schema real de segurança.
- `V3__seed_seguranca_inicial.sql` adiciona seed inicial:
  - perfil `ADMIN`;
  - permissões básicas;
  - usuário `admin@invplus.com`;
  - vínculos entre usuário, perfil e permissões.
- `V7__schema_cadastros.sql` cria as tabelas base do módulo Cadastros e permissões `CADASTROS`.
- `V8__endereco_uf_varchar.sql` ajusta `endereco.uf` para compatibilidade com validação JPA.
- `V9__cadastros_fundamentais.sql` adiciona `version`, cria `pais`, `estado`, `cidade` e `filial`, e reforça índices para consultas por registros ativos.
- `V10__cadastros_fase_2.sql` consolida as tabelas da fase 2 de Cadastros.
- `V11__permissoes_cadastros_fase_2.sql` adiciona permissões granulares por entidade.
- `V12__categoria_hierarquia_nome_unico.sql` adiciona hierarquia em Categoria e unicidade por nome ativo.
- `V13__permissao_categoria_wildcard.sql` adiciona `CATEGORIA:*`.
- `V14__categoria_ordem_marca_campos.sql` adiciona ordenação de Categoria e campos/índices finais de Marca.
- `V15__permissao_marca_wildcard.sql` adiciona `MARCA:*`.
- `V16__empresa_melhorias.sql` adiciona melhorias finais de Empresa, índices e constraint de CRT.
- `V17__permissao_empresa_wildcard.sql` adiciona `EMPRESA:*` e reforça permissões de Empresa.
- `V18__arquivo_melhorias.sql` adiciona storage, hash SHA-256, URL, extensão e índices finais de Arquivo.
- `V19__permissao_arquivo_wildcard.sql` adiciona `ARQUIVO:*` e reforça permissões de Arquivo.
- `V20__arquivo_colunas_legadas_opcionais.sql` libera colunas legadas após a migração para o contrato novo.
- `V21__schema_pessoa.sql` cria a tabela base `pessoa`.
- `V22__permissoes_pessoa.sql` adiciona permissões `PESSOAS:*`, `PESSOA:*` e CRUD de Pessoa.

### Docker

- `docker/compose/docker-compose.yml` agora usa variáveis de ambiente para senhas e usuários.
- PostgreSQL local expõe a porta `5433`.
- Criado `docker/compose/.env.example`.

### Testes

- Adicionado profile de teste em `backend/src/test/resources/application-test.yml`.
- Atualizado `BackendApplicationTests` para usar `@ActiveProfiles("test")`.
- Adicionados testes de repository para o seed de segurança.
- Adicionados testes de login real via `AuthControllerTests`.
- Adicionados testes de repository e controller para o módulo Cadastros.

### Scripts locais

- `criar-arquitetura-backend.ps1`: script usado para criar a arquitetura base.
- `criar-estrutura-security.ps1`: script usado para criar a estrutura inicial do módulo `security`.

> Observação: esses scripts refletem etapas de bootstrap e podem ficar fora do commit final caso o objetivo seja versionar apenas o estado resultante do projeto.

---

## Ordem de desenvolvimento

```
 Fase 1 — Infraestrutura
 ├── [x] 1. Preparação do ambiente
 ├── [ ] 2. Configuração do Git
 ├── [x] 3. Configuração do Docker
 ├── [x] 4. PostgreSQL
 ├── [x] 5. Projeto Spring Boot
 └── [x] 6. Flyway

 Fase 2 — Backend (módulos)
 ├── [x] 7.  Segurança (JWT + Perfis)
 ├── [x] 8.  Cadastros - fases 1 e 2
 ├── [ ] 9.  Pessoas - iniciado com Pessoa
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
- [x] Estrutura base do backend Spring Boot
- [x] Docker Compose para infraestrutura local
- [x] Flyway com migrations iniciais
- [x] Schema inicial de segurança
- [x] Seed inicial de segurança
- [x] Login real com Spring Security + JWT
- [x] Testes de contexto, seed e login
- [x] Módulo de Cadastros base com API REST
- [x] Sprint 4 - Fase 1: País, Estado, Cidade, Endereço, Empresa e Filial
- [x] Sprint 4 - Fase 2: UnidadeMedida concluída com checklist completa
- [x] Sprint 4 - Fase 2: Categoria concluída com checklist completa
- [x] Sprint 4 - Fase 2: Marca concluída com checklist completa
- [x] Sprint 4 - Fase 2: Empresa concluída com checklist completa
- [x] Sprint 4 - Fase 2: Arquivo concluída com checklist completa
- [x] Sprint 4 - Fase 2 concluída e revisada
- [x] Testes de repository e controller para Cadastros

### 🔄 Em andamento
- [ ] Consolidação do módulo de Segurança
- [x] Módulo Pessoas iniciado com entidade Pessoa

### ⬜ Backend
- [x] Módulo de Segurança (JWT)
- [x] Módulo de Cadastros - fases 1 e 2
- [ ] Módulo de Pessoas iniciado com Pessoa
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
