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
- `V56__schema_deposito.sql`
- `V57__permissoes_deposito.sql`
- `V58__deposito_controles_operacionais.sql`
- `V59__schema_localizacao_estoque.sql`
- `V60__permissoes_localizacao_estoque.sql`
- `V61__schema_estoque.sql`
- `V62__permissoes_estoque.sql`
- `V63__schema_movimentacao_estoque.sql`
- `V64__permissoes_movimentacao_estoque.sql`
- `V65__schema_reserva_estoque.sql`
- `V66__permissoes_reserva_estoque.sql`
- `V67__schema_pedido_compra.sql`
- `V68__permissoes_pedido_compra.sql`
- `V69__schema_pedido_compra_item.sql`
- `V70__permissoes_pedido_compra_item.sql`
- `V71__schema_entrada_mercadoria.sql`
- `V72__permissoes_entrada_mercadoria.sql`
- `V73__schema_entrada_mercadoria_item.sql`
- `V74__permissoes_entrada_mercadoria_item.sql`
- `V75__compras_consolidacao.sql`
- `V76__schema_pedido_venda.sql`
- `V77__permissoes_pedido_venda.sql`
- `V78__schema_pedido_venda_item.sql`
- `V79__permissoes_pedido_venda_item.sql`
- `V80__ajuste_reserva_estoque_origem_unica.sql`
- `V81__schema_venda_faturamento.sql`
- `V82__permissoes_venda_faturamento.sql`
- `V83__schema_forma_pagamento.sql`
- `V84__permissoes_forma_pagamento.sql`
- `V85__schema_condicao_pagamento.sql`
- `V86__permissoes_condicao_pagamento.sql`
- `V87__condicao_pagamento_parcelas.sql`
- `V88__schema_conta_receber.sql`
- `V89__permissoes_conta_receber.sql`
- `V90__schema_parcela_receber.sql`
- `V91__permissoes_parcela_receber.sql`
- `V92__schema_recebimento.sql`
- `V93__permissoes_recebimento.sql`

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
- `businessflow/ProdutoImagemBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/produtos-imagens`, paginacao, permissoes `PRODUTO_IMAGEM:*`, `PRODUTO:*` e CRUD granular. O vinculo guarda `principal`, `ordem` e `descricao`, impede duplicidade ativa de produto + arquivo, garante apenas uma imagem principal ativa por produto, valida ordem nao negativa, filtra por produto, arquivo, principal e ativo, e usa soft delete por padrao. O fluxo integrado usa `POST /api/v1/arquivos/upload` antes de criar o vinculo.

`ProdutoCodigoBarras` permite multiplos codigos por Produto:

- `modules/produtos/service/ProdutoCodigoBarrasServiceTest.java`
- `modules/produtos/controller/ProdutoCodigoBarrasControllerTests.java`
- `businessflow/ProdutoCodigoBarrasBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/produtos-codigos-barras`, paginacao, permissoes `PRODUTO_CODIGO_BARRAS:*`, `PRODUTO:*` e CRUD granular. O vinculo guarda tipo do codigo, codigo, indicador de principal e observacao. Suporta EAN de unidade, EAN de caixa, EAN de pacote, GTIN logistico e codigo interno alternativo, impede duplicidade ativa de codigo, garante apenas um codigo principal ativo por produto, filtra por produto, tipo, codigo, principal e ativo, e usa soft delete por padrao.

`ProdutoPreco` registra historico de precos comerciais por tipo, vigencia e empresa opcional:

- `modules/produtos/service/ProdutoPrecoServiceTest.java`
- `modules/produtos/controller/ProdutoPrecoControllerTests.java`
- `businessflow/ProdutoPrecoBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/produtos-precos`, paginacao, permissoes `PRODUTO_PRECO:*`, `PRODUTO:*` e CRUD granular. O registro guarda `tipoPreco`, `preco`, vigencia inicial/final, indicador de principal, observacao e `empresaId` opcional para multiempresa. Suporta precos `CUSTO`, `VENDA`, `ATACADO`, `VAREJO`, `PROMOCIONAL` e `ESPECIAL`, impede duplicidade ativa por produto/tipo/empresa/vigencia inicial, garante apenas um preco principal ativo por produto/tipo/empresa, filtra por produto, empresa, tipo, principal, vigencia e ativo, possui `GET /api/v1/produtos-precos/vigente` para buscar preco vigente por produto/tipo/data, e usa soft delete por padrao.

