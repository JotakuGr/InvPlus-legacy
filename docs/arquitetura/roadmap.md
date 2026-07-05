# Roadmap - invPlus ERP

## Fase 1 - Preparacao

- [x] Definir nome provisório do projeto
- [x] Definir arquitetura inicial
- [x] Escolher stack tecnologica
- [x] Organizar estrutura de pastas
- [x] Criar documentacao inicial
- [x] Configurar Git

## Fase 2 - Ambiente

- [x] Configurar Docker Compose
- [x] Configurar PostgreSQL
- [x] Configurar pgAdmin
- [x] Configurar Redis para uso futuro
- [x] Configurar RabbitMQ para uso futuro
- [x] Ajustar credenciais via variaveis de ambiente

## Fase 3 - Backend Base

- [x] Criar projeto Spring Boot
- [x] Configurar Maven
- [x] Configurar PostgreSQL no Spring
- [x] Configurar Flyway
- [x] Criar migrations iniciais
- [x] Criar estrutura `common`, `shared`, `security` e `modules`
- [x] Padronizar estrutura interna dos modulos de negocio
- [x] Criar `BaseEntity`
- [x] Criar `SoftDeleteEntity`
- [x] Adicionar `version` com `@Version` para concorrencia otimista nas entidades auditadas
- [x] Criar `BaseRepository` com `JpaSpecificationExecutor`
- [x] Criar `BaseCrudService`, `BaseCrudController` e contrato `CrudMapper`
- [x] Ativar auditoria automatica com `AuditorAware<UUID>`
- [x] Padronizar listagens REST com `Page<T>`
- [x] Criar base inicial de eventos em `shared/events`
- [x] Criar value objects iniciais em `shared/domain/valueobject`
- [x] Padronizar logs com SLF4J + Logback
- [x] Criar tratamento global de excecoes

## Fase 4 - Seguranca

- [x] Criar entidade `Usuario`
- [x] Criar entidade `Perfil`
- [x] Criar entidade `Permissao`
- [x] Criar entidade `Auditoria`
- [x] Criar repositories JPA
- [x] Criar seed inicial de seguranca
- [x] Inserir perfil `ADMIN`
- [x] Inserir permissoes basicas
- [x] Inserir usuario administrador inicial
- [x] Implementar login real
- [x] Implementar JWT
- [x] Implementar filtro Bearer Token
- [x] Implementar refresh token persistido como hash
- [x] Implementar logout com revogacao de refresh token
- [x] Implementar primeiro endpoint com `@PreAuthorize`
- [x] Testar consulta via repository
- [x] Testar login via controller
- [x] Testar refresh token, logout, token invalido, token expirado e permissao negada

## Fase 5 - Endurecimento de Seguranca

- [ ] Trocar implementacao manual de JWT por `JwtEncoder`/`JwtDecoder` do Spring Security
- [ ] Definir politica de troca obrigatoria da senha inicial
- [x] Criar refresh token ou estrategia equivalente
- [x] Criar controle granular por permissoes nos endpoints
- [x] Criar logout/invalidation strategy inicial
- [ ] Criar blacklist de access token no Redis
- [ ] Criar auditoria automatica de login e eventos sensiveis

## Fase 6 - Modulos Principais

