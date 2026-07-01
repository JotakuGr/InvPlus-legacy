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
- [x] Criar `BaseEntity`
- [x] Criar `SoftDeleteEntity`
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
- [x] Testar consulta via repository
- [x] Testar login via controller

## Fase 5 - Endurecimento de Seguranca

- [ ] Trocar implementacao manual de JWT por `JwtEncoder`/`JwtDecoder` do Spring Security
- [ ] Definir politica de troca obrigatoria da senha inicial
- [ ] Criar refresh token ou estrategia equivalente
- [ ] Criar controle granular por permissoes nos endpoints
- [ ] Criar logout/invalidation strategy se necessario
- [ ] Criar auditoria automatica de login e eventos sensiveis

## Fase 6 - Modulos Principais

- [ ] Cadastros
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

