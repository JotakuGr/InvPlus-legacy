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
- `V36__schema_produto.sql`
- `V37__permissoes_produto.sql`
- `V38__schema_produto_fornecedor.sql`
- `V39__permissoes_produto_fornecedor.sql`
- `V40__produto_fornecedor_melhorias.sql`
- `V41__permissoes_produto_fornecedor_preco.sql`
- `V42__schema_produto_fiscal.sql`
- `V43__permissoes_produto_fiscal.sql`
- `V44__produto_tipo_natureza.sql`
- `V45__produto_status_controles.sql`
- `V46__arquivo_cloudflare_r2.sql`
- `V47__schema_produto_imagem.sql`
- `V48__permissoes_produto_imagem.sql`
- `V49__schema_produto_codigo_barras.sql`
- `V50__permissoes_produto_codigo_barras.sql`
- `V51__schema_produto_preco.sql`
- `V52__permissoes_produto_preco.sql`
- `V53__schema_produto_unidade.sql`
- `V54__permissoes_produto_unidade.sql`
- `V55__produto_unidade_fracionamento.sql`

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

Essa entidade usa o endpoint `/api/v1/arquivos`, paginacao, cache, permissao granular `ARQUIVO`, permissao wildcard `ARQUIVO:*`, storage multi-provedor (`LOCAL`, `S3`, `MINIO`, `AZURE`, `GOOGLE_CLOUD`, `CLOUDFLARE_R2`), hash SHA-256 unico entre registros ativos, vinculo generico `entidade/entidadeId` e soft delete por padrao. O endpoint `POST /api/v1/arquivos/upload` recebe multipart, valida tipo/tamanho, grava o binario pelo `shared/storage`, salva metadados e retorna `ArquivoResponse`.

`shared/storage` centraliza a infraestrutura de arquivos:

- `StorageProvider.java`
- `StorageProperties.java`
- `StoredFile.java`
- `StorageService.java`
- `LocalStorageService.java`
- `StorageFacade.java`
- `StorageWebConfig.java`

O provider LOCAL grava em disco, calcula SHA-256, gera nome unico e monta URL publica. Cloudflare R2 ja esta previsto no enum e na constraint de banco, mas a implementacao fisica sera adicionada quando as credenciais e bucket forem definidos.

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

`Produto` inicia o modulo Produtos com testes dedicados:

- `modules/produtos/service/ProdutoServiceTest.java`
- `modules/produtos/controller/ProdutoControllerTests.java`

Essa entidade usa o endpoint `/api/v1/produtos`, paginacao, permissoes `PRODUTO:*` e CRUD granular. Produto referencia Categoria, UnidadeMedida e, opcionalmente, Marca. Guarda codigo, SKU, codigo interno, codigo de barras, nome, descricao, tipo, natureza, status, precos, pesos, dimensoes, estoque minimo e controles de lote, validade, fracionamento, estoque, compra e venda. Impede duplicidade ativa de codigo, SKU e codigo de barras, valida valores nao negativos, valida codigo de barras de forma flexivel para GTIN com 8, 12, 13 ou 14 digitos numericos e usa soft delete por padrao.

Filtros avancados foram centralizados em `modules/produtos/specification/ProdutoSpecification.java`. A listagem aceita filtros por nome, codigo, SKU, codigo de barras, categoria, marca, tipo, natureza, status, controle de estoque, permissao de compra, permissao de venda e ativo. O endpoint `GET /api/v1/produtos/search?termo=...` faz busca rapida em nome, codigo, SKU e codigo de barras para fluxos como PDV, vendas e compras.

`ProdutoFornecedor` vincula Produto e Fornecedor para compras, cotacoes, entrada de mercadoria e historico de custo:

- `modules/produtos/service/ProdutoFornecedorServiceTest.java`
- `modules/produtos/controller/ProdutoFornecedorControllerTests.java`

Essa entidade usa o endpoint `/api/v1/produtos-fornecedores`, paginacao, permissoes `PRODUTO_FORNECEDOR:*`, `PRODUTO:*` e CRUD granular. O vinculo guarda codigo do fornecedor, lead time, prioridade, status, indicador de fornecedor principal e observacao. Impede duplicidade ativa de produto + fornecedor, garante apenas um fornecedor principal ativo por produto, valida lead time/prioridade nao negativos e usa soft delete por padrao. Os filtros foram centralizados em `modules/produtos/specification/ProdutoFornecedorSpecification.java` por produto, fornecedor, principal, ativo, status e codigo do fornecedor.

`ProdutoFornecedorPreco` registra historico de custos por vigencia:

- `modules/produtos/service/ProdutoFornecedorPrecoServiceTest.java`

Essa entidade usa o endpoint `/api/v1/produtos-fornecedores-precos`, paginacao, permissoes `PRODUTO_FORNECEDOR_PRECO:*`, `PRODUTO:*` e CRUD granular. Guarda preco, vigencia inicial e vigencia final, impede duplicidade ativa por vinculo + vigencia inicial, valida preco nao negativo, valida vigencia final posterior ou igual a inicial e usa soft delete por padrao.

`ProdutoFiscal` separa dados tributarios do cadastro comercial do produto:

- `modules/produtos/service/ProdutoFiscalServiceTest.java`
- `modules/produtos/controller/ProdutoFiscalControllerTests.java`

Essa entidade usa o endpoint `/api/v1/produtos-fiscais`, paginacao, permissoes `PRODUTO_FISCAL:*`, `PRODUTO:*` e CRUD granular. Guarda NCM, CEST, origem da mercadoria, CFOP padrao, CST/CSOSN, CST PIS/COFINS, aliquotas de ICMS/IPI/PIS/COFINS e observacao. Impede mais de uma ficha fiscal ativa por produto, valida NCM com 8 digitos, CEST com 7 digitos quando informado, aliquotas nao negativas, filtros por produto/NCM/CEST/origem/ativo e usa soft delete por padrao.

