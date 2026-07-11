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
V56__schema_deposito.sql
V57__permissoes_deposito.sql
V58__deposito_controles_operacionais.sql
V59__schema_localizacao_estoque.sql
V60__permissoes_localizacao_estoque.sql
V61__schema_estoque.sql
V62__permissoes_estoque.sql
V63__schema_movimentacao_estoque.sql
V64__permissoes_movimentacao_estoque.sql
V65__schema_reserva_estoque.sql
V66__permissoes_reserva_estoque.sql
V67__schema_pedido_compra.sql
V68__permissoes_pedido_compra.sql
V69__schema_pedido_compra_item.sql
V70__permissoes_pedido_compra_item.sql
V71__schema_entrada_mercadoria.sql
V72__permissoes_entrada_mercadoria.sql
V73__schema_entrada_mercadoria_item.sql
V74__permissoes_entrada_mercadoria_item.sql
V75__compras_consolidacao.sql
V76__schema_pedido_venda.sql
V77__permissoes_pedido_venda.sql
V78__schema_pedido_venda_item.sql
V79__permissoes_pedido_venda_item.sql
V80__ajuste_reserva_estoque_origem_unica.sql
V81__schema_venda_faturamento.sql
V82__permissoes_venda_faturamento.sql
V83__schema_forma_pagamento.sql
V84__permissoes_forma_pagamento.sql
V85__schema_condicao_pagamento.sql
V86__permissoes_condicao_pagamento.sql
V87__condicao_pagamento_parcelas.sql
V88__schema_conta_receber.sql
V89__permissoes_conta_receber.sql
V90__schema_parcela_receber.sql
V91__permissoes_parcela_receber.sql
V92__schema_recebimento.sql
V93__permissoes_recebimento.sql
V94__venda_faturamento_conta_receber.sql
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

O modulo Estoque foi iniciado com `Deposito`, exposto em `/api/v1/depositos`. A entidade representa locais fisicos, logicos ou em transito onde o estoque fica armazenado, como loja principal, estoque central, camara fria, almoxarifado ou estoque em transito. O cadastro referencia `Empresa` obrigatoria e `Filial` opcional, classifica o deposito com `TipoDeposito`, define o deposito principal da empresa e controla se ele participa de venda, compra, transferencia e saldo fisico com `controlaEstoque`. O campo `bloqueado` representa bloqueio operacional temporario, diferente de soft delete ou inativacao. A regra atual garante codigo unico por empresa entre registros ativos, apenas um deposito principal ativo por empresa, filial ativa e pertencente a empresa informada, filtros por empresa/filial/codigo/nome/tipo/principal/controle de estoque/bloqueado/ativo, soft delete, auditoria, versionamento e permissoes `DEPOSITO:*` ou `ESTOQUE:*`.

`LocalizacaoEstoque`, exposta em `/api/v1/localizacoes-estoque`, detalha a posicao fisica ou logica dentro de um `Deposito`, como rua, corredor, prateleira, nivel, posicao, camara fria ou area de devolucao. A entidade referencia deposito obrigatorio, possui codigo unico por deposito entre registros ativos, permite marcar uma localizacao principal por deposito e usa `bloqueada` para impedir uso operacional sem excluir nem inativar o cadastro. As listagens aceitam filtros por deposito, codigo, descricao, rua, corredor, prateleira, principal, bloqueada e ativo, sempre com paginacao, soft delete, auditoria, versionamento e permissoes `LOCALIZACAO_ESTOQUE:*` ou `ESTOQUE:*`. A migration de schema ficou em `V59` porque `V58` ja estava reservada para os controles operacionais de Deposito.

