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
V23__schema_cliente.sql
V24__permissoes_cliente.sql
V25__schema_fornecedor.sql
V26__permissoes_fornecedor.sql
V27__schema_funcionario.sql
V28__permissoes_funcionario.sql
V29__schema_transportadora.sql
V30__permissoes_transportadora.sql
V31__pessoa_role.sql
V32__schema_contato_pessoa.sql
V33__permissoes_contato_pessoa.sql
V34__schema_documento_pessoa.sql
V35__permissoes_documento_pessoa.sql
V36__schema_produto.sql
V37__permissoes_produto.sql
V38__schema_produto_fornecedor.sql
V39__permissoes_produto_fornecedor.sql
V40__produto_fornecedor_melhorias.sql
V41__permissoes_produto_fornecedor_preco.sql
V42__schema_produto_fiscal.sql
V43__permissoes_produto_fiscal.sql
V44__produto_tipo_natureza.sql
V45__produto_status_controles.sql
V46__arquivo_cloudflare_r2.sql
V47__schema_produto_imagem.sql
V48__permissoes_produto_imagem.sql
V49__schema_produto_codigo_barras.sql
V50__permissoes_produto_codigo_barras.sql
V51__schema_produto_preco.sql
V52__permissoes_produto_preco.sql
V53__schema_produto_unidade.sql
V54__permissoes_produto_unidade.sql
V55__produto_unidade_fracionamento.sql
```

O modulo Cadastros adiciona tabelas base reutilizaveis por outros dominios e permissao granular `CADASTROS` para proteger os endpoints REST. A fase fundamental criou a hierarquia geografica `pais -> estado -> cidade`, vinculou `endereco` a `cidade` e adicionou `filial` vinculada a `empresa`.

A fase 2 ja concluiu `UnidadeMedida`, exposta em `/api/v1/unidades-medida`, `Categoria`, exposta em `/api/v1/categorias`, `Marca`, exposta em `/api/v1/marcas`, `Empresa`, exposta em `/api/v1/empresas`, e `Arquivo`, exposta em `/api/v1/arquivos`. Todas usam permissao granular, paginacao, cache, soft delete, auditoria e testes unitarios/de integracao. `Categoria` tambem possui hierarquia opcional, ordenacao por `ordem`, caminho completo, bloqueio de auto-relacionamento e ciclo. `Arquivo` usa storage multi-provedor, hash SHA-256 para integridade/deteccao de duplicidade e vinculo generico `entidade/entidadeId`.

O upload fisico de arquivos foi isolado em `shared/storage`. O endpoint `POST /api/v1/arquivos/upload` aceita `multipart/form-data`, valida arquivo vazio, limite de 10 MB e tipos `image/jpeg`, `image/png`, `image/webp` e `application/pdf`, grava pelo provider configurado e salva somente metadados na tabela `arquivo`. O provider LOCAL esta implementado com publicacao em `/uploads/**` para desenvolvimento. Cloudflare R2 ja esta previsto em `StorageArquivo`, `StorageProvider` e na migration `V46__arquivo_cloudflare_r2.sql`, ficando a implementacao do client/bucket para etapa futura.

O modulo Pessoas foi iniciado com `Pessoa`, exposta em `/api/v1/pessoas`. Ela e o aggregate root para Cliente, Fornecedor, Funcionario, Transportadora, ContatoPessoa e DocumentoPessoa. O cadastro usa `tipoPessoa` (`FISICA` ou `JURIDICA`), CPF/CNPJ unico entre registros ativos, CPF/CNPJ imutavel apos a criacao, soft delete, auditoria, paginacao e permissoes `PESSOA:*`/`PESSOAS:*`. Apenas `PessoaService` deve alterar dados cadastrais centrais como nome, CPF/CNPJ, telefone e email.

`Cliente`, exposto em `/api/v1/clientes`, representa apenas o papel comercial de uma `Pessoa`, sem duplicar nome, CPF/CNPJ, telefone ou email. A entidade possui limite de credito, observacao e IDs preparados para condicao de pagamento e tabela de preco futuras. A regra atual permite a mesma pessoa assumir outros papeis no futuro, mas impede dois clientes ativos para a mesma pessoa. O endpoint usa paginacao, soft delete, auditoria e permissoes `CLIENTE:*`/`PESSOAS:*`.

`Fornecedor`, exposto em `/api/v1/fornecedores`, tambem representa um papel de `Pessoa` e nao duplica dados cadastrais. A entidade guarda condicao de pagamento futura, prazo medio de entrega e observacao. A mesma pessoa pode ser cliente e fornecedor ao mesmo tempo, mas nao pode possuir dois fornecedores ativos. O endpoint usa paginacao, soft delete, auditoria e permissoes `FORNECEDOR:*`/`PESSOAS:*`.

`Funcionario`, exposto em `/api/v1/funcionarios`, representa uma pessoa que trabalha na empresa e pode opcionalmente estar vinculada a um `Usuario` para acesso ao sistema. A entidade guarda cargo, salario, admissao, demissao e observacao. A mesma pessoa nao pode possuir dois funcionarios ativos, e o mesmo usuario nao pode estar vinculado a dois funcionarios ativos. O endpoint usa paginacao, soft delete, auditoria e permissoes `FUNCIONARIO:*`/`PESSOAS:*`.

`Transportadora`, exposta em `/api/v1/transportadoras`, representa o papel logistico de uma `Pessoa` e nao duplica dados cadastrais. A entidade guarda ANTT, tipo de veiculo principal, capacidade de carga em kg e observacao. A mesma pessoa pode assumir outros papeis em paralelo, mas nao pode possuir duas transportadoras ativas. O endpoint usa paginacao, soft delete, auditoria e permissoes `TRANSPORTADORA:*`/`PESSOAS:*`.

`PessoaRole` registra os papeis ativos de uma Pessoa (`CLIENTE`, `FORNECEDOR`, `FUNCIONARIO` e `TRANSPORTADORA`) sem substituir as tabelas especificas. Os services de cada papel registram/removem esse vinculo ao criar, atualizar a Pessoa vinculada ou excluir logicamente um papel.

`ContatoPessoa`, exposto em `/api/v1/contatos-pessoa`, guarda contatos operacionais de uma Pessoa, como Financeiro, Compras, Comercial ou RH. Ele nao altera dados cadastrais da Pessoa, possui validacao de pessoa ativa, nome obrigatorio e email valido, usa paginacao, soft delete, auditoria e permissoes `CONTATO_PESSOA:*`/`PESSOAS:*`.

`DocumentoPessoa`, exposto em `/api/v1/documentos-pessoa`, guarda documentos cadastrais de uma Pessoa, como RG, CPF, CNH, contrato social, alvara, licenca sanitaria, certificado digital e comprovante de endereco. Ele pode apontar para `Arquivo` quando houver anexo digital, valida pessoa ativa, arquivo ativo quando informado, tipo e numero obrigatorios, duplicidade por pessoa/tipo/numero e validade posterior ou igual a emissao. O endpoint usa paginacao, soft delete, auditoria e permissoes `DOCUMENTO_PESSOA:*`/`PESSOAS:*`.

Melhorias futuras planejadas para Pessoas incluem filtros avancados nas listagens, endpoint resumido de Pessoa com papeis/contatos/documentos, busca global por termo, historico de alteracoes relevantes, enderecos vinculados por tipo e validacao real de CPF/CNPJ por digitos verificadores. As regras por tipo de pessoa devem continuar flexiveis no curto prazo, permitindo que Pessoa Fisica ou Juridica assuma papeis conforme o processo de negocio exigir.

O modulo Produtos foi iniciado com `Produto`, exposto em `/api/v1/produtos`. Produto e a entidade central para os proximos modulos de Estoque, Compras, Vendas, Fiscal e Relatorios. Ele referencia `Categoria`, `Marca` opcional e `UnidadeMedida`, guarda codigos, descricao, tipo do produto, natureza do produto, status operacional, precos, pesos, dimensoes, estoque minimo e controles de lote, validade, fracionamento, estoque, compra e venda. O endpoint usa paginacao, soft delete, auditoria e permissoes `PRODUTO:*`.

A listagem de Produto possui filtros avancados por nome, codigo, SKU, codigo de barras, categoria, marca, tipo, natureza, status, controle de estoque, permissao de compra, permissao de venda e ativo usando `ProdutoSpecification`. Tambem existe busca rapida em `GET /api/v1/produtos/search?termo=...`, preparada para fluxos operacionais como PDV, vendas e compras. O codigo de barras segue validacao flexivel de GTIN, aceitando 8, 12, 13 ou 14 digitos numericos. `TipoProduto` classifica itens como produto, servico, materia-prima, insumo, embalagem, combustivel, ativo imobilizado e uso/consumo. `NaturezaProduto` indica se participa de venda, compra ou ambos. `StatusProduto` separa estado operacional de soft delete, permitindo produtos ativos, em desenvolvimento, descontinuados, bloqueados ou suspensos sem apagar historico. Dados fiscais do produto, historico de preco e imagens permanecem como responsabilidades separadas para `ProdutoFiscal`, historico de preco e integracao futura com `Arquivo`.

`ProdutoFornecedor` foi adicionado em `/api/v1/produtos-fornecedores` para relacionar Produto e Fornecedor em processos de compras, cotacao, entrada de mercadoria e historico de custo. A entidade possui filtros por produto, fornecedor, principal, ativo, status e codigo do fornecedor, impede duplicidade ativa de produto + fornecedor e garante apenas um fornecedor principal ativo por produto com indice unico parcial. Tambem possui prioridade, lead time e status `FornecedorProdutoStatus` para permitir escolha operacional entre fornecedores homologados, preferenciais, eventuais e bloqueados. As permissoes seguem o recurso `PRODUTO_FORNECEDOR` com CRUD granular e wildcard.

O historico de custos foi separado em `ProdutoFornecedorPreco`, exposto em `/api/v1/produtos-fornecedores-precos`. Cada preco possui vigencia inicial e final, permitindo analises futuras de compras, financeiro, formacao de preco e relatorios sem sobrescrever custos antigos.

`ProdutoFiscal` foi adicionado em `/api/v1/produtos-fiscais` para isolar dados tributarios do produto comercial. A entidade guarda NCM, CEST, origem da mercadoria, CFOP padrao, CST/CSOSN, CST PIS/COFINS e aliquotas de ICMS, IPI, PIS e COFINS. A regra atual permite apenas uma ficha fiscal ativa por produto, valida NCM com 8 digitos, CEST com 7 digitos quando informado, aliquotas nao negativas, pagina filtros por produto/NCM/CEST/origem/ativo e protege o recurso com `PRODUTO_FISCAL:*` ou `PRODUTO:*`.

`ProdutoImagem` foi adicionado em `/api/v1/produtos-imagens` para vincular Produto e Arquivo sem duplicar metadados de arquivo. A entidade guarda imagem principal, ordem e descricao, garante apenas uma imagem principal ativa por produto, impede duplicidade ativa de produto + arquivo, filtra por produto/arquivo/principal/ativo e protege o recurso com `PRODUTO_IMAGEM:*` ou `PRODUTO:*`. O fluxo real Produto -> upload de Arquivo -> ProdutoImagem esta coberto por teste integrado.

`ProdutoCodigoBarras` foi adicionado em `/api/v1/produtos-codigos-barras` para substituir a limitacao de um unico codigo de barras no cadastro de Produto. A entidade guarda tipo do codigo, codigo, indicador de principal e observacao, suportando EAN unidade, EAN caixa, EAN pacote, GTIN logistico e codigo interno alternativo. A regra atual garante codigo unico entre registros ativos, apenas um codigo principal ativo por produto, filtros por produto/tipo/codigo/principal/ativo e protecao por `PRODUTO_CODIGO_BARRAS:*` ou `PRODUTO:*`. O fluxo real Categoria/Marca/UnidadeMedida -> Produto -> ProdutoCodigoBarras esta coberto por teste integrado.

`ProdutoPreco` foi adicionado em `/api/v1/produtos-precos` para historico de precos comerciais do Produto. A entidade guarda tipo de preco, valor, vigencia inicial/final, principal, observacao e `empresa_id` opcional, preparando precificacao multiempresa. Suporta precos de custo, venda, atacado, varejo, promocional e especial. A regra atual garante preco unico por produto/tipo/empresa/vigencia inicial entre registros ativos, apenas um preco principal ativo por produto/tipo/empresa, filtros por produto/empresa/tipo/principal/vigencia/ativo, busca de preco vigente em `GET /api/v1/produtos-precos/vigente` e protecao por `PRODUTO_PRECO:*` ou `PRODUTO:*`. Produto ainda mantem preco de custo/venda por compatibilidade da fase atual, mas esses campos foram documentados como legados e os proximos fluxos de Vendas, PDV e Financeiro devem consumir `ProdutoPreco`.

`ProdutoUnidade` foi adicionado em `/api/v1/produtos-unidades` para permitir que um mesmo produto seja comprado, vendido ou estocado em unidades diferentes. A entidade guarda unidade de medida, fator de conversao, indicador de unidade padrao, flags de compra/venda/estoque/fracionamento e observacao. A regra atual garante produto + unidade unico entre registros ativos, apenas uma unidade padrao ativa por produto e fator de conversao igual a 1 para a unidade padrao. Os filtros cobrem produto, unidade de medida, unidade padrao, compra, venda, estoque, fracionamento e ativo, com protecao por `PRODUTO_UNIDADE:*` ou `PRODUTO:*`.

`ProdutoAggregateService` foi criado como camada de leitura consolidada. O endpoint `GET /api/v1/produtos/{id}/completo` retorna Produto, fornecedores, precos, imagens, codigos de barras, ficha fiscal, unidade padrao e demais unidades ativas em uma unica resposta. Essa decisao reduz chamadas do frontend e prepara telas de Vendas, Compras e Estoque sem acoplar esses modulos aos services internos de cada subentidade.

O fluxo integrado de Produtos agora possui `ProdutoBusinessFlowIntegrationTest`, que cria Pessoa Juridica, Fornecedor, Categoria, Marca, UnidadeMedida, Produto, ProdutoFornecedor, ProdutoFornecedorPreco e ProdutoFiscal usando os endpoints reais. Esse teste valida os vinculos e os principais filtros, servindo como prova de que Cadastros, Pessoas, Produtos e fiscal basico conversam corretamente.

Backlog fiscal: historico fiscal por vigencia, dados fiscais por UF, separacao de CFOP por operacao, validacao automatica de CST/CSOSN conforme CRT da Empresa, regras tributarias inteligentes, cache seletivo para ProdutoFiscal, busca fiscal ampliada e auditoria fiscal apos o modulo Fiscal. Backlog de catalogo: `tipoImagem` em ProdutoImagem, origem do ProdutoCodigoBarras, `TipoControleEstoque`, `marcaPropria`, `ProdutoClassificacaoABC`, kits com `ProdutoKit` e `ProdutoKitItem`, `ProdutoFacade`, testes de concorrencia otimista e migracao futura dos campos legados de Produto para consumo integral de ProdutoPreco/ProdutoCodigoBarras.

Indices simples, compostos e parciais sao usados nas principais consultas por registros ativos e relacionamentos, como `estado(pais_id, ativo)`, `cidade(estado_id, ativo)`, `filial(empresa_id, ativo)`, `categoria(LOWER(nome)) WHERE ativo = TRUE AND deleted_at IS NULL`, `categoria(categoria_pai_id, ordem, nome)`, `marca(LOWER(nome)) WHERE ativo = TRUE AND deleted_at IS NULL`, `empresa(cnpj) WHERE ativo = TRUE AND deleted_at IS NULL`, `arquivo(hash_sha256) WHERE ativo = TRUE AND deleted_at IS NULL`, `arquivo(entidade, entidade_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `pessoa(cpf_cnpj) WHERE ativo = TRUE AND deleted_at IS NULL`, `cliente(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `fornecedor(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `funcionario(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `funcionario(usuario_id) WHERE usuario_id IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL`, `transportadora(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `pessoa_role(pessoa_id, role) WHERE ativo = TRUE AND deleted_at IS NULL`, `contato_pessoa(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `documento_pessoa(pessoa_id, tipo_documento, LOWER(numero)) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto(LOWER(codigo)) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto(LOWER(sku)) WHERE sku IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL`, `produto(LOWER(codigo_barras)) WHERE codigo_barras IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL`, `produto(categoria_id/marca_id/unidade_medida_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fornecedor(produto_id, fornecedor_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fornecedor(produto_id) WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `produto_fornecedor(produto_id, prioridade) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fornecedor_preco(produto_fornecedor_id, vigencia_inicio, vigencia_fim) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fiscal(produto_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fiscal(ncm) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_imagem(produto_id, arquivo_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_imagem(produto_id) WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `produto_codigo_barras(LOWER(codigo)) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_codigo_barras(produto_id) WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `produto_preco(produto_id, tipo_preco, empresa_id/coalesce, vigencia_inicio) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_preco(produto_id, tipo_preco, empresa_id/coalesce) WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `produto_unidade(produto_id, unidade_medida_id) WHERE ativo = TRUE AND deleted_at IS NULL` e `produto_unidade(produto_id) WHERE unidade_padrao = TRUE AND ativo = TRUE AND deleted_at IS NULL`. Cache com Redis e filas com RabbitMQ permanecem provisionados no Docker, mas ainda entram como evolucao funcional futura.

O cache funcional inicial usa Spring Cache em cadastros de baixa alteracao, como Categoria, Marca, Arquivo, UnidadeMedida, Pais, Estado e Cidade. O Redis permanece como proximo passo para cache distribuido quando houver mais de uma instancia da API.

Logs seguem SLF4J + Logback por `logback-spring.xml`. Codigo de aplicacao nao deve usar `System.out.println()`.

Migrations Flyway devem separar schema de seeds tecnicos sempre que possivel. Inserts sao aceitos apenas para dados estruturais do sistema, como permissoes, perfis, usuario administrador inicial e vinculos de seguranca. Toda migration com `INSERT INTO` deve ser idempotente usando `ON CONFLICT` ou `WHERE NOT EXISTS`; essa regra e validada por `MigrationIdempotencyTest`.

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
