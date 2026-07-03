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
- `V23__schema_cliente.sql`
- `V24__permissoes_cliente.sql`
- `V25__schema_fornecedor.sql`
- `V26__permissoes_fornecedor.sql`
- `V27__schema_funcionario.sql`
- `V28__permissoes_funcionario.sql`
- `V29__schema_transportadora.sql`
- `V30__permissoes_transportadora.sql`
- `V31__pessoa_role.sql`
- `V32__schema_contato_pessoa.sql`
- `V33__permissoes_contato_pessoa.sql`
- `V34__schema_documento_pessoa.sql`
- `V35__permissoes_documento_pessoa.sql`

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

Essa entidade usa o endpoint `/api/v1/pessoas`, paginacao, permissoes `PESSOA:*`, `PESSOAS:*` e CRUD granular, CPF/CNPJ unico entre registros ativos, CPF/CNPJ imutavel apos a criacao, validacao por tipo `FISICA`/`JURIDICA` e soft delete por padrao.

`Cliente` continua o modulo Pessoas com testes dedicados:

- `modules/pessoas/service/ClienteServiceTest.java`
- `modules/pessoas/controller/ClienteControllerTests.java`

Essa entidade usa o endpoint `/api/v1/clientes`, paginacao, permissoes `CLIENTE:*`, `PESSOAS:*` e CRUD granular. Cliente representa apenas o papel comercial de uma Pessoa, guarda limite de credito, observacao, referencias futuras para condicao de pagamento e tabela de preco, impede duplicidade de cliente ativo para a mesma pessoa e usa soft delete por padrao.

`Fornecedor` continua o modulo Pessoas com testes dedicados:

- `modules/pessoas/service/FornecedorServiceTest.java`
- `modules/pessoas/controller/FornecedorControllerTests.java`

Essa entidade usa o endpoint `/api/v1/fornecedores`, paginacao, permissoes `FORNECEDOR:*`, `PESSOAS:*` e CRUD granular. Fornecedor representa apenas o papel de fornecimento de uma Pessoa, guarda condicao de pagamento futura, prazo medio de entrega e observacao, impede duplicidade de fornecedor ativo para a mesma pessoa e usa soft delete por padrao.

`Funcionario` continua o modulo Pessoas com testes dedicados:

- `modules/pessoas/service/FuncionarioServiceTest.java`
- `modules/pessoas/controller/FuncionarioControllerTests.java`

Essa entidade usa o endpoint `/api/v1/funcionarios`, paginacao, permissoes `FUNCIONARIO:*`, `PESSOAS:*` e CRUD granular. Funcionario representa o papel trabalhista de uma Pessoa, guarda cargo, salario, admissao, demissao e observacao, permite vinculo opcional com Usuario, impede duplicidade de funcionario ativo para a mesma pessoa, impede usuario ativo vinculado a dois funcionarios e usa soft delete por padrao.

`Transportadora` continua o modulo Pessoas com testes dedicados:

- `modules/pessoas/service/TransportadoraServiceTest.java`
- `modules/pessoas/controller/TransportadoraControllerTests.java`

Essa entidade usa o endpoint `/api/v1/transportadoras`, paginacao, permissoes `TRANSPORTADORA:*`, `PESSOAS:*` e CRUD granular. Transportadora representa o papel logistico de uma Pessoa, guarda ANTT, tipo de veiculo principal, capacidade de carga em kg e observacao, impede duplicidade de transportadora ativa para a mesma pessoa e usa soft delete por padrao.

`PessoaRole` deixa explicito o aggregate root `Pessoa` e registra papeis ativos:

- `modules/pessoas/entity/PessoaRole.java`
- `modules/pessoas/service/PessoaRoleService.java`
- `modules/pessoas/repository/PessoaRoleRepository.java`

Esse registro auxiliar convive com as entidades especificas `Cliente`, `Fornecedor`, `Funcionario` e `Transportadora`, facilitando consultas futuras sobre quais papeis uma Pessoa exerce.

`ContatoPessoa` continua o modulo Pessoas com testes dedicados:

- `modules/pessoas/service/ContatoPessoaServiceTest.java`
- `modules/pessoas/controller/ContatoPessoaControllerTests.java`

Essa entidade usa o endpoint `/api/v1/contatos-pessoa`, paginacao, permissoes `CONTATO_PESSOA:*`, `PESSOAS:*` e CRUD granular. ContatoPessoa guarda contatos operacionais da Pessoa, como Financeiro, Compras, Comercial ou RH, sem alterar dados cadastrais do aggregate root. Usa soft delete, auditoria e validacao de nome obrigatorio, pessoa ativa e email valido.

`DocumentoPessoa` continua o modulo Pessoas com testes dedicados:

- `modules/pessoas/service/DocumentoPessoaServiceTest.java`
- `modules/pessoas/controller/DocumentoPessoaControllerTests.java`

Essa entidade usa o endpoint `/api/v1/documentos-pessoa`, paginacao, permissoes `DOCUMENTO_PESSOA:*`, `PESSOAS:*` e CRUD granular. DocumentoPessoa guarda documentos cadastrais da Pessoa, como RG, CPF, CNH, contrato social, alvara, licenca sanitaria, certificado digital e comprovante de endereco. Pode se vincular opcionalmente a `Arquivo`, impede duplicidade ativa por pessoa/tipo/numero, valida validade posterior ou igual a emissao e usa soft delete por padrao.

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