`Estoque`, exposto em `/api/v1/estoques`, representa o saldo fisico atual de um Produto em uma LocalizacaoEstoque. A modelagem segue Produto -> Empresa/Filial -> Deposito -> Localizacao -> Estoque, evitando saldo agregado apenas por produto. A entidade guarda quantidade fisica, reservada, disponivel, avariada, em transito, estoque minimo, estoque maximo, data do ultimo movimento e `ultimoMovimentoId` para futura integracao com `MovimentacaoEstoque`. A disponibilidade e derivada pela regra `quantidadeDisponivel = quantidade - quantidadeReservada`, e os metodos de dominio `adicionar`, `retirar`, `reservar`, `cancelarReserva`, `baixarReserva`, `adicionarAvaria` e `removerAvaria` centralizam as proximas regras de Compra, Venda, PDV, Inventario e Transferencia. O endpoint `GET /api/v1/estoques/saldo` consulta saldo por produto, empresa, deposito e localizacao. A consolidacao adicionou `GET /api/v1/estoques/{id}/completo`, com saldo, ultima movimentacao e reservas ativas, e `GET /api/v1/estoques/disponivel`, resposta enxuta para consultas frequentes de PDV, Vendas e Compras. As listagens filtram por produto, empresa, filial, deposito, localizacao, estoque baixo, estoque zerado, com saldo, sem saldo e ativo, com permissoes `ESTOQUE_SALDO:*` ou `ESTOQUE:*`.

`MovimentacaoEstoque`, exposta em `/api/v1/movimentacoes-estoque`, registra o historico imutavel de tudo que alterou o estoque. A entidade nao herda soft delete, nao possui endpoint de edicao ou exclusao e guarda o contexto congelado da movimentacao: estoque, produto, empresa, filial opcional, deposito, localizacao, tipo, quantidade, saldo anterior, saldo posterior, origem, documento, observacao, data da movimentacao e auditoria de criacao. `origem` deixou de ser texto livre no codigo e passou a usar `MovimentacaoOrigem` com `COMPRA`, `VENDA`, `AJUSTE`, `TRANSFERENCIA`, `INVENTARIO`, `RESERVA`, `DEVOLUCAO`, `PRODUCAO`, `IMPORTACAO` e `API`; a coluna permanece `VARCHAR`, entao nao houve necessidade de nova migration. O `EstoqueService` delega alteracoes de saldo ao `EstoqueDomainService`, que centraliza `entrada`, `saida`, `ajuste`, `transferencia`, `reservar`, `cancelarReserva` e `baixarReserva`, registra movimentacoes e atualiza `ultimoMovimentoId`. A regra arquitetural para os proximos modulos e clara: Compras, Vendas, Inventario, Transferencia e ReservaEstoque nao devem alterar saldo diretamente via repository; devem chamar o dominio de estoque. As consultas aceitam filtros por estoque, produto, empresa, deposito, localizacao, tipo, origem e periodo, protegidas por `MOVIMENTACAO_ESTOQUE:*`, `MOVIMENTACAO_ESTOQUE:VISUALIZAR` ou `ESTOQUE:*`.

`ReservaEstoque`, exposta em `/api/v1/reservas-estoque`, separa quantidade para venda, pedido, separacao ou expedicao sem baixar fisicamente o saldo. A entidade fotografa Estoque, Produto, Empresa, Filial opcional, Deposito e LocalizacaoEstoque, guarda quantidade, `StatusReservaEstoque`, `MovimentacaoOrigem`, origem_id, expiracao, baixa, cancelamento e observacao. Nao existe `PUT` generico: a reserva possui fluxo de estado com criacao, `/cancelar` e `/baixar`. Reserva ativa aumenta `quantidadeReservada` e reduz `quantidadeDisponivel`; cancelamento devolve disponibilidade; baixa reduz quantidade fisica e reservada. Todas essas transicoes passam por `EstoqueDomainService`, que tambem gera `MovimentacaoEstoque`. As consultas filtram por estoque, produto, empresa, filial, deposito, localizacao, status, origem, origem_id e ativo, protegidas por `RESERVA_ESTOQUE:*`, acoes especificas ou `ESTOQUE:*`.

`EstoqueAggregateService` e `EstoqueFacade` completam a camada de consolidacao do estoque. O aggregate atende leituras ricas para frontend e operacoes, enquanto a facade passa a ser a API interna recomendada para Compras, Vendas, PDV e Inventario executarem entrada, saida, transferencia, ajuste, inventario, reserva, cancelamento e baixa sem conhecer repositories. Eventos de dominio `EstoqueAlteradoEvent`, `ReservaCriadaEvent`, `ReservaCanceladaEvent` e `ReservaBaixadaEvent` foram preparados para futura invalidacao de cache, auditoria, mensageria e notificacoes. A concorrencia do saldo e coberta por teste de optimistic lock e por carga simples com reservas simultaneas.