`ProdutoUnidade` registra unidades alternativas e fatores de conversao do produto:

- `modules/produtos/service/ProdutoUnidadeServiceTest.java`
- `modules/produtos/controller/ProdutoUnidadeControllerTests.java`
- `businessflow/ProdutoUnidadeBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/produtos-unidades`, paginacao, permissoes `PRODUTO_UNIDADE:*`, `PRODUTO:*` e CRUD granular. O registro guarda unidade de medida, fator de conversao, unidade padrao, flags de compra, venda, estoque, fracionamento e observacao. Impede duplicidade ativa de produto + unidade, garante apenas uma unidade padrao ativa por produto, exige fator de conversao igual a 1 para unidade padrao, filtra por produto, unidade, unidade padrao, compra, venda, estoque, fracionamento e ativo, e usa soft delete por padrao.

`ProdutoAggregateService` monta uma leitura completa de Produto para telas e processos que precisam de contexto consolidado:

- `modules/produtos/service/ProdutoAggregateService.java`
- `modules/produtos/dto/response/ProdutoCompletoResponse.java`

O endpoint `GET /api/v1/produtos/{id}/completo` retorna Produto, fornecedores, precos, imagens, codigos de barras, ficha fiscal, unidade padrao e demais unidades ativas em uma unica chamada. Essa camada e somente de leitura e evita que o frontend precise chamar varios endpoints para montar a mesma tela.

`ProdutoBusinessFlowIntegrationTest` cobre o fluxo completo entre modulos:

- `businessflow/ProdutoBusinessFlowIntegrationTest.java`

Esse teste cria Pessoa Juridica, transforma em Fornecedor, cria Categoria, Marca e UnidadeMedida, cadastra Produto, vincula ProdutoFornecedor, registra ProdutoFornecedorPreco, cria ProdutoFiscal e valida os principais vinculos, filtros, buscas e leitura agregada do produto completo. Ele funciona como prova de integracao entre Cadastros, Pessoas, Produtos e fiscal basico.

Backlog fiscal registrado: historico por vigencia, tributacao por UF, CFOP por tipo de operacao, validacao CST/CSOSN por CRT da Empresa, validacoes fiscais inteligentes, cache seletivo de ProdutoFiscal, busca fiscal ampliada e auditoria fiscal apos o modulo Fiscal. Backlog de catalogo registrado: `TipoControleEstoque`, `marcaPropria`, `ProdutoClassificacaoABC`, kits com `ProdutoKit`/`ProdutoKitItem` e migracao futura do preco legado de Produto para consumo integral de ProdutoPreco.

`Deposito` inicia o modulo Estoque com o local onde produtos serao armazenados:

- `modules/estoque/service/DepositoServiceTest.java`
- `modules/estoque/controller/DepositoControllerTests.java`
- `businessflow/DepositoBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/depositos`, paginacao, permissoes `DEPOSITO:*`, `ESTOQUE:*` e CRUD granular. O registro guarda empresa obrigatoria, filial opcional, codigo, nome, descricao, tipo do deposito, indicador de principal, flags de venda, compra e transferencia, controle de estoque e bloqueio operacional. Impede codigo duplicado por empresa entre registros ativos, garante apenas um deposito principal ativo por empresa, valida filial ativa e pertencente a empresa informada, filtra por empresa, filial, codigo, nome, tipo, principal, controle de estoque, bloqueado e ativo, e usa soft delete por padrao.

`LocalizacaoEstoque` representa a posicao fisica ou logica dentro de um deposito:

- `modules/estoque/service/LocalizacaoEstoqueServiceTest.java`
- `modules/estoque/controller/LocalizacaoEstoqueControllerTests.java`
- `businessflow/LocalizacaoEstoqueBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/localizacoes-estoque`, paginacao, permissoes `LOCALIZACAO_ESTOQUE:*`, `ESTOQUE:*` e CRUD granular. O registro guarda deposito obrigatorio, codigo, descricao, rua, corredor, prateleira, nivel, posicao, indicador de principal e bloqueio operacional. Impede codigo duplicado por deposito entre registros ativos, garante apenas uma localizacao principal ativa por deposito, filtra por deposito, codigo, descricao, rua, corredor, prateleira, principal, bloqueada e ativo, e usa soft delete por padrao. A numeracao ficou em `V59`/`V60` porque `V58` ja estava ocupada pelos controles operacionais de Deposito.