- [x] Cadastros
  - [x] Criar migration `V7__schema_cadastros.sql`
  - [x] Criar migration de ajuste `V8__endereco_uf_varchar.sql`
  - [x] Criar migration `V9__cadastros_fundamentais.sql`
  - [x] Criar migration `V10__cadastros_fase_2.sql`
  - [x] Criar migration `V11__permissoes_cadastros_fase_2.sql`
  - [x] Criar migration `V12__categoria_hierarquia_nome_unico.sql`
  - [x] Criar migration `V13__permissao_categoria_wildcard.sql`
  - [x] Criar migration `V14__categoria_ordem_marca_campos.sql`
  - [x] Criar migration `V15__permissao_marca_wildcard.sql`
  - [x] Criar migration `V16__empresa_melhorias.sql`
  - [x] Criar migration `V17__permissao_empresa_wildcard.sql`
  - [x] Criar migration `V18__arquivo_melhorias.sql`
  - [x] Criar migration `V19__permissao_arquivo_wildcard.sql`
  - [x] Criar migration `V20__arquivo_colunas_legadas_opcionais.sql`
  - [x] Criar entidades `Pais`, `Estado`, `Cidade`, `Endereco`, `Empresa`, `Filial`, `Categoria`, `Marca`, `UnidadeMedida` e `Arquivo`
  - [x] Criar repositories JPA herdando `BaseRepository`
  - [x] Criar DTOs de request e response
  - [x] Criar mappers, specifications e validators para a fase fundamental
  - [x] Criar services com CRUD inicial, paginacao e exclusao logica
  - [x] Criar controllers REST protegidos por permissao
  - [x] Aplicar cache inicial em cadastros de baixa alteracao
  - [x] Criar testes de repository e controller
  - [x] Fase 2 - UnidadeMedida completa: migration, entity, repository, DTOs, mapper, validator, service, controller, paginacao, soft delete, auditoria, cache, logs, testes unitarios, testes de integracao e documentacao
  - [x] Fase 2 - Categoria completa: migration, entity, repository, DTOs, mapper, validator, service, controller, paginacao, soft delete, auditoria, cache, logs, testes unitarios, testes de integracao, documentacao, ordem e caminho completo
  - [x] Fase 2 - Marca completa: migration, entity, repository, DTOs, mapper, validator, service, controller, paginacao, soft delete, auditoria, cache, logs, testes unitarios, testes de integracao e documentacao
  - [x] Fase 2 - Empresa completa: migration, entity, repository, DTOs, mapper, validator, service, controller, paginacao, soft delete, auditoria, cache, logs, testes unitarios, testes de integracao e documentacao
  - [x] Fase 2 - Arquivo completa: migration, entity, repository, DTOs, mapper, validator, service, controller, paginacao, soft delete, auditoria, cache, logs, testes unitarios, testes de integracao, documentacao, storage multi-provedor, hash SHA-256 e vinculo generico entidade/entidadeId
  - [x] Criar camada compartilhada `shared/storage` com provider LOCAL
  - [x] Adicionar `CLOUDFLARE_R2` em `StorageArquivo` e migration `V46__arquivo_cloudflare_r2.sql`
  - [x] Criar endpoint `POST /api/v1/arquivos/upload`
  - [x] Validar upload vazio, tipos permitidos, limite de 10 MB e metadados salvos
  - [ ] Implementar provider fisico Cloudflare R2 com bucket, credenciais e client S3-compatible
  - [x] Fase 2 - Revisao geral concluida antes de Pessoas
  - [ ] Fase 2 - Cadastros gerais fiscais: CategoriaProduto, SubCategoriaProduto, Fabricante, NCM, CEST, CFOP, OrigemMercadoria, CST, CSOSN e AliquotaICMS