O modulo Compras foi iniciado com `PedidoCompra`, `PedidoCompraItem`, `EntradaMercadoria` e `EntradaMercadoriaItem`. `PedidoCompra`, exposto em `/api/v1/pedidos-compra`, registra a intencao de compra por empresa e fornecedor, com numero unico por empresa, status `ABERTO`, `APROVADO`, `PARCIALMENTE_RECEBIDO`, `RECEBIDO` e `CANCELADO`, valor total calculado pelos itens e acoes explicitas de aprovacao e cancelamento. Pedidos cancelados ou recebidos nao podem ser alterados.

`PedidoCompraItem`, exposto em `/api/v1/pedidos-compra-itens`, representa os produtos comprados no pedido. A entidade referencia pedido, produto e unidade do produto, valida quantidade maior que zero, valor unitario nao negativo e recalcula o valor total do pedido sempre que um item e criado, atualizado ou excluido logicamente.

`EntradaMercadoria`, exposta em `/api/v1/entradas-mercadoria`, representa o recebimento fisico da compra. Ela pode nascer vinculada a um PedidoCompra, usa status `ABERTA`, `CONFERIDA`, `EFETIVADA` e `CANCELADA`, e so movimenta estoque quando a entrada e efetivada. A efetivacao chama `EstoqueFacade.entrada(...)`, que delega ao dominio de estoque, atualiza saldo, registra `MovimentacaoEstoque` com origem `COMPRA` e preenche o historico de movimento.

`EntradaMercadoriaItem`, exposto em `/api/v1/entradas-mercadoria-itens`, detalha cada produto recebido, deposito e localizacao. O service valida que a localizacao pertence ao deposito, calcula valor total por quantidade e valor unitario, e bloqueia alteracoes quando a entrada ja nao esta editavel. O fluxo integrado `ComprasBusinessFlowIntegrationTest` valida PedidoCompra -> Itens -> EntradaMercadoria -> Estoque -> MovimentacaoEstoque, provando a conversa entre Cadastros, Pessoas, Produtos, Estoque e Compras.

A consolidacao de Compras segue o mesmo desenho aplicado em Produtos e Estoque. `CompraAggregateService` monta `PedidoCompraCompletoResponse` e `EntradaMercadoriaCompletaResponse`, expostos em `GET /api/v1/pedidos-compra/{id}/completo` e `GET /api/v1/entradas-mercadoria/{id}/completo`, para entregar ao frontend itens, historico, entradas vinculadas e movimentacoes geradas em uma unica chamada. `CompraFacade` passa a ser a API interna de alto nivel para outros modulos criarem pedido, aprovarem, cancelarem, registrarem entrada e efetivarem recebimento sem conhecer os services menores.

`PedidoCompraHistorico` e `EntradaMercadoriaHistorico` registram as transicoes de status com status anterior, novo status, usuario, data e observacao. `PedidoCompra` tambem foi preparado para aprovacao por nivel com `nivelAprovacao`, `aprovadoPor` e `aprovadoEm`. Eventos de dominio `PedidoCompraCriadoEvent`, `PedidoCompraAprovadoEvent` e `EntradaMercadoriaEfetivadaEvent` foram adicionados para futura integracao com Financeiro, Fiscal, Auditoria, RabbitMQ e notificacoes.

O modulo Vendas foi iniciado com `PedidoVenda`, exposto em `/api/v1/pedidos-venda`. A entidade registra o cabecalho comercial por Empresa e Cliente, com Filial opcional e Funcionario vendedor opcional. O pedido possui numero unico por empresa entre registros ativos, status `ABERTO`, `APROVADO`, `SEPARANDO`, `FATURADO` e `CANCELADO`, valor total inicial zerado, desconto, acrescimo, datas, observacao e campos de aprovacao (`nivelAprovacao`, `aprovadoPor`, `aprovadoEm`). Pedidos cancelados ou faturados nao podem ser alterados, e pedido faturado nao pode ser cancelado nem excluido logicamente.

