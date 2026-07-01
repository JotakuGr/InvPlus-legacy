# Arquitetura Geral - invPlus ERP

## Visao Geral

O invPlus ERP e desenvolvido inicialmente como um monolito modular, organizado por dominios de negocio.

A escolha por monolito modular permite iniciar com menor complexidade operacional, mantendo separacao interna por modulos e uma base preparada para evolucao futura.

## Tipo de Arquitetura

- Monolito modular
- API REST
- Backend Java 25 com Spring Boot 4.1
- Frontend Angular
- PostgreSQL 18
- Docker Compose para infraestrutura local
- Flyway para versionamento do banco
- Spring Security + JWT para autenticacao stateless

## Visao em Camadas

```text
Frontend Angular
      |
      v
API REST Spring Boot
      |
      v
Controllers
      |
      v
Services
      |
      v
Repositories
      |
      v
PostgreSQL
```

## Organizacao por Pacotes

```text
br.com.invplus
├── config
├── common
├── shared
├── security
└── modules
```

## Responsabilidades

### `config`

Configuracoes globais da aplicacao.

### `common`

Constantes e enums compartilhados, sem dependencia de regra de negocio especifica.

### `shared`

Elementos reutilizaveis por todos os modulos:

- entidades base;
- respostas padronizadas;
- excecoes compartilhadas;
- handler global de excecoes.

### `security`

Modulo responsavel por:

- login;
- JWT;
- filtro de autenticacao;
- usuario;
- perfil;
- permissao;
- auditoria.

### `modules`

Pacote reservado para os modulos de negocio:

- cadastros;
- pessoas;
- produtos;
- estoque;
- compras;
- vendas;
- pagamentos;
- faturamento;
- financeiro;
- fiscal;
- CRM.

## Fluxo de Autenticacao

```text
POST /api/v1/auth/login
      |
      v
AuthController
      |
      v
SecurityService
      |
      v
AuthenticationManager
      |
      v
UsuarioDetailsService
      |
      v
UsuarioRepository
      |
      v
PostgreSQL
      |
      v
JWT assinado retornado ao cliente
```

## Fluxo de Requisicao Autenticada

```text
Request com Authorization: Bearer <token>
      |
      v
JwtAuthenticationFilter
      |
      v
JwtService valida token
      |
      v
SecurityContext
      |
      v
Controller protegido
```

## Banco de Dados

O banco e evoluido por Flyway:

```text
V1__init_extensions.sql
V2__schema_seguranca.sql
V3__seed_seguranca_inicial.sql
```

## Objetivo Arquitetural

Criar uma aplicacao organizada, escalavel e de facil manutencao, separando responsabilidades por modulos de negocio e mantendo uma base segura para crescimento do ERP.