- [ ] Pessoas
  - [x] Criar migration `V21__schema_pessoa.sql`
  - [x] Criar migration `V22__permissoes_pessoa.sql`
  - [x] Criar entidade base `Pessoa`
  - [x] Criar repository, DTOs, mapper, validator, service e controller
  - [x] Criar testes unitarios e de integracao para Pessoa
  - [x] Bloquear alteracao de CPF/CNPJ apos criacao de Pessoa
  - [x] Criar migration `V23__schema_cliente.sql`
  - [x] Criar migration `V24__permissoes_cliente.sql`
  - [x] Criar Cliente como papel comercial de Pessoa
  - [x] Criar repository, DTOs, mapper, validator, service e controller de Cliente
  - [x] Criar testes unitarios e de integracao para Cliente
  - [x] Criar migration `V25__schema_fornecedor.sql`
  - [x] Criar migration `V26__permissoes_fornecedor.sql`
  - [x] Criar Fornecedor como papel comercial de Pessoa
  - [x] Criar repository, DTOs, mapper, validator, service e controller de Fornecedor
  - [x] Criar testes unitarios e de integracao para Fornecedor
  - [x] Criar migration `V27__schema_funcionario.sql`
  - [x] Criar migration `V28__permissoes_funcionario.sql`
  - [x] Criar Funcionario como papel trabalhista de Pessoa
  - [x] Criar vinculo opcional entre Funcionario e Usuario
  - [x] Criar repository, DTOs, mapper, validator, service e controller de Funcionario
  - [x] Criar testes unitarios e de integracao para Funcionario
  - [x] Criar migration `V29__schema_transportadora.sql`
  - [x] Criar migration `V30__permissoes_transportadora.sql`
  - [x] Criar Transportadora como papel logistico de Pessoa
  - [x] Criar repository, DTOs, mapper, validator, service e controller de Transportadora
  - [x] Criar testes unitarios e de integracao para Transportadora
  - [x] Criar migration `V31__pessoa_role.sql`
  - [x] Registrar papeis ativos em `PessoaRole` sem substituir as tabelas especificas
  - [x] Integrar Cliente, Fornecedor, Funcionario e Transportadora com `PessoaRole`
  - [x] Criar migration `V32__schema_contato_pessoa.sql`
  - [x] Criar migration `V33__permissoes_contato_pessoa.sql`
  - [x] Criar ContatoPessoa
  - [x] Criar repository, DTOs, mapper, validator, service e controller de ContatoPessoa
  - [x] Criar testes unitarios e de integracao para ContatoPessoa
  - [x] Criar migration `V34__schema_documento_pessoa.sql`
  - [x] Criar migration `V35__permissoes_documento_pessoa.sql`
  - [x] Criar DocumentoPessoa
  - [x] Criar enum `TipoDocumentoPessoa`
  - [x] Criar repository, DTOs, mapper, validator, service e controller de DocumentoPessoa
  - [x] Criar testes unitarios e de integracao para DocumentoPessoa
  - [ ] Filtros avancados: Pessoa por nome, CPF/CNPJ, email e tipo; papeis por nome da Pessoa; DocumentoPessoa por tipo, numero e pessoa
  - [ ] Historico de alteracoes para Pessoa, Cliente, Fornecedor e Funcionario
  - [ ] Enderecos vinculados a Pessoa por tipo: cobranca, entrega, fiscal, comercial e residencial
  - [ ] Endpoint resumido `GET /api/v1/pessoas/{id}/resumo` com dados, papeis, contatos e documentos
  - [ ] Validacao real de CPF/CNPJ com digitos verificadores
  - [ ] Busca global `GET /api/v1/pessoas/search?termo=...`
  - [ ] Regras flexiveis por tipo de pessoa para papeis futuros