`PedidoVendaItem`, exposto em `/api/v1/pedidos-venda-itens`, liga PedidoVenda, Produto, ProdutoPreco e ProdutoUnidade. O frontend nao envia `valorUnitario`; o service busca o `ProdutoPreco`, valida vigencia, valida que preco e unidade pertencem ao produto, calcula subtotal e total do item, e recalcula o valor total do pedido a cada criacao, atualizacao ou exclusao logica.

Ao aprovar um PedidoVenda, o service exige status `ABERTO`, exige itens e cria reservas automaticas de estoque para cada item. A quantidade comercial e convertida pelo `ProdutoUnidade.fatorConversao`; a reserva usa `MovimentacaoOrigem.VENDA` e `origemId = pedidoVendaId`, passa por `ReservaEstoqueService` e `EstoqueDomainService.reservar(...)`, reduz `quantidadeDisponivel`, aumenta `quantidadeReservada`, registra `MovimentacaoEstoque`, grava historico e publica `PedidoVendaReservadoEvent` antes de `PedidoVendaAprovadoEvent`. Ao cancelar pedido aprovado ou em separacao, as reservas ativas da origem `VENDA` sao canceladas, restaurando a disponibilidade. A migration `V80__ajuste_reserva_estoque_origem_unica.sql` ajusta o indice unico de reservas para bloquear duplicidade por estoque + origem + origem_id, permitindo que um pedido tenha reservas em estoques diferentes.

`VendaFaturamento`, exposto em `/api/v1/vendas-faturamentos`, transforma um PedidoVenda aprovado em venda faturada. O endpoint `POST /api/v1/vendas-faturamentos/faturar` exige pedido `APROVADO`, numero unico por empresa, reservas ativas de origem `VENDA` e `condicaoPagamentoId`. O service cria o faturamento com status `FATURADO`, baixa as reservas via `ReservaEstoqueService.baixar(...)`, reduz saldo fisico, gera `MovimentacaoEstoque`, registra historico, cria `ContaReceber`, gera `ParcelasReceber` pela condicao de pagamento, vincula a conta ao faturamento, publica eventos e atualiza o PedidoVenda para `FATURADO`. Nao existe `PUT` generico para faturamento nesta fase; cancelamento possui endpoint de estado separado e nao reabre automaticamente estoque.

`VendaAggregateService` agora retorna `PedidoVendaCompletoResponse` com cabecalho, itens, faturamento, reservas e historico em `GET /api/v1/pedidos-venda/{id}/completo`. `VendaFacade` centraliza criacao, aprovacao, cancelamento, faturamento e manipulacao de itens, preparando a integracao futura com Expedicao, Financeiro e Fiscal.

A primeira consolidacao de Vendas ja nasce com `PedidoVendaHistorico`, `VendaAggregateService`, `VendaFacade` e eventos de dominio `PedidoVendaCriadoEvent`, `PedidoVendaReservadoEvent`, `PedidoVendaAprovadoEvent`, `PedidoVendaCanceladoEvent`, `VendaFaturamentoCriadoEvent`, `PedidoVendaFaturadoEvent` e `ContaReceberGeradaPorVendaEvent`. `PedidoVendaBusinessFlowIntegrationTest` valida o fluxo real Empresa -> Cliente -> PedidoVenda, historico e concorrencia otimista; `PedidoVendaReservaIntegrationTest` valida PedidoVenda -> PedidoVendaItem -> ReservaEstoque -> Estoque -> MovimentacaoEstoque, incluindo cancelamento e conflito `409` em aprovacao concorrente; `VendaFaturamentoBusinessFlowIntegrationTest` valida PedidoVenda aprovado -> ReservaEstoque ativa -> VendaFaturamento -> baixa da reserva -> saldo fisico reduzido -> PedidoVenda faturado -> ContaReceber -> ParcelaReceber.

