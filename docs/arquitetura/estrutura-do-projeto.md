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

O tratamento global de erros usa `GlobalExceptionHandler` e `ErrorResponse`. Validacoes e operacoes invalidas retornam `400`, duplicidades usam `ConflictException` com `409`, concorrencia otimista tambem retorna `409`, recursos nao encontrados retornam `404`, falhas de autenticacao retornam `401` e falta de permissao retorna `403`.

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
- `V14__categoria_ordem_marca_campos.sql`
- `V15__permissao_marca_wildcard.sql`
- `V16__empresa_melhorias.sql`
- `V17__permissao_empresa_wildcard.sql`
- `V18__arquivo_melhorias.sql`
- `V19__permissao_arquivo_wildcard.sql`
- `V20__arquivo_colunas_legadas_opcionais.sql`
- `V21__schema_pessoa.sql`
- `V22__permissoes_pessoa.sql`

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

Essa entidade usa o endpoint `/api/v1/categorias`, paginacao, cache, permissao granular `CATEGORIA`, permissao wildcard `CATEGORIA:*`, categoria pai opcional, ordenacao por `ordem`, caminho completo, bloqueio de auto-relacionamento, bloqueio de ciclo hierarquico e soft delete por padrao.

`Marca` tambem possui testes dedicados:

- `modules/cadastros/service/MarcaServiceTest.java`
- `modules/cadastros/controller/MarcaControllerTests.java`

Essa entidade usa o endpoint `/api/v1/marcas`, paginacao, cache, permissao granular `MARCA`, permissao wildcard `MARCA:*`, campos opcionais `site` e `fabricante`, nome unico entre registros ativos e soft delete por padrao.

`Empresa` tambem possui testes dedicados:

- `modules/cadastros/service/EmpresaServiceTest.java`
- `modules/cadastros/controller/EmpresaControllerTests.java`

Essa entidade usa o endpoint `/api/v1/empresas`, paginacao, cache, permissao granular `EMPRESA`, permissao wildcard `EMPRESA:*`, CNPJ unico entre registros ativos, validacao simples de CNPJ com 14 digitos, validacao de CRT e soft delete por padrao.

`Arquivo` tambem possui testes dedicados:

- `modules/cadastros/service/ArquivoServiceTest.java`
- `modules/cadastros/controller/ArquivoControllerTests.java`

Essa entidade usa o endpoint `/api/v1/arquivos`, paginacao, cache, permissao granular `ARQUIVO`, permissao wildcard `ARQUIVO:*`, storage multi-provedor (`LOCAL`, `S3`, `MINIO`, `AZURE`, `GOOGLE_CLOUD`), hash SHA-256 unico entre registros ativos, vinculo generico `entidade/entidadeId` e soft delete por padrao.

`Pessoa` inicia o modulo Pessoas com testes dedicados:

- `modules/pessoas/service/PessoaServiceTest.java`
- `modules/pessoas/controller/PessoaControllerTests.java`

Essa entidade usa o endpoint `/api/v1/pessoas`, paginacao, permissoes `PESSOA:*`, `PESSOAS:*` e CRUD granular, CPF/CNPJ unico entre registros ativos, validacao por tipo `FISICA`/`JURIDICA` e soft delete por padrao.

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