`Estoque` representa o saldo fisico atual de um produto em uma localizacao:

- `modules/estoque/service/EstoqueServiceTest.java`
- `modules/estoque/controller/EstoqueControllerTests.java`
- `businessflow/EstoqueBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/estoques`, o endpoint operacional `GET /api/v1/estoques/saldo`, paginacao, permissoes `ESTOQUE_SALDO:*`, `ESTOQUE:*` e CRUD granular. O saldo referencia Produto, Empresa, Deposito e LocalizacaoEstoque, com Filial opcional. A regra atual impede duplicidade ativa para produto + empresa + deposito + localizacao, valida que filial pertence a empresa, deposito pertence a empresa e localizacao pertence ao deposito. `quantidadeDisponivel` nao e digitada: e recalculada como `quantidade - quantidadeReservada`. A entidade tambem guarda quantidade avariada, quantidade em transito, estoque minimo, estoque maximo, `ultimoMovimentoEm` e `ultimoMovimentoId` preparado para a futura `MovimentacaoEstoque`.

Leituras consolidadas de Estoque:

- `modules/estoque/service/EstoqueAggregateService.java`
- `modules/estoque/service/EstoqueFacade.java`
- `modules/estoque/dto/response/EstoqueCompletoResponse.java`
- `modules/estoque/dto/response/EstoqueDisponivelResponse.java`
- `concurrency/EstoqueConcurrencyTest.java`

O endpoint `GET /api/v1/estoques/{id}/completo` retorna saldo, ultima movimentacao, reservas ativas e disponibilidade em uma unica resposta. O endpoint `GET /api/v1/estoques/disponivel` retorna saldo rapido por produto, empresa, deposito e localizacao. `EstoqueFacade` e a API interna que os proximos modulos devem usar para entrada, saida, transferencia, ajuste, inventario, reserva, cancelamento e baixa.

`MovimentacaoEstoque` representa o historico imutavel das alteracoes de saldo:

- `modules/estoque/service/MovimentacaoEstoqueServiceTest.java`
- `modules/estoque/controller/MovimentacaoEstoqueControllerTests.java`
- `shared/domain/service/EstoqueDomainServiceTest.java`

Essa entidade usa os endpoints somente-leitura `/api/v1/movimentacoes-estoque` e `/api/v1/movimentacoes-estoque/{id}`, paginacao, permissoes `MOVIMENTACAO_ESTOQUE:*`, `MOVIMENTACAO_ESTOQUE:VISUALIZAR` e `ESTOQUE:*`. O registro guarda estoque, produto, empresa, filial opcional, deposito, localizacao, tipo da movimentacao, quantidade, saldo anterior, saldo posterior, origem, documento, observacao e data da movimentacao. `origem` usa o enum `MovimentacaoOrigem`, mantendo filtros e relatorios consistentes. A entidade nao usa soft delete, nao expoe update/delete funcional e e criada internamente pelo `EstoqueDomainService`, que passa a ser o ponto unico para entrada, saida, ajuste, transferencia, reserva, cancelamento e baixa de reserva. Nenhum modulo futuro deve alterar saldo por repository diretamente.

`ReservaEstoque` representa quantidade separada sem baixa fisica imediata:

- `modules/estoque/service/ReservaEstoqueServiceTest.java`
- `modules/estoque/controller/ReservaEstoqueControllerTests.java`
- `businessflow/ReservaEstoqueBusinessFlowIntegrationTest.java`

Essa entidade usa os endpoints `/api/v1/reservas-estoque`, `/api/v1/reservas-estoque/{id}`, `/api/v1/reservas-estoque/{id}/cancelar` e `/api/v1/reservas-estoque/{id}/baixar`, paginacao, permissoes `RESERVA_ESTOQUE:*`, `RESERVA_ESTOQUE:CRIAR`, `RESERVA_ESTOQUE:VISUALIZAR`, `RESERVA_ESTOQUE:CANCELAR`, `RESERVA_ESTOQUE:BAIXAR` e `ESTOQUE:*`. A reserva fotografa estoque, produto, empresa, filial, deposito e localizacao, usa status `ATIVA`, `CANCELADA`, `BAIXADA` e `EXPIRADA`, impede origem/origemId ativa duplicada e nao possui `PUT` generico. Criacao, cancelamento e baixa chamam `EstoqueDomainService`, garantindo que reservado, disponivel, saldo fisico e movimentacoes permaneçam sincronizados.