O modulo Pagamentos foi iniciado com `FormaPagamento`, exposta em `/api/v1/formas-pagamento`, e `CondicaoPagamento`, exposta em `/api/v1/condicoes-pagamento`. `FormaPagamento` classifica meios como dinheiro, Pix, cartao de credito, cartao de debito, boleto, transferencia, cheque, crediario e outros. A regra de negocio separa troco e parcelamento: troco so e permitido para `DINHEIRO`, enquanto parcelamento fica limitado a `CARTAO_CREDITO`, `BOLETO` e `CREDIARIO`. `CondicaoPagamento` usa parcelas filhas em `condicao_pagamento_parcela`, `RegraVencimento`, `consideraFeriados`, juros, multa e desconto. A soma das parcelas deve fechar 100%, numeros de parcela nao podem repetir e o endpoint `POST /api/v1/condicoes-pagamento/{id}/simular` calcula vencimentos e valores. Ambas usam soft delete, auditoria, versionamento, filtros paginados, codigo unico entre registros ativos e permissoes granulares.

O modulo Financeiro foi iniciado com `ContaReceber`, exposta em `/api/v1/contas-receber`, e `ParcelaReceber`, exposta em `/api/v1/parcelas-receber`. ContaReceber registra titulos a receber por Empresa e Cliente, com Filial e VendaFaturamento opcionais. O status inicial e `ABERTA`, `valorPago` inicia em zero e `valorAberto` e recalculado a partir de `valorTotal - valorPago`. O numero e unico por empresa entre registros ativos, contas pagas ou canceladas nao podem ser alteradas, e o cancelamento possui endpoint de estado separado em `POST /api/v1/contas-receber/{id}/cancelar`.

Quando o faturamento de venda e concluido, `ContaReceberService.gerarPorVendaFaturamento(...)` cria automaticamente a conta financeira, usa a `CondicaoPagamento` recebida no request para gerar as parcelas e impede duplicidade de conta para o mesmo `VendaFaturamento`. `PedidoVendaCompletoResponse` passa a devolver tambem `contaReceber` e `parcelasReceber`, permitindo ao frontend enxergar o ciclo comercial e financeiro em uma consulta agregada.

`ParcelaReceber` representa cada vencimento da conta. O endpoint `POST /api/v1/contas-receber/{id}/gerar-parcelas` usa `CondicaoPagamento` e `GeradorParcelasService`, respeitando `RegraVencimento`, ajustando arredondamento na ultima parcela e impedindo nova geracao quando a conta ja possui parcelas ativas. A baixa em `POST /api/v1/parcelas-receber/{id}/baixar` separa principal, juros, multa e desconto, permite pagamento parcial ou total, recalcula o saldo aberto da parcela e consolida `valorPago`, `valorAberto` e status da ContaReceber. As permissoes usam `CONTA_RECEBER:*` e `PARCELA_RECEBER:*` com acoes granulares para criar, visualizar, editar, cancelar e baixar.

`Recebimento`, exposto em `/api/v1/recebimentos`, registra o evento financeiro de entrada de dinheiro para uma ParcelaReceber. O request recebe apenas parcela, forma de pagamento, valores e data; conta, empresa, filial e cliente sao copiados da parcela/conta para facilitar filtros e historico. A criacao salva o recebimento e delega a baixa para `ParcelaReceberService.baixar(...)`, mantendo parcela e ContaReceber sincronizadas. O cancelamento usa `POST /api/v1/recebimentos/{id}/cancelar`, muda o recebimento para `CANCELADO`, estorna a parcela e reconsolida a conta sem apagar o registro historico.

`ContaPagar`, exposta em `/api/v1/contas-pagar`, inicia o fluxo de obrigacoes com fornecedores. A entidade relaciona Empresa, Filial opcional, Fornecedor e EntradaMercadoria opcional, inicia com status `ABERTA`, valor pago zero e valor aberto igual ao total. O numero e unico por empresa entre registros ativos. Contas pagas ou canceladas nao podem ser alteradas, e o cancelamento usa uma transicao de estado que preserva o historico financeiro.

Quando uma EntradaMercadoria e informada, o service valida que ela pertence a mesma Empresa, Filial e Fornecedor da ContaPagar. As consultas sao paginadas e aceitam filtros por relacionamentos, numero, status, periodo de emissao e ativo. O acesso usa `CONTA_PAGAR:*` e permissoes granulares de criar, visualizar, editar, cancelar e baixar.

