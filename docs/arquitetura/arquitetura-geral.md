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
- repository, service, controller e mapper base para CRUDs;
- respostas padronizadas;
- excecoes compartilhadas;
- handler global de excecoes;
- value objects reutilizaveis;
- eventos de dominio.

`BaseEntity` centraliza `id`, datas de auditoria, usuario de criacao/alteracao e `version` para concorrencia otimista. A auditoria e preenchida automaticamente com Spring Data JPA Auditing e `AuditorAware<UUID>`. Entidades que herdam `SoftDeleteEntity` tambem usam `ativo` e `deleted_at`; as consultas padrao devem ocultar registros com exclusao logica.

`BaseRepository` herda `JpaRepository` e `JpaSpecificationExecutor`, garantindo suporte a Specifications em todos os cadastros que usam a base. Listagens REST devem retornar `Page<T>` e aceitar `page`, `size` e `sort`.

O tratamento de erros fica centralizado em `GlobalExceptionHandler`, retornando sempre `ErrorResponse`. Validacoes e operacoes invalidas retornam HTTP 400, recursos inexistentes retornam HTTP 404, duplicidades e conflitos de estado retornam HTTP 409, conflitos de concorrencia otimista retornam HTTP 409, falhas de autenticacao retornam HTTP 401 e ausencia de permissao retorna HTTP 403.

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

- cadastros, ja iniciado com Pais, Estado, Cidade, Endereco, Empresa, Filial, Categoria, Marca, UnidadeMedida e Arquivo;
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

O login tambem retorna um refresh token opaco. O backend salva apenas o hash SHA-256 desse refresh token na tabela `refresh_token`.

```text
POST /api/v1/auth/refresh
      |
      v
RefreshTokenService valida hash e expiracao
      |
      v
Novo access token e novo refresh token
```

```text
POST /api/v1/auth/logout
      |
      v
RefreshTokenService revoga o refresh token
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
UsuarioDetailsService recarrega usuario/permissoes
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

O modulo Cadastros adiciona tabelas base reutilizaveis por outros dominios e permissao granular `CADASTROS` para proteger os endpoints REST. A fase fundamental criou a hierarquia geografica `pais -> estado -> cidade`, vinculou `endereco` a `cidade` e adicionou `filial` vinculada a `empresa`.

A fase 2 ja concluiu `UnidadeMedida`, exposta em `/api/v1/unidades-medida`, `Categoria`, exposta em `/api/v1/categorias`, `Marca`, exposta em `/api/v1/marcas`, `Empresa`, exposta em `/api/v1/empresas`, e `Arquivo`, exposta em `/api/v1/arquivos`. Todas usam permissao granular, paginacao, cache, soft delete, auditoria e testes unitarios/de integracao. `Categoria` tambem possui hierarquia opcional, ordenacao por `ordem`, caminho completo, bloqueio de auto-relacionamento e ciclo. `Arquivo` usa storage multi-provedor, hash SHA-256 para integridade/deteccao de duplicidade e vinculo generico `entidade/entidadeId`.

O modulo Pessoas foi iniciado com `Pessoa`, exposta em `/api/v1/pessoas`. Ela e a base para Cliente, Fornecedor, Funcionario, Transportadora, ContatoPessoa e DocumentoPessoa. O cadastro usa `tipoPessoa` (`FISICA` ou `JURIDICA`), CPF/CNPJ unico entre registros ativos, soft delete, auditoria, paginacao e permissoes `PESSOA:*`/`PESSOAS:*`.

Indices simples, compostos e parciais sao usados nas principais consultas por registros ativos e relacionamentos, como `estado(pais_id, ativo)`, `cidade(estado_id, ativo)`, `filial(empresa_id, ativo)`, `categoria(LOWER(nome)) WHERE ativo = TRUE AND deleted_at IS NULL`, `categoria(categoria_pai_id, ordem, nome)`, `marca(LOWER(nome)) WHERE ativo = TRUE AND deleted_at IS NULL`, `empresa(cnpj) WHERE ativo = TRUE AND deleted_at IS NULL`, `arquivo(hash_sha256) WHERE ativo = TRUE AND deleted_at IS NULL` e `arquivo(entidade, entidade_id) WHERE ativo = TRUE AND deleted_at IS NULL`. Cache com Redis e filas com RabbitMQ permanecem provisionados no Docker, mas ainda entram como evolucao funcional futura.

O cache funcional inicial usa Spring Cache em cadastros de baixa alteracao, como Categoria, Marca, Arquivo, UnidadeMedida, Pais, Estado e Cidade. O Redis permanece como proximo passo para cache distribuido quando houver mais de uma instancia da API.

Logs seguem SLF4J + Logback por `logback-spring.xml`. Codigo de aplicacao nao deve usar `System.out.println()`.

## Ordem de Implementacao por Entidade

Cada nova entidade deve seguir a ordem abaixo:

1. Migration Flyway
2. Entity
3. Repository
4. DTO Request
5. DTO Response
6. Mapper
7. Specification
8. Validator
9. Service
10. Controller
11. Testes
12. Documentacao

## Objetivo Arquitetural

Criar uma aplicacao organizada, escalavel e de facil manutencao, separando responsabilidades por modulos de negocio e mantendo uma base segura para crescimento do ERP.