Eventos preparados em `modules/estoque/events`: `EstoqueAlteradoEvent`, `ReservaCriadaEvent`, `ReservaCanceladaEvent` e `ReservaBaixadaEvent`. Eles documentam os contratos de dominio para futura invalidacao de cache, auditoria, mensageria e notificacoes.

`PedidoCompra` inicia o modulo Compras com a intencao de compra por empresa e fornecedor:

- `modules/compras/service/PedidoCompraService.java`
- `modules/compras/controller/PedidoCompraController.java`
- `businessflow/ComprasBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/pedidos-compra`, paginacao, permissoes `PEDIDO_COMPRA:*`, acoes granulares de criar, visualizar, editar, cancelar e aprovar. O pedido guarda empresa, filial opcional, fornecedor, numero, status, datas, observacao e valor total calculado pelos itens. A regra atual garante numero unico por empresa entre registros ativos, status inicial `ABERTO`, bloqueio de alteracao para pedidos `CANCELADO` ou `RECEBIDO`, aprovacao explicita e cancelamento por endpoint de estado.

`PedidoCompraItem`, exposto em `/api/v1/pedidos-compra-itens`, representa os produtos comprados dentro do pedido. O item referencia pedido, produto e unidade do produto, guarda quantidade, valor unitario, valor total calculado e observacao. O service bloqueia itens em pedidos cancelados ou recebidos, valida quantidade maior que zero e valor unitario nao negativo, e recalcula o valor total do pedido ao criar, atualizar ou excluir logicamente um item.

`EntradaMercadoria`, exposta em `/api/v1/entradas-mercadoria`, representa o recebimento fisico. Ela referencia empresa, filial opcional, fornecedor e pedido de compra opcional, usa status `ABERTA`, `CONFERIDA`, `EFETIVADA` e `CANCELADA`, e so movimenta estoque quando o endpoint `/efetivar` e chamado. A efetivacao busca os itens da entrada, chama `EstoqueFacade.entrada(...)`, registra `MovimentacaoEstoque` com origem `COMPRA`, atualiza o saldo existente e marca o pedido vinculado como `RECEBIDO`.

`EntradaMercadoriaItem`, exposto em `/api/v1/entradas-mercadoria-itens`, representa cada produto recebido. O item referencia entrada, item do pedido opcional, produto, unidade do produto, deposito e localizacao, valida que a localizacao pertence ao deposito, calcula valor total por quantidade e valor unitario, e so pode ser alterado enquanto a entrada permanece editavel.

`ComprasBusinessFlowIntegrationTest` cobre o fluxo real entre modulos: cria Empresa, Deposito, Localizacao, Categoria, UnidadeMedida, Produto, Estoque, Pessoa juridica, Fornecedor, PedidoCompra, PedidoCompraItem, EntradaMercadoria e EntradaMercadoriaItem; depois efetiva a entrada, valida aumento do saldo, disponibilidade, movimentacao de estoque com origem `COMPRA` e status final do pedido.

A Sprint de Consolidacao de Compras adicionou:

- `modules/compras/service/CompraAggregateService.java`
- `modules/compras/service/CompraFacade.java`
- `modules/compras/service/CompraHistoricoService.java`
- `modules/compras/dto/response/PedidoCompraCompletoResponse.java`
- `modules/compras/dto/response/EntradaMercadoriaCompletaResponse.java`
- `modules/compras/entity/PedidoCompraHistorico.java`
- `modules/compras/entity/EntradaMercadoriaHistorico.java`
- `modules/compras/events/PedidoCompraCriadoEvent.java`
- `modules/compras/events/PedidoCompraAprovadoEvent.java`
- `modules/compras/events/EntradaMercadoriaEfetivadaEvent.java`

