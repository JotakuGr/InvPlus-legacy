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
  - [x] Criar entidades `Pais`, `Estado`, `Cidade`, `Endereco`, `Empresa`, `Filial`, `Categoria`, `Marca`, `UnidadeMedida` e `Arquivo`
  - [x] Criar repositories JPA herdando `BaseRepository`
  - [x] Criar DTOs de request e response
  - [x] Criar mappers, specifications e validators para a fase fundamental
  - [x] Criar services com CRUD inicial, paginacao e exclusao logica
  - [x] Criar controllers REST protegidos por permissao
  - [x] Aplicar cache inicial em cadastros de baixa alteracao
  - [x] Criar testes de repository e controller
  - [x] Fase 2 - UnidadeMedida completa: migration, entity, repository, DTOs, mapper, validator, service, controller, paginacao, soft delete, auditoria, cache, logs, testes unitarios, testes de integracao e documentacao
  - [x] Fase 2 - Categoria completa: migration, entity, repository, DTOs, mapper, validator, service, controller, paginacao, soft delete, auditoria, cache, logs, testes unitarios, testes de integracao e documentacao
  - [ ] Fase 2 - Proximas entidades: Marca, Empresa e Arquivo
  - [ ] Fase 2 - Cadastros gerais fiscais: CategoriaProduto, SubCategoriaProduto, Fabricante, NCM, CEST, CFOP, OrigemMercadoria, CST, CSOSN e AliquotaICMS
- [ ] Pessoas
- [ ] Produtos
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