`ParcelaPagar`, exposta em `/api/v1/parcelas-pagar`, representa os vencimentos da ContaPagar. Pode ser criada manualmente ou gerada em lote por `POST /api/v1/contas-pagar/{id}/gerar-parcelas`, reutilizando `CondicaoPagamento`, `GeradorParcelasService` e as regras de ajuste de dias uteis. A soma gerada e ajustada na ultima parcela para fechar exatamente o valor total da conta.

A baixa em `POST /api/v1/parcelas-pagar/{id}/baixar` permite pagamento parcial ou total e separa principal, juros, multa e desconto. Depois de cada baixa ou cancelamento, `ParcelaPagarService` recalcula `valorPago`, `valorAberto` e o status da ContaPagar na mesma transacao. Parcelas pagas ou canceladas nao podem ser alteradas. O acesso usa `PARCELA_PAGAR:*` e permissoes granulares.

`Pagamento`, exposto em `/api/v1/pagamentos`, registra o evento financeiro de saida vinculado a uma ParcelaPagar e FormaPagamento. ContaPagar, Empresa, Filial e Fornecedor sao copiados da parcela para preservar contexto e facilitar consultas. A criacao salva o evento com status `ATIVO` e delega a baixa para `ParcelaPagarService`, mantendo uma unica porta para alteracao dos saldos.

O cancelamento em `POST /api/v1/pagamentos/{id}/cancelar` nao remove o evento: estorna principal e encargos na ParcelaPagar, reconsolida a ContaPagar e muda o Pagamento para `CANCELADO` na mesma transacao. As consultas aceitam filtros por parcela, conta, forma, empresa, filial, fornecedor, status, periodo e ativo. O acesso usa `PAGAMENTO:*` e permissoes de criar, visualizar e cancelar.

`MovimentoFinanceiro`, exposto em `/api/v1/movimentos-financeiros`, consolida entradas e saídas em um registro rastreável por origem. Recebimentos criam movimentos `ENTRADA/RECEBIMENTO` e pagamentos criam `SAIDA/PAGAMENTO`; os services de origem também cancelam o movimento na mesma transação quando ocorre estorno. Ajustes, transferências e estornos manuais usam o mesmo modelo, enquanto a API bloqueia a criação manual das origens automáticas. A restrição única parcial por `origem + origem_id` evita duplicidade ativa sem eliminar o histórico cancelado.

`CentroCusto`, exposto em `/api/v1/centros-custo`, classifica receitas e despesas em uma árvore isolada por Empresa. A entidade suporta tipos `RECEITA`, `DESPESA` e `AMBOS`, caminho hierárquico completo, código único por empresa, soft delete, auditoria e optimistic lock. O validator impede pai de outra empresa, autorreferência e ciclos indiretos. A migration V106 integra o centro opcionalmente a ContaReceber, ContaPagar e MovimentoFinanceiro, validando Empresa e natureza da operação. Recebimentos e pagamentos propagam a classificação para o movimento gerado, e as consultas e relatórios financeiros aceitam filtro por centro.

`PlanoConta`, exposto em `/api/v1/planos-contas`, organiza a classificação contábil gerencial em uma árvore isolada por Empresa. Contas `SINTETICA` são agrupadoras e contas `ANALITICA` podem receber lançamentos. A natureza distingue receita, despesa, ativo, passivo e patrimônio líquido. O domínio calcula o caminho completo, aplica soft delete, auditoria e optimistic lock, enquanto o validator protege unicidade por empresa, autorreferência, vínculos entre empresas e ciclos indiretos. A migration V109 integra o plano opcionalmente a ContaReceber, ContaPagar e MovimentoFinanceiro: contas a receber e entradas exigem plano analítico de receita; contas a pagar e saídas exigem plano analítico de despesa. Recebimentos e pagamentos propagam a classificação para o movimento gerado, e consultas e relatórios financeiros aceitam filtro por plano de conta.