`ProdutoImagem` vincula Produto e Arquivo para imagens do catalogo:

- `modules/produtos/service/ProdutoImagemServiceTest.java`
- `modules/produtos/controller/ProdutoImagemControllerTests.java`
- `modules/produtos/integration/ProdutoImagemBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/produtos-imagens`, paginacao, permissoes `PRODUTO_IMAGEM:*`, `PRODUTO:*` e CRUD granular. O vinculo guarda `principal`, `ordem` e `descricao`, impede duplicidade ativa de produto + arquivo, garante apenas uma imagem principal ativa por produto, valida ordem nao negativa, filtra por produto, arquivo, principal e ativo, e usa soft delete por padrao. O fluxo integrado usa `POST /api/v1/arquivos/upload` antes de criar o vinculo.

`ProdutoCodigoBarras` permite multiplos codigos por Produto:

- `modules/produtos/service/ProdutoCodigoBarrasServiceTest.java`
- `modules/produtos/controller/ProdutoCodigoBarrasControllerTests.java`
- `modules/produtos/integration/ProdutoCodigoBarrasBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/produtos-codigos-barras`, paginacao, permissoes `PRODUTO_CODIGO_BARRAS:*`, `PRODUTO:*` e CRUD granular. O vinculo guarda tipo do codigo, codigo, indicador de principal e observacao. Suporta EAN de unidade, EAN de caixa, EAN de pacote, GTIN logistico e codigo interno alternativo, impede duplicidade ativa de codigo, garante apenas um codigo principal ativo por produto, filtra por produto, tipo, codigo, principal e ativo, e usa soft delete por padrao.

`ProdutoPreco` registra historico de precos comerciais por tipo, vigencia e empresa opcional:

- `modules/produtos/service/ProdutoPrecoServiceTest.java`
- `modules/produtos/controller/ProdutoPrecoControllerTests.java`
- `modules/produtos/integration/ProdutoPrecoBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/produtos-precos`, paginacao, permissoes `PRODUTO_PRECO:*`, `PRODUTO:*` e CRUD granular. O registro guarda `tipoPreco`, `preco`, vigencia inicial/final, indicador de principal, observacao e `empresaId` opcional para multiempresa. Suporta precos `CUSTO`, `VENDA`, `ATACADO`, `VAREJO`, `PROMOCIONAL` e `ESPECIAL`, impede duplicidade ativa por produto/tipo/empresa/vigencia inicial, garante apenas um preco principal ativo por produto/tipo/empresa, filtra por produto, empresa, tipo, principal, vigencia e ativo, possui `GET /api/v1/produtos-precos/vigente` para buscar preco vigente por produto/tipo/data, e usa soft delete por padrao.

`ProdutoUnidade` registra unidades alternativas e fatores de conversao do produto:

- `modules/produtos/service/ProdutoUnidadeServiceTest.java`
- `modules/produtos/controller/ProdutoUnidadeControllerTests.java`
- `modules/produtos/integration/ProdutoUnidadeBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/produtos-unidades`, paginacao, permissoes `PRODUTO_UNIDADE:*`, `PRODUTO:*` e CRUD granular. O registro guarda unidade de medida, fator de conversao, unidade padrao, flags de compra, venda, estoque, fracionamento e observacao. Impede duplicidade ativa de produto + unidade, garante apenas uma unidade padrao ativa por produto, exige fator de conversao igual a 1 para unidade padrao, filtra por produto, unidade, unidade padrao, compra, venda, estoque, fracionamento e ativo, e usa soft delete por padrao.

`ProdutoAggregateService` monta uma leitura completa de Produto para telas e processos que precisam de contexto consolidado:

- `modules/produtos/service/ProdutoAggregateService.java`
- `modules/produtos/dto/response/ProdutoCompletoResponse.java`

O endpoint `GET /api/v1/produtos/{id}/completo` retorna Produto, fornecedores, precos, imagens, codigos de barras, ficha fiscal, unidade padrao e demais unidades ativas em uma unica chamada. Essa camada e somente de leitura e evita que o frontend precise chamar varios endpoints para montar a mesma tela.

`ProdutoBusinessFlowIntegrationTest` cobre o fluxo completo entre modulos:

- `modules/produtos/integration/ProdutoBusinessFlowIntegrationTest.java`

Esse teste cria Pessoa Juridica, transforma em Fornecedor, cria Categoria, Marca e UnidadeMedida, cadastra Produto, vincula ProdutoFornecedor, registra ProdutoFornecedorPreco, cria ProdutoFiscal e valida os principais vinculos, filtros, buscas e leitura agregada do produto completo. Ele funciona como prova de integracao entre Cadastros, Pessoas, Produtos e fiscal basico.

Backlog fiscal registrado: historico por vigencia, tributacao por UF, CFOP por tipo de operacao, validacao CST/CSOSN por CRT da Empresa, validacoes fiscais inteligentes, cache seletivo de ProdutoFiscal, busca fiscal ampliada e auditoria fiscal apos o modulo Fiscal. Backlog de catalogo registrado: `TipoControleEstoque`, `marcaPropria`, `ProdutoClassificacaoABC`, kits com `ProdutoKit`/`ProdutoKitItem` e migracao futura do preco legado de Produto para consumo integral de ProdutoPreco.

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

`shared/infrastructure/MigrationIdempotencyTest.java` valida a politica de seeds tecnicos em migrations. Qualquer arquivo SQL com `INSERT INTO` precisa conter `ON CONFLICT` ou `WHERE NOT EXISTS`, evitando duplicidade de permissoes, perfis, usuarios tecnicos e vinculos de seguranca.