- [ ] Produtos
  - [x] Criar migration `V36__schema_produto.sql`
  - [x] Criar migration `V37__permissoes_produto.sql`
  - [x] Criar entidade central `Produto`
  - [x] Criar repository, DTOs, mapper, validator, service e controller de Produto
  - [x] Criar filtros avancados por nome, codigo, SKU, codigo de barras, categoria, marca e ativo
  - [x] Criar enums `TipoProduto` e `NaturezaProduto`
  - [x] Criar migration `V44__produto_tipo_natureza.sql`
  - [x] Adicionar filtros por tipo e natureza do produto
  - [x] Criar enum `StatusProduto`
  - [x] Criar migration `V45__produto_status_controles.sql`
  - [x] Adicionar `controlaEstoque`, `permiteCompra` e `permiteVenda`
  - [x] Adicionar filtros por status e controles operacionais do produto
  - [x] Criar endpoint `GET /api/v1/produtos/search?termo=...` para busca rapida
  - [x] Validar codigo de barras de forma flexivel para GTIN com 8, 12, 13 ou 14 digitos numericos
  - [x] Criar testes unitarios e de integracao para Produto
  - [x] Criar ProdutoFiscal para NCM, CFOP, CEST, origem e tributacao
  - [x] Criar migrations `V42__schema_produto_fiscal.sql` e `V43__permissoes_produto_fiscal.sql`
  - [x] Criar enum `OrigemMercadoria`
  - [x] Criar filtros de ProdutoFiscal por produto, NCM, CEST, origem e ativo
  - [x] Garantir uma ficha fiscal ativa por produto
  - [x] Criar testes unitarios e de integracao para ProdutoFiscal
  - [ ] Backlog: criar historico fiscal por vigencia (`ProdutoFiscalHistorico` ou `vigenciaInicio/vigenciaFim`)
  - [ ] Backlog: separar tributacao fiscal por UF com `ProdutoFiscalUF`
  - [ ] Backlog: separar CFOP de compra, venda, transferencia e devolucao
  - [ ] Backlog: validar CST/CSOSN conforme CRT da Empresa
  - [ ] Backlog: criar validacoes fiscais inteligentes por regime tributario
  - [ ] Backlog: aplicar cache seletivo em ProdutoFiscal
  - [ ] Backlog: expandir busca fiscal para descricao, observacao, NCM, CEST e origem
  - [ ] Backlog: criar auditoria fiscal apos o modulo Fiscal
  - [ ] Backlog: criar `TipoControleEstoque` com NORMAL, LOTE, SERIE e LOTE_E_VALIDADE
  - [ ] Backlog: adicionar `marcaPropria`
  - [ ] Backlog: criar `ProdutoClassificacaoABC`
  - [ ] Backlog: preparar kits com `ProdutoKit` e `ProdutoKitItem`
  - [ ] Criar historico de preco para vendas e financeiro
  - [x] Criar ProdutoImagem
  - [x] Integrar ProdutoImagem com Arquivo
  - [x] Criar migrations `V47__schema_produto_imagem.sql` e `V48__permissoes_produto_imagem.sql`
  - [x] Criar filtros de ProdutoImagem por produto, arquivo, principal e ativo
  - [x] Garantir apenas uma imagem principal ativa por produto
  - [x] Criar testes unitarios, controller e fluxo integrado para ProdutoImagem
  - [ ] Backlog: adicionar `tipoImagem` em ProdutoImagem para PRINCIPAL, GALERIA, FICHA_TECNICA, MANUAL e CERTIFICADO
  - [x] Criar ProdutoCodigoBarras
  - [x] Criar migrations `V49__schema_produto_codigo_barras.sql` e `V50__permissoes_produto_codigo_barras.sql`
  - [x] Criar enum `TipoCodigoBarras` com EAN unidade, EAN caixa, EAN pacote, GTIN logistico e codigo interno alternativo
  - [x] Criar filtros de ProdutoCodigoBarras por produto, tipo, codigo, principal e ativo
  - [x] Garantir codigo unico entre registros ativos e apenas um codigo principal ativo por produto
  - [x] Criar testes unitarios, controller e fluxo integrado para ProdutoCodigoBarras
  - [ ] Backlog: adicionar origem do codigo de barras: GERADO, FABRICANTE e IMPORTADO
  - [x] Criar ProdutoPreco
  - [x] Criar migrations `V51__schema_produto_preco.sql` e `V52__permissoes_produto_preco.sql`
  - [x] Criar enum `TipoPrecoProduto` com CUSTO, VENDA, ATACADO, VAREJO, PROMOCIONAL e ESPECIAL
  - [x] Preparar `ProdutoPreco` para multiempresa com `empresa_id` opcional
  - [x] Criar filtros de ProdutoPreco por produto, empresa, tipo, principal, vigencia e ativo
  - [x] Garantir preco unico por produto, tipo, empresa e vigencia inicial entre registros ativos
  - [x] Garantir apenas um preco principal ativo por produto, tipo e empresa
  - [x] Criar busca de preco vigente por produto, tipo e data
  - [x] Criar testes unitarios, controller e fluxo integrado para ProdutoPreco
  - [x] Criar ProdutoUnidade
  - [x] Criar migrations `V53__schema_produto_unidade.sql` e `V54__permissoes_produto_unidade.sql`
  - [x] Criar migration `V55__produto_unidade_fracionamento.sql`
  - [x] Criar filtros de ProdutoUnidade por produto, unidade, unidade padrao, compra, venda, estoque, fracionamento e ativo
  - [x] Garantir produto + unidade unico entre registros ativos
  - [x] Garantir apenas uma unidade padrao ativa por produto
  - [x] Exigir fator de conversao igual a 1 para unidade padrao
  - [x] Adicionar `permiteFracionamento` em ProdutoUnidade
  - [x] Criar testes unitarios, controller e fluxo integrado para ProdutoUnidade
  - [ ] Proxima ordem sugerida: ProdutoKit
  - [x] Criar ProdutoFornecedor
  - [x] Criar migrations `V38__schema_produto_fornecedor.sql` e `V39__permissoes_produto_fornecedor.sql`
  - [x] Criar filtros de ProdutoFornecedor por produto, fornecedor, principal, ativo e codigo do fornecedor
  - [x] Garantir apenas um fornecedor principal ativo por produto
  - [x] Criar testes unitarios e de integracao para ProdutoFornecedor
  - [x] Adicionar prioridade, status e lead time em ProdutoFornecedor
  - [x] Criar `FornecedorProdutoStatus` com HOMOLOGADO, PREFERENCIAL, EVENTUAL e BLOQUEADO
  - [x] Criar `ProdutoFornecedorPreco` para historico de custos por vigencia
  - [x] Criar migrations `V40__produto_fornecedor_melhorias.sql` e `V41__permissoes_produto_fornecedor_preco.sql`
  - [x] Criar testes unitarios para ProdutoFornecedorPreco
  - [x] Criar teste de fluxo completo `ProdutoBusinessFlowIntegrationTest`
  - [x] Validar integracao Pessoa -> Fornecedor -> Produto -> ProdutoFornecedor -> ProdutoFornecedorPreco -> ProdutoFiscal
  - [x] Criar `ProdutoAggregateService` e `ProdutoCompletoResponse`
  - [x] Criar endpoint `GET /api/v1/produtos/{id}/completo`
  - [x] Documentar `precoCusto`, `precoVenda` e `codigoBarras` de Produto como campos legados
  - [ ] Backlog: criar `ProdutoFacade` para operacoes compostas de negocio
  - [ ] Backlog: criar testes especificos de concorrencia otimista para Produto, ProdutoPreco e ProdutoUnidade
  - [ ] Criar ProdutoCategoria
  - [ ] Avaliar cache seletivo apenas para consultas estaveis de catalogo, evitando cache agressivo de preco e estoque
  - [ ] Migrar/remover preco de custo/venda legado de Produto apos estabilizar consumo de ProdutoPreco em Vendas/PDV
  - [ ] Preparar PostgreSQL Full Text Search para nome, descricao, marca e categoria
- [ ] Estoque
- [ ] Compras
- [ ] Vendas
- [ ] Pagamentos
- [ ] Faturamento
- [ ] Financeiro
- [ ] Fiscal
- [ ] CRM

## Fase 7 - Frontend

- [ ] Criar projeto Angular
- [ ] Criar layout base
- [ ] Criar tela de login
- [ ] Integrar login com API REST
- [ ] Criar dashboard inicial

## Fase 8 - Qualidade e Entrega

- [ ] Configurar OpenAPI/Swagger
- [ ] Aumentar cobertura de testes
- [ ] Configurar CI/CD
- [ ] Configurar observabilidade
- [ ] Preparar deploy com Docker

## Fase 9 - Evolucoes Futuras

- [ ] Cache com Redis
- [ ] Mensageria com RabbitMQ
- [ ] Relatorios e BI
- [ ] Integracao bancaria
- [ ] API publica
- [ ] Aplicativo mobile
- [ ] Microsservicos

## Ordem padrao de implementacao por entidade

Para manter consistencia entre os modulos, cada entidade deve evoluir nesta ordem:

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
