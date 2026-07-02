# Estrutura do Projeto - invPlus ERP

## Estrutura Geral

```text
invPlus/
├── backend/
├── frontend/
├── database/
├── docker/
├── docs/
├── scripts/
├── README.md
└── setup-invplus.ps1
```

## Backend

```text
backend/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/br/com/invplus/
    │   │   ├── BackendApplication.java
    │   │   ├── config/
    │   │   ├── common/
    │   │   │   ├── constants/
    │   │   │   └── enums/
    │   │   ├── shared/
    │   │   │   ├── api/
    │   │   │   └── domain/
    │   │   ├── security/
    │   │   │   ├── config/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   ├── entity/
    │   │   │   ├── enums/
    │   │   │   ├── exception/
    │   │   │   ├── filter/
    │   │   │   ├── mapper/
    │   │   │   ├── repository/
    │   │   │   ├── service/
    │   │   │   └── validator/
    │   │   └── modules/
    │   │       ├── cadastros/
    │   │       ├── pessoas/
    │   │       ├── produtos/
    │   │       ├── estoque/
    │   │       ├── compras/
    │   │       ├── vendas/
    │   │       ├── pagamentos/
    │   │       ├── faturamento/
    │   │       ├── financeiro/
    │   │       ├── fiscal/
    │   │       └── crm/
    │   └── resources/
    │       ├── application.yml
    │       └── META-INF/
    └── test/
        ├── java/
        └── resources/
```

## Modulo Security

## Modulos de Negocio

A pasta `modules` esta padronizada para todos os dominios principais do ERP:

```text
modules/
|-- cadastros/
|-- pessoas/
|-- produtos/
|-- estoque/
|-- compras/
|-- vendas/
|-- pagamentos/
|-- faturamento/
|-- financeiro/
|-- fiscal/
`-- crm/
```

Cada modulo segue o mesmo contrato interno:

```text
controller/       # Endpoints REST
dto/request/      # Payloads de entrada
dto/response/     # Payloads de saida
entity/           # Entidades JPA
mapper/           # Conversoes entre entity/dto
repository/       # Repositories Spring Data
service/          # Regras de negocio
specification/    # Filtros dinamicos e consultas compostas
validator/        # Validacoes especificas do modulo
exception/        # Excecoes especificas do modulo
```

O modulo `security` ja possui a primeira implementacao funcional:

Principais arquivos atuais:

- `config/SecurityConfig.java`
- `controller/AuthController.java`
- `dto/LoginRequest.java`
- `dto/LoginResponse.java`
- `dto/RefreshTokenRequest.java`
- `dto/LogoutRequest.java`
- `dto/LogoutResponse.java`
- `entity/Usuario.java`
- `entity/Perfil.java`
- `entity/Permissao.java`
- `entity/Auditoria.java`
- `entity/RefreshToken.java`
- `filter/JwtAuthenticationFilter.java`
- `repository/UsuarioRepository.java`
- `repository/RefreshTokenRepository.java`
- `service/SecurityService.java`
- `service/JwtService.java`
- `service/UsuarioDetailsService.java`
- `service/RefreshTokenService.java`

O modulo `cadastros` ja possui a primeira fatia funcional:

- `entity/Pais.java`
- `entity/Estado.java`
- `entity/Cidade.java`
- `entity/Empresa.java`
- `entity/Filial.java`
- `entity/Endereco.java`
- `entity/Categoria.java`
- `entity/Marca.java`
- `entity/UnidadeMedida.java`
- `entity/Arquivo.java`
- `repository/*Repository.java`
- `dto/request/*Request.java`
- `dto/response/*Response.java`
- `mapper/*Mapper.java`
- `specification/*Specification.java`
- `validator/*Validator.java`
- `service/*Service.java`
- `controller/*Controller.java`

Esses arquivos seguem o contrato padrao do modulo: migration, entidade JPA, repository, DTOs, mapper, specification, validator, service, controller REST, testes e documentacao. As classes e metodos receberam comentarios curtos para facilitar leitura e manutencao.

```text
security/
├── config/
│   └── SecurityConfig.java
├── controller/
│   └── AuthController.java
├── dto/
│   ├── LoginRequest.java
│   └── LoginResponse.java
├── entity/
│   ├── Usuario.java
│   ├── Perfil.java
│   ├── Permissao.java
│   └── Auditoria.java
├── enums/
│   ├── AcaoAuditoria.java
│   └── AcaoPermissao.java
├── filter/
│   └── JwtAuthenticationFilter.java
├── repository/
│   ├── UsuarioRepository.java
│   ├── PerfilRepository.java
│   ├── PermissaoRepository.java
│   └── AuditoriaRepository.java
└── service/
    ├── SecurityService.java
    ├── UsuarioDetailsService.java
    └── JwtService.java
```

## Shared e Common

```text
common/
├── constants/
│   └── AppConstants.java
└── enums/
    ├── StatusRegistro.java
    └── TipoPessoa.java

shared/
├── api/
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   └── response/
│       ├── ApiResponse.java
│       └── ErrorResponse.java
└── domain/
    ├── entity/
    │   ├── BaseEntity.java
    │   └── SoftDeleteEntity.java
    └── exception/
        ├── BusinessException.java
        └── ResourceNotFoundException.java
```

`BaseEntity` concentra auditoria, UUID e `version` para concorrencia otimista. `SoftDeleteEntity` adiciona `ativo` e `deleted_at`; por padrao, repositories/services devem consultar apenas registros ativos e nao excluidos.

Bases compartilhadas adicionadas:

- `shared/domain/repository/BaseRepository.java`
- `shared/domain/service/BaseCrudService.java`
- `shared/domain/mapper/CrudMapper.java`
- `shared/api/controller/BaseCrudController.java`
- `shared/config/JpaAuditingConfig.java`
- `shared/events/`
- `shared/domain/valueobject/`

Listagens REST dos cadastros retornam `Page<T>` e aceitam `page`, `size` e `sort`.

## Database

```text
database/
└── migrations/
    └── database/
        ├── V1__init_extensions.sql
        ├── V2__schema_seguranca.sql
        └── V3__seed_seguranca_inicial.sql
```

Migrations atuais ja implementadas:

- `V1__init_extensions.sql`
- `V2__schema_seguranca.sql`
- `V3__seed_seguranca_inicial.sql`
- `V4__refresh_token.sql`
- `V5__auditoria_refresh_token.sql`
- `V6__refresh_token_ip_varchar.sql`
- `V7__schema_cadastros.sql`
- `V8__endereco_uf_varchar.sql`
- `V9__cadastros_fundamentais.sql`
- `V10__cadastros_fase_2.sql`
- `V11__permissoes_cadastros_fase_2.sql`
- `V12__categoria_hierarquia_nome_unico.sql`
- `V13__permissao_categoria_wildcard.sql`

## Docker

```text
docker/
└── compose/
    ├── docker-compose.yml
    └── .env.example
```

Testes atuais tambem incluem:

- `modules/cadastros/controller/CadastrosControllerTests.java`
- `modules/cadastros/repository/CadastrosRepositoryTests.java`

O fluxo de controller de Cadastros cobre criacao de empresa, soft delete por padrao e a hierarquia Pais -> Estado -> Cidade -> Endereco -> Filial.

`UnidadeMedida` tambem possui testes dedicados:

- `modules/cadastros/service/UnidadeMedidaServiceTest.java`
- `modules/cadastros/controller/UnidadeMedidaControllerTests.java`

Essa entidade usa o endpoint `/api/v1/unidades-medida`, paginacao, cache, permissao granular `UNIDADE_MEDIDA` e soft delete por padrao.

`Categoria` tambem possui testes dedicados:

- `modules/cadastros/service/CategoriaServiceTest.java`
- `modules/cadastros/controller/CategoriaControllerTests.java`

Essa entidade usa o endpoint `/api/v1/categorias`, paginacao, cache, permissao granular `CATEGORIA`, permissao wildcard `CATEGORIA:*`, categoria pai opcional, bloqueio de auto-relacionamento, bloqueio de ciclo hierarquico e soft delete por padrao.

## Testes

```text
backend/src/test/
├── java/br/com/invplus/
│   ├── BackendApplicationTests.java
│   └── security/
│       ├── controller/
│       │   └── AuthControllerTests.java
│       └── repository/
│           └── SecuritySeedRepositoryTests.java
└── resources/
    └── application-test.yml
```