Os endpoints `GET /api/v1/pedidos-compra/{id}/completo` e `GET /api/v1/entradas-mercadoria/{id}/completo` reduzem chamadas do frontend ao trazer pedido/entrada, itens, historico, entradas vinculadas e movimentacoes geradas. `CompraFacade` passa a ser a API interna recomendada para outros modulos chamarem criacao, aprovacao, cancelamento, registro e efetivacao de compras. A V75 tambem prepara aprovacao por nivel com `nivelAprovacao`, `aprovadoPor` e `aprovadoEm`.

`PedidoVenda` inicia o modulo Vendas com o cabecalho comercial por empresa e cliente:

- `modules/vendas/service/PedidoVendaServiceTest.java`
- `modules/vendas/controller/PedidoVendaControllerTests.java`
- `businessflow/PedidoVendaBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/pedidos-venda`, paginacao, permissoes `PEDIDO_VENDA:*` e acoes granulares de criar, visualizar, editar, aprovar e cancelar. O pedido referencia Empresa, Filial opcional, Cliente obrigatorio e Funcionario vendedor opcional, guarda numero, status, datas, valor total, desconto, acrescimo, observacao e campos de aprovacao. A regra atual garante numero unico por empresa entre registros ativos, status inicial `ABERTO`, valor total inicial zerado e bloqueio de alteracao para pedidos `CANCELADO` ou `FATURADO`.

`PedidoVendaItem` liga o pedido ao catalogo comercial:

- `modules/vendas/entity/PedidoVendaItem.java`
- `modules/vendas/repository/PedidoVendaItemRepository.java`
- `modules/vendas/dto/request/PedidoVendaItemRequest.java`
- `modules/vendas/dto/response/PedidoVendaItemResponse.java`
- `modules/vendas/mapper/PedidoVendaItemMapper.java`
- `modules/vendas/specification/PedidoVendaItemSpecification.java`
- `modules/vendas/validator/PedidoVendaItemValidator.java`
- `modules/vendas/service/PedidoVendaItemService.java`
- `modules/vendas/controller/PedidoVendaItemController.java`
- `modules/vendas/service/PedidoVendaItemServiceTest.java`
- `modules/vendas/controller/PedidoVendaItemControllerTests.java`
- `businessflow/PedidoVendaItemBusinessFlowIntegrationTest.java`

Essa entidade usa o endpoint `/api/v1/pedidos-venda-itens`, paginacao, permissoes `PEDIDO_VENDA_ITEM:*` e CRUD granular. O item referencia PedidoVenda, Produto, ProdutoPreco e ProdutoUnidade. O frontend informa quantidade, desconto e acrescimo, mas nao informa valor unitario: o backend usa `ProdutoPreco.preco`, calcula subtotal e valor total, valida vigencia do preco, valida que preco e unidade pertencem ao produto e recalcula `PedidoVenda.valorTotal` ao criar, atualizar ou excluir logicamente um item.

A aprovacao de PedidoVenda agora cria reservas automaticas de estoque usando `ReservaEstoqueService` e `EstoqueDomainService`. A origem da reserva e `MovimentacaoOrigem.VENDA`, com `origemId` igual ao id do pedido. Cancelar um pedido aprovado cancela as reservas ativas e restaura a disponibilidade do saldo. A migration `V80__ajuste_reserva_estoque_origem_unica.sql` ajusta o indice unico para permitir varias reservas do mesmo pedido em estoques diferentes, sem permitir duplicidade no mesmo estoque.

Leituras e contratos de Vendas:

- `modules/vendas/service/VendaAggregateService.java`
- `modules/vendas/service/VendaFacade.java`
- `modules/vendas/service/VendaHistoricoService.java`
- `modules/vendas/dto/response/PedidoVendaCompletoResponse.java`
- `modules/vendas/entity/PedidoVendaHistorico.java`
- `modules/vendas/events/PedidoVendaCriadoEvent.java`
- `modules/vendas/events/PedidoVendaReservadoEvent.java`
- `modules/vendas/events/PedidoVendaAprovadoEvent.java`
- `modules/vendas/events/PedidoVendaCanceladoEvent.java`
- `modules/vendas/events/PedidoVendaItemCriadoEvent.java`
- `modules/vendas/events/PedidoVendaItemAlteradoEvent.java`
- `modules/vendas/events/PedidoVendaItemRemovidoEvent.java`
- `modules/vendas/events/VendaFaturamentoCriadoEvent.java`
- `modules/vendas/events/PedidoVendaFaturadoEvent.java`

