# Stack Tecnologica - invPlus ERP

## Backend

| Tecnologia | Versao/uso |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Web MVC | API REST |
| Spring Data JPA | Persistencia relacional |
| Hibernate ORM | Mapeamento objeto-relacional |
| Spring Security | Autenticacao e autorizacao |
| JWT | Autenticacao stateless |
| Bean Validation | Validacao de DTOs e entidades |
| Flyway | Versionamento do banco |
| Maven | Build e gerenciamento de dependencias |
| Lombok | Reducao de boilerplate |
| Jackson 3 | Serializacao JSON via Spring Boot 4 |

## Frontend

| Tecnologia | Uso |
|---|---|
| Angular | Interface web SPA |
| TypeScript | Linguagem do frontend |
| HTML | Estrutura das telas |
| CSS/SCSS | Estilizacao |

## Banco de Dados

| Tecnologia | Uso |
|---|---|
| PostgreSQL 18 | Banco relacional principal |
| pgcrypto | UUID e hash BCrypt no seed inicial |
| citext | Email case-insensitive |

## Infraestrutura

| Tecnologia | Uso |
|---|---|
| Docker | Containers locais |
| Docker Compose | Orquestracao local |
| pgAdmin | Administracao visual do PostgreSQL |
| Redis | Container preparado para uso futuro |
| RabbitMQ | Container preparado para mensageria futura |

## Testes

| Tecnologia | Uso |
|---|---|
| JUnit 5 | Testes automatizados |
| Spring Boot Test | Testes de contexto e integracao |
| MockMvc | Testes de controller |
| AssertJ | Assertions fluentes |

## Configuracoes de Seguranca

O profile principal nao possui credenciais fixas. As seguintes variaveis sao obrigatorias:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
INVPLUS_JWT_SECRET
INVPLUS_JWT_EXPIRATION_MINUTES
```

O profile `test` usa `backend/src/test/resources/application-test.yml` para manter a suite automatizada reproduzivel.

## Tecnologias Futuras

| Tecnologia | Uso futuro |
|---|---|
| OpenAPI/Swagger | Documentacao de API |
| Redis | Cache |
| RabbitMQ | Mensageria |
| CI/CD | Automacao de build e deploy |
| Observabilidade | Logs, metricas e tracing |
| Microsservicos | Evolucao arquitetural futura |