`FinanceiroResumoService` também expõe a DRE financeira em `GET /api/v1/financeiro/relatorios/dre`. O relatório considera somente movimentos financeiros ativos com PlanoConta informado, agrupa `ENTRADA` como receita e `SAIDA` como despesa, calcula receita bruta, despesas operacionais, resultado operacional e saldo final. Os filtros aceitos são período, Empresa, Filial, CentroCusto e PlanoConta.

`ContaBancaria`, exposta em `/api/v1/contas-bancarias`, cadastra contas bancárias e caixas internos por Empresa, com Filial opcional. A entidade guarda banco, agência, número da conta, tipo, titular, documento do titular, chave Pix, saldo inicial, saldo atual, data do saldo inicial e indicador de conta principal. A regra atual garante Empresa ativa, Filial da mesma Empresa, documento com 11 ou 14 dígitos quando informado, saldo inicial não negativo, saldo atual iniciado pelo saldo inicial, unicidade por Empresa + banco + agência + número e apenas uma conta principal ativa por Empresa. As migrations V110/V111 criam a tabela, índices parciais e permissões `CONTA_BANCARIA:*`.

`ExtratoBancario`, exposto em `/api/v1/extratos-bancarios`, registra importacoes de extratos por ContaBancaria. A entidade guarda periodo, saldos inicial/final, origem (`MANUAL`, `OFX`, `CSV`, `CNAB`, `API_BANCARIA`, `OPEN_FINANCE`), nome e hash do arquivo, data de importacao automatica, status e observacao. A regra atual valida conta bancaria ativa, periodo valido, hash SHA-256 quando informado, hash unico entre extratos ativos e cancelamento por status `CANCELADO` sem soft delete, preservando a trilha historica de importacao. As migrations V112/V113 criam a tabela, indices e permissoes `EXTRATO_BANCARIO:*`.

`ExtratoBancarioLancamento`, exposto em `/api/v1/extratos-bancarios-lancamentos`, detalha os creditos e debitos importados ou digitados em um ExtratoBancario. A entidade guarda data, descricao, documento, tipo (`CREDITO` ou `DEBITO`), valor, saldo apos lancamento, identificador bancario, categoria bancaria, conciliacao e observacao. A regra atual exige extrato ativo e nao cancelado, valor positivo, identificador unico por extrato ativo quando informado, categoria default `OUTRO` e conciliacao inicial falsa. Lancamentos conciliados nao podem ser editados nem excluidos, e o service ja prepara os metodos internos `marcarComoConciliado` e `desmarcarComoConciliado` para a futura conciliacao bancaria. As migrations V114/V115 criam a tabela, indices e permissoes `EXTRATO_BANCARIO_LANCAMENTO:*`.

`ConciliacaoBancaria`, exposta em `/api/v1/conciliacoes-bancarias`, liga um lancamento do extrato a um MovimentoFinanceiro ativo sem usar soft delete. O aggregate transacional valida tipo e valor, persiste a conciliacao, marca o lancamento e recalcula o estado do extrato. Cancelamentos preservam a conciliacao com status `CANCELADA`, usuario, data e motivo, liberam o lancamento e recalculam o extrato. A consistencia concorrente usa `@Version`, tratamento HTTP 409 e indices unicos parciais para lancamento e movimento conciliados. As migrations V116/V117 criam schema, indices e permissoes `CONCILIACAO_BANCARIA:*`.

`ImportacaoExtratoBancario` registra o processamento de arquivos sem duplicar o dominio de extratos. A camada `financeiro/importacao` separa controller multipart, parser seguro, DTOs intermediarios, validacao e orquestracao. `OfxParser` converte OFX 1.x/2.x para `ExtratoImportado`; o service identifica a ContaBancaria e delega a criacao a `ExtratoBancarioService` e `ExtratoBancarioLancamentoService`. O arquivo nao e armazenado, o saldo interno nao e alterado e a conciliacao nao ocorre automaticamente. As migrations V118/V119 criam o relatorio de processamento e permissoes `IMPORTACAO_EXTRATO:*`.