O endpoint `GET /api/v1/pedidos-venda/{id}/completo` retorna pedido, itens, faturamento, reservas e historico em uma unica chamada. `VendaFacade` passa a ser a API interna recomendada para outros modulos criarem, aprovarem, cancelarem, faturarem e manipularem itens de pedidos de venda sem conhecer services menores. `PedidoVendaBusinessFlowIntegrationTest` valida o fluxo real Empresa -> Cliente -> PedidoVenda, historico de status e optimistic lock. `PedidoVendaItemBusinessFlowIntegrationTest` valida Cliente -> PedidoVenda -> PedidoVendaItem -> ProdutoPreco -> ProdutoUnidade, totalizacao do pedido, leitura agregada e optimistic lock do item. `PedidoVendaReservaIntegrationTest` valida aprovacao com reserva automatica, cancelamento restaurando disponibilidade, movimentacao gerada e concorrencia retornando `409` sem reserva duplicada.

`VendaFaturamento` conclui a primeira versao do fluxo comercial de venda:

- `modules/vendas/entity/VendaFaturamento.java`
- `modules/vendas/repository/VendaFaturamentoRepository.java`
- `modules/vendas/dto/request/VendaFaturamentoRequest.java`
- `modules/vendas/dto/response/VendaFaturamentoResponse.java`
- `modules/vendas/mapper/VendaFaturamentoMapper.java`
- `modules/vendas/specification/VendaFaturamentoSpecification.java`
- `modules/vendas/validator/VendaFaturamentoValidator.java`
- `modules/vendas/service/VendaFaturamentoService.java`
- `modules/vendas/controller/VendaFaturamentoController.java`
- `modules/vendas/service/VendaFaturamentoServiceTest.java`
- `integration/vendas/VendaFaturamentoControllerTests.java`
- `businessflow/VendaFaturamentoBusinessFlowIntegrationTest.java`

O endpoint `POST /api/v1/vendas-faturamentos/faturar` exige um PedidoVenda aprovado, com reservas ativas de origem `VENDA`. O service cria o faturamento, baixa as reservas com `ReservaEstoqueService.baixar(...)`, reduz o saldo fisico, gera movimentacoes de estoque, grava historico e atualiza o PedidoVenda para `FATURADO`. As permissoes usam `VENDA_FATURAMENTO:*`, `VENDA_FATURAMENTO:CRIAR`, `VENDA_FATURAMENTO:VISUALIZAR`, `VENDA_FATURAMENTO:FATURAR` e `VENDA_FATURAMENTO:CANCELAR`.

`FormaPagamento` inicia o modulo Pagamentos com os meios de pagamento/recebimento:

- `modules/pagamentos/entity/FormaPagamento.java`
- `modules/pagamentos/enums/TipoFormaPagamento.java`
- `modules/pagamentos/repository/FormaPagamentoRepository.java`
- `modules/pagamentos/dto/request/FormaPagamentoRequest.java`
- `modules/pagamentos/dto/response/FormaPagamentoResponse.java`
- `modules/pagamentos/mapper/FormaPagamentoMapper.java`
- `modules/pagamentos/specification/FormaPagamentoSpecification.java`
- `modules/pagamentos/validator/FormaPagamentoValidator.java`
- `modules/pagamentos/service/FormaPagamentoService.java`
- `modules/pagamentos/controller/FormaPagamentoController.java`
- `modules/pagamentos/service/FormaPagamentoServiceTest.java`
- `integration/pagamentos/FormaPagamentoControllerTests.java`

Essa entidade usa `/api/v1/formas-pagamento`, paginacao, permissoes `FORMA_PAGAMENTO:*` e CRUD granular. A regra atual garante codigo unico entre registros ativos, troco apenas para `DINHEIRO` e parcelamento apenas para `CARTAO_CREDITO`, `BOLETO` e `CREDIARIO`.

`CondicaoPagamento` representa regras de vencimento e parcelamento:

- `modules/pagamentos/entity/CondicaoPagamento.java`
- `modules/pagamentos/entity/CondicaoPagamentoParcela.java`
- `modules/financeiro/enums/RegraVencimento.java`
- `modules/financeiro/service/CalculadoraVencimentoService.java`
- `modules/financeiro/service/GeradorParcelasService.java`
- `modules/financeiro/service/ParcelaGerada.java`
- `modules/pagamentos/repository/CondicaoPagamentoRepository.java`
- `modules/pagamentos/dto/request/CondicaoPagamentoRequest.java`
- `modules/pagamentos/dto/request/CondicaoPagamentoParcelaRequest.java`
- `modules/pagamentos/dto/request/SimulacaoCondicaoPagamentoRequest.java`
- `modules/pagamentos/dto/response/CondicaoPagamentoResponse.java`
- `modules/pagamentos/dto/response/CondicaoPagamentoParcelaResponse.java`
- `modules/pagamentos/mapper/CondicaoPagamentoMapper.java`
- `modules/pagamentos/specification/CondicaoPagamentoSpecification.java`
- `modules/pagamentos/validator/CondicaoPagamentoValidator.java`
- `modules/pagamentos/service/CondicaoPagamentoService.java`
- `modules/pagamentos/controller/CondicaoPagamentoController.java`
- `modules/pagamentos/service/CondicaoPagamentoServiceTest.java`
- `integration/pagamentos/CondicaoPagamentoControllerTests.java`
- `businessflow/PagamentosBusinessFlowIntegrationTest.java`

Essa entidade usa `/api/v1/condicoes-pagamento`, paginacao, permissoes `CONDICAO_PAGAMENTO:*` e CRUD granular. A regra atual garante codigo unico entre registros ativos, parcelas obrigatorias, numero de parcela unico por condicao, dias nao negativos, percentual maior que zero e soma total das parcelas fechando 100%. O endpoint `POST /api/v1/condicoes-pagamento/{id}/simular` calcula vencimentos e valores usando `RegraVencimento`, considerando finais de semana e deixando feriados preparados para evolucao futura.

`ContaReceber` inicia o modulo Financeiro com titulos a receber:

- `modules/financeiro/entity/ContaReceber.java`
- `modules/financeiro/enums/StatusContaReceber.java`
- `modules/financeiro/repository/ContaReceberRepository.java`
- `modules/financeiro/dto/request/ContaReceberRequest.java`
- `modules/financeiro/dto/response/ContaReceberResponse.java`
- `modules/financeiro/mapper/ContaReceberMapper.java`
- `modules/financeiro/specification/ContaReceberSpecification.java`
- `modules/financeiro/validator/ContaReceberValidator.java`
- `modules/financeiro/service/ContaReceberService.java`
- `modules/financeiro/controller/ContaReceberController.java`
- `modules/financeiro/service/ContaReceberServiceTest.java`
- `integration/financeiro/ContaReceberControllerTests.java`

Essa entidade usa `/api/v1/contas-receber`, paginacao, permissoes `CONTA_RECEBER:*`, criar, visualizar, editar, cancelar e baixar. A regra atual exige Empresa e Cliente ativos, valor total maior que zero, numero unico por empresa entre registros ativos, `valorPago` inicial igual a zero e `valorAberto` calculado por `valorTotal - valorPago`. Contas `PAGA` ou `CANCELADA` nao podem ser alteradas, e o cancelamento usa endpoint proprio `POST /api/v1/contas-receber/{id}/cancelar`.

`ParcelaReceber` representa cada vencimento de uma ContaReceber:

- `modules/financeiro/entity/ParcelaReceber.java`
- `modules/financeiro/enums/StatusParcelaReceber.java`
- `modules/financeiro/repository/ParcelaReceberRepository.java`
- `modules/financeiro/dto/request/ParcelaReceberRequest.java`
- `modules/financeiro/dto/request/BaixaParcelaReceberRequest.java`
- `modules/financeiro/dto/request/GerarParcelasReceberRequest.java`
- `modules/financeiro/dto/response/ParcelaReceberResponse.java`
- `modules/financeiro/mapper/ParcelaReceberMapper.java`
- `modules/financeiro/specification/ParcelaReceberSpecification.java`
- `modules/financeiro/validator/ParcelaReceberValidator.java`
- `modules/financeiro/service/ParcelaReceberService.java`
- `modules/financeiro/controller/ParcelaReceberController.java`
- `modules/financeiro/service/ParcelaReceberServiceTest.java`
- `integration/financeiro/ParcelaReceberControllerTests.java`
- `businessflow/ParcelaReceberBusinessFlowIntegrationTest.java`