Indices simples, compostos e parciais sao usados nas principais consultas por registros ativos e relacionamentos, como `estado(pais_id, ativo)`, `cidade(estado_id, ativo)`, `filial(empresa_id, ativo)`, `categoria(LOWER(nome)) WHERE ativo = TRUE AND deleted_at IS NULL`, `categoria(categoria_pai_id, ordem, nome)`, `marca(LOWER(nome)) WHERE ativo = TRUE AND deleted_at IS NULL`, `empresa(cnpj) WHERE ativo = TRUE AND deleted_at IS NULL`, `arquivo(hash_sha256) WHERE ativo = TRUE AND deleted_at IS NULL`, `arquivo(entidade, entidade_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `pessoa(cpf_cnpj) WHERE ativo = TRUE AND deleted_at IS NULL`, `cliente(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `fornecedor(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `funcionario(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `funcionario(usuario_id) WHERE usuario_id IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL`, `transportadora(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `pessoa_role(pessoa_id, role) WHERE ativo = TRUE AND deleted_at IS NULL`, `contato_pessoa(pessoa_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `documento_pessoa(pessoa_id, tipo_documento, LOWER(numero)) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto(LOWER(codigo)) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto(LOWER(sku)) WHERE sku IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL`, `produto(LOWER(codigo_barras)) WHERE codigo_barras IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL`, `produto(categoria_id/marca_id/unidade_medida_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fornecedor(produto_id, fornecedor_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fornecedor(produto_id) WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `produto_fornecedor(produto_id, prioridade) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fornecedor_preco(produto_fornecedor_id, vigencia_inicio, vigencia_fim) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fiscal(produto_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_fiscal(ncm) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_imagem(produto_id, arquivo_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_imagem(produto_id) WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `produto_codigo_barras(LOWER(codigo)) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_codigo_barras(produto_id) WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `produto_preco(produto_id, tipo_preco, empresa_id/coalesce, vigencia_inicio) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_preco(produto_id, tipo_preco, empresa_id/coalesce) WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `produto_unidade(produto_id, unidade_medida_id) WHERE ativo = TRUE AND deleted_at IS NULL`, `produto_unidade(produto_id) WHERE unidade_padrao = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `deposito(empresa_id, LOWER(codigo)) WHERE ativo = TRUE AND deleted_at IS NULL`, `deposito(empresa_id) WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL`, `deposito(controla_estoque) WHERE ativo = TRUE AND deleted_at IS NULL` e `deposito(bloqueado) WHERE ativo = TRUE AND deleted_at IS NULL`. Cache com Redis e filas com RabbitMQ permanecem provisionados no Docker, mas ainda entram como evolucao funcional futura.

O cache funcional inicial usa Spring Cache em cadastros de baixa alteracao, como Categoria, Marca, Arquivo, UnidadeMedida, Pais, Estado e Cidade. O Redis permanece como proximo passo para cache distribuido quando houver mais de uma instancia da API.

Logs seguem SLF4J + Logback por `logback-spring.xml`. Codigo de aplicacao nao deve usar `System.out.println()`.

Migrations Flyway devem separar schema de seeds tecnicos sempre que possivel. Inserts sao aceitos apenas para dados estruturais do sistema, como permissoes, perfis, usuario administrador inicial e vinculos de seguranca. Toda migration com `INSERT INTO` deve ser idempotente usando `ON CONFLICT` ou `WHERE NOT EXISTS`; essa regra e validada por `MigrationIdempotencyTest`.

Os testes automatizados estao organizados por finalidade: `businessflow/` para fluxos ponta a ponta entre modulos, `concurrency/` para optimistic lock e concorrencia, `integration/<modulo>/` para controllers e integracoes focadas por dominio, `security/` para autenticacao/permissoes/seeds, `performance/` para cargas locais simples e `modules/<modulo>/service/` para regras de dominio mais proximas do codigo testado.

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

Sugestoes de conciliacao sao calculadas sob demanda. O banco aplica os filtros eliminatorios e o Java pontua
somente candidatos compativeis; a confirmacao continua no servico transacional de conciliacao.

A central de conciliacao usa projecoes agregadas e paginas de pendencias. A confirmacao em lote delega cada par
a um bean transacional independente, preservando sucessos quando outro item e rejeitado.

O contexto empresarial liga Usuario a Empresa e Filial por entidades auditadas. O JWT carrega apenas UUIDs e o
filtro revalida os vinculos antes de montar o principal autenticado.