Essa entidade usa `/api/v1/parcelas-receber`, paginacao, permissoes `PARCELA_RECEBER:*`, criar, visualizar, editar, cancelar e baixar. O endpoint `POST /api/v1/contas-receber/{id}/gerar-parcelas` usa `CondicaoPagamento` e `GeradorParcelasService` para criar as parcelas da conta, respeitando regra de vencimento e bloqueando nova geracao quando ja existem parcelas ativas. A baixa em `POST /api/v1/parcelas-receber/{id}/baixar` aceita pagamento parcial ou total, separa juros, multa e desconto, recalcula o saldo aberto da parcela e consolida `valorPago`, `valorAberto` e status da ContaReceber.

`Recebimento` registra cada entrada financeira vinculada a uma ParcelaReceber:

- `modules/financeiro/entity/Recebimento.java`
- `modules/financeiro/enums/StatusRecebimento.java`
- `modules/financeiro/repository/RecebimentoRepository.java`
- `modules/financeiro/dto/request/RecebimentoRequest.java`
- `modules/financeiro/dto/response/RecebimentoResponse.java`
- `modules/financeiro/mapper/RecebimentoMapper.java`
- `modules/financeiro/specification/RecebimentoSpecification.java`
- `modules/financeiro/validator/RecebimentoValidator.java`
- `modules/financeiro/service/RecebimentoService.java`
- `modules/financeiro/controller/RecebimentoController.java`
- `modules/financeiro/service/RecebimentoServiceTest.java`
- `integration/financeiro/RecebimentoControllerTests.java`
- `businessflow/RecebimentoBusinessFlowIntegrationTest.java`

Essa entidade usa `/api/v1/recebimentos`, paginacao, filtros por parcela, conta, forma de pagamento, empresa, filial, cliente e periodo. Nao possui `PUT` generico: a criacao registra o recebimento e baixa a parcela; o cancelamento usa `/cancelar`, muda o status para `CANCELADO`, estorna a parcela e atualiza a ContaReceber.

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

Estrutura atual dos testes:

```text
backend/src/test/java/br/com/invplus/
|-- businessflow/              # Fluxos ponta a ponta entre modulos
|-- concurrency/               # Concorrencia e optimistic lock
|-- integration/
|   |-- cadastros/
|   |-- pessoas/
|   |-- produtos/
|   |-- estoque/
|   |-- compras/
|   |-- vendas/
|   `-- financeiro/
|-- performance/               # Cargas locais simples
|-- security/                  # Auth, JWT, permissoes e seeds
|-- modules/                   # Testes de service por dominio
`-- shared/
```

Testes de service continuam em `modules/<modulo>/service/`, perto das regras de dominio.

`ContaPagar` amplia o modulo Financeiro com a estrutura:

- `modules/financeiro/entity/ContaPagar.java`
- `modules/financeiro/repository/ContaPagarRepository.java`
- `modules/financeiro/dto/request/ContaPagarRequest.java`
- `modules/financeiro/dto/response/ContaPagarResponse.java`
- `modules/financeiro/mapper/ContaPagarMapper.java`
- `modules/financeiro/validator/ContaPagarValidator.java`
- `modules/financeiro/specification/ContaPagarSpecification.java`
- `modules/financeiro/service/ContaPagarService.java`
- `modules/financeiro/controller/ContaPagarController.java`
- `modules/financeiro/service/ContaPagarServiceTest.java`
- `integration/financeiro/ContaPagarControllerTests.java`
- `businessflow/ContaPagarBusinessFlowIntegrationTest.java`

O endpoint base e `/api/v1/contas-pagar`; migrations V95 e V96 criam o schema,
indices, constraints e permissoes.

`shared/infrastructure/MigrationIdempotencyTest.java` valida a politica de seeds tecnicos em migrations. Qualquer arquivo SQL com `INSERT INTO` precisa conter `ON CONFLICT` ou `WHERE NOT EXISTS`, evitando duplicidade de permissoes, perfis, usuarios tecnicos e vinculos de seguranca.
