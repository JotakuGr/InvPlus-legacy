<div align="center">

<img src="docs/images/logo-invplus.png" alt="invPlus ERP" width="320"/>

# invPlus ERP

### Sistema ERP moderno, modular e escalável para gestão empresarial

Desenvolvido com **Java 25**, **Spring Boot**, **Angular**, **PostgreSQL**, **Docker** e arquitetura orientada a domínio.

<br>

<a href="https://github.com/JotakuGr/invPlus/actions/workflows/backend-ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/JotakuGr/invPlus/backend-ci.yml?label=Backend%20CI&style=for-the-badge"/>
</a>

<a href="https://github.com/JotakuGr/invPlus/actions/workflows/docker.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/JotakuGr/invPlus/docker.yml?label=Docker&style=for-the-badge"/>
</a>

<a href="https://github.com/JotakuGr/invPlus/actions/workflows/codeql.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/JotakuGr/invPlus/codeql.yml?label=CodeQL&style=for-the-badge"/>
</a>

<a href="https://github.com/JotakuGr/invPlus/actions/workflows/dependency-review.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/JotakuGr/invPlus/dependency-review.yml?label=Dependency%20Review&style=for-the-badge"/>
</a>

<!--Licença-->
<img src="https://img.shields.io/github/license/JotakuGr/invPlus?style=for-the-badge">

<br>

<img src="https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Versão-0.1.0--alpha-orange?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Licença-MIT-blue?style=for-the-badge"/>

<br>

<img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/Angular-20-DD0031?style=for-the-badge&logo=angular&logoColor=white"/>
<img src="https://img.shields.io/badge/PostgreSQL-17-336791?style=for-the-badge&logo=postgresql&logoColor=white"/>
<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"/>
<img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white"/>

</div>

---

> [!WARNING]
>
> ## 🚧 Projeto em Desenvolvimento
>
> O **invPlus ERP** encontra-se em desenvolvimento ativo.
>
> O backend possui uma base sólida, arquitetura consolidada e ampla cobertura de testes automatizados, porém **o sistema ainda não está pronto para utilização em ambiente de produção**.

---

<div align="center">

## 📚 Navegação Rápida

[📖 Documentação](docs/) •
[🏗 Arquitetura](docs/arquitetura/arquitetura-geral.md) •
[📚 API REST](docs/api/endpoints.md) •
[🧪 Testes](docs/testes/testes.md) •
[🗺 Roadmap](docs/roadmap/roadmap.md) •
[📋 Regras de Negócio](docs/regras-negocio/)

</div>

---

# 📖 Sobre

O **invPlus ERP** é um sistema ERP moderno desenvolvido para oferecer uma base robusta para gestão empresarial, utilizando uma arquitetura modular baseada em boas práticas de engenharia de software.

O projeto foi concebido priorizando:

- Arquitetura escalável
- Baixo acoplamento
- Alta coesão
- Código limpo
- Segurança
- Testabilidade
- Facilidade de manutenção
- Evolução contínua

Toda a estrutura foi desenvolvida utilizando **Spring Boot**, separando claramente responsabilidades através de:

- Controllers
- Services
- Validators
- Specifications
- Domain Services
- Aggregate Services
- Facades
- Eventos de Domínio

---

# ✨ Principais Características

- ✅ Arquitetura Modular
- ✅ Java 25
- ✅ Spring Boot 3
- ✅ Spring Security + JWT
- ✅ PostgreSQL
- ✅ Flyway
- ✅ Docker
- ✅ Redis
- ✅ RabbitMQ
- ✅ Domain Services
- ✅ Aggregate Services
- ✅ Facades
- ✅ Eventos de Domínio
- ✅ Soft Delete
- ✅ Auditoria
- ✅ Optimistic Lock
- ✅ Paginação
- ✅ Specifications
- ✅ GitHub Actions
- ✅ CodeQL
- ✅ Dependency Review
- ✅ Testes Automatizados

---

# 🛠 Tecnologias

<div align="center">

<img src="https://skillicons.dev/icons?i=java,spring,angular,postgres,docker,redis,rabbitmq,git,github,idea"/>

</div>

---

# 📊 Arquitetura em Números

| Métrica | Valor |
|---------|------:|
| Java | **25** |
| Spring Boot | **3.x** |
| Angular | **20** |
| Migrations Flyway | **119+** |
| Testes Automatizados | **657** |
| Controllers REST | **60+** |
| Entidades | **70+** |
| DTOs | **200+** |
| Services | **70+** |
| Arquitetura | **Modular** |
| Banco de Dados | **PostgreSQL** |
| Segurança | **JWT + RBAC** |
| Integração Contínua | **GitHub Actions** |

---

# 📦 Módulos Implementados

| Módulo | Status |
|---------|:------:|
| 🔐 Segurança | ✅ |
| 🏢 Cadastros | ✅ |
| 👥 Pessoas | ✅ |
| 📦 Produtos | ✅ |
| 🏬 Estoque | ✅ |
| 🛒 Compras | ✅ |
| 💰 Vendas | ✅ |
| 🏦 Financeiro | ✅ |

---

# 🏗 Arquitetura

```text
Cliente
      │
      ▼
Controllers
      │
      ▼
Services
      │
      ▼
Validators
      │
      ▼
Domain Services
      │
      ▼
Repositories
      │
      ▼
PostgreSQL
```

---

# 📂 Estrutura do Projeto

<details>
<summary>Visualizar estrutura</summary>

```text
invPlus
│
├── backend
├── frontend
├── docs
├── database
├── docker
├── scripts
└── .github
```

</details>

---

# 🎥 Demonstração

Em breve serão adicionados:

- GIFs
- Screenshots
- Dashboard
- Fluxos de negócio
- Swagger/OpenAPI
- Vídeos de demonstração

---
# 🏛 Arquitetura

O **invPlus ERP** foi desenvolvido utilizando uma arquitetura modular, baseada em princípios de **DDD (Domain-Driven Design)**, **SOLID** e **Clean Architecture**, buscando alta coesão, baixo acoplamento e facilidade de evolução.

## Camadas da Aplicação

```text
                    Cliente
                        │
                        ▼
                 REST Controllers
                        │
                        ▼
                    Services
                        │
         ┌──────────────┴──────────────┐
         ▼                             ▼
   Domain Services              Aggregate Services
         │                             │
         └──────────────┬──────────────┘
                        ▼
                    Repositories
                        │
                        ▼
                   PostgreSQL
```

---

## Padrões Utilizados

| Padrão | Utilização |
|---------|------------|
| Domain Service | Regras compartilhadas entre entidades |
| Aggregate Service | Consultas consolidadas |
| Facade | Comunicação entre módulos |
| Specification | Filtros dinâmicos |
| Mapper | Conversão Entity ⇄ DTO |
| Validator | Validações de negócio |
| DTO | Comunicação da API |
| Soft Delete | Exclusão lógica |
| Auditoria | Rastreabilidade |
| Optimistic Lock | Controle de concorrência |
| Eventos de Domínio | Integrações internas |

---

# 📦 Estrutura do Projeto

```text
invPlus
│
├── backend
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── br/com/invplus
│   │   │   │       ├── modules
│   │   │   │       └── shared
│   │   │   └── resources
│   │   └── test
│   │
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend
│
├── docs
│
├── docker
│
├── scripts
│
└── .github
```

---

# 🧩 Módulos

| Módulo | Descrição | Status |
|---------|-----------|:------:|
| 🔐 Segurança | JWT, autenticação, autorização e permissões | ✅ |
| 🏢 Cadastros | Empresas, filiais, países, estados e cidades | ✅ |
| 👥 Pessoas | Clientes, fornecedores e funcionários | ✅ |
| 📦 Produtos | Produtos, preços, unidades e classificações | ✅ |
| 🏬 Estoque | Saldo, movimentações, reservas e localização | ✅ |
| 🛒 Compras | Pedido de compra e entrada de mercadoria | ✅ |
| 💰 Vendas | Pedido, faturamento e reservas | ✅ |
| 🏦 Financeiro | Contas, parcelas, pagamentos, recebimentos, DRE e conciliação | ✅ |
| 📊 Relatórios | Dashboard, Fluxo de Caixa e DRE | ✅ |
| 🌐 API Pública | Planejado | 🚧 |
| 📱 Mobile | Planejado | 🚧 |

---

# 🔄 Fluxos de Negócio

## Fluxo Comercial

```text
Pedido de Venda
        │
        ▼
Reserva de Estoque
        │
        ▼
Faturamento
        │
        ▼
Conta a Receber
        │
        ▼
Parcelas
        │
        ▼
Recebimento
        │
        ▼
Movimento Financeiro
```

As sugestoes de conciliacao sao calculadas sob demanda, com filtros por conta, empresa, tipo, valor e data.
A confirmacao permanece manual e revalida todas as regras financeiras.

---

## Fluxo de Compras

```text
Pedido de Compra
        │
        ▼
Entrada de Mercadoria
        │
        ▼
Estoque
        │
        ▼
Conta a Pagar
        │
        ▼
Parcelas
        │
        ▼
Pagamento
        │
        ▼
Movimento Financeiro
```

---

## Fluxo Bancário

```text
Conta Bancária
        │
        ▼
Extrato Bancário
        │
        ▼
Lançamentos
        │
        ▼
Conciliação Bancária
        │
        ▼
Movimento Financeiro
```

---

# ⚙ Como Executar

## Pré-requisitos

- Java 25
- Maven Wrapper
- Docker Desktop
- PostgreSQL
- Redis
- RabbitMQ

---

## Clonar o projeto

```bash
git clone https://github.com/JotakuGr/invPlus.git
```

```bash
cd invPlus
```

---

## Backend

```bash
cd backend

./mvnw spring-boot:run
```

Windows

```powershell
.\mvnw.cmd spring-boot:run
```

---

## Executar os testes

```bash
./mvnw test
```

ou

```powershell
.\mvnw.cmd test
```

---

## Docker

```bash
docker compose up -d
```

---

# 🧪 Qualidade

O projeto utiliza uma suíte completa de testes automatizados.

### Tipos de testes

- Testes Unitários
- Testes REST
- Testes de Integração
- Business Flow
- Segurança
- Concorrência
- Performance

---

### Organização

```text
src/test/java

integration/

businessflow/

security/

performance/

concurrency/

modules/
```

---

### Ferramentas

- JUnit 5
- Mockito
- MockMvc
- Spring Boot Test

---

# 🔒 Segurança

O projeto possui:

- JWT Authentication
- Spring Security
- RBAC (Role Based Access Control)
- Permissões Granulares
- Auditoria
- Soft Delete
- Optimistic Lock
- GitHub CodeQL
- Dependency Review
- GitHub Actions

---

# 📚 Documentação

Toda a documentação do projeto encontra-se organizada em:

| Documento | Descrição |
|------------|-----------|
| docs/api | Endpoints REST |
| docs/arquitetura | Arquitetura do sistema |
| docs/banco | Banco de dados e migrations |
| docs/regras-negocio | Regras por módulo |
| docs/testes | Estratégia de testes |
| docs/roadmap | Roadmap do projeto |
| docs/diagramas | Diagramas Mermaid |
| docs/padroes | Padrões utilizados |
| docs/adr | Architecture Decision Records |

---

# 🗺 Roadmap

O desenvolvimento do **invPlus ERP** segue uma evolução contínua.

## Concluído

- ✅ Arquitetura Modular
- ✅ Spring Security + JWT
- ✅ Cadastros
- ✅ Pessoas
- ✅ Produtos
- ✅ Estoque
- ✅ Compras
- ✅ Vendas
- ✅ Financeiro
- ✅ Conta Bancária
- ✅ Extrato Bancário
- ✅ Fluxo de Caixa
- ✅ DRE
- ✅ Plano de Contas
- ✅ Centro de Custo
- ✅ Conciliação Bancária
- ✅ Importação de extratos OFX
- ✅ GitHub Actions
- ✅ Docker
- ✅ CodeQL
- ✅ Dependency Review
- ✅ Documentação Técnica

---

## Em desenvolvimento

- 🚧 Open Finance
- 🚧 Dashboard Gerencial
- 🚧 Indicadores Financeiros
- 🚧 API Pública
- 🚧 Frontend Angular

---

## Futuro

- 📱 Aplicativo Mobile
- ☁ Multiempresa Avançado
- 📊 Business Intelligence
- 🤖 Inteligência Artificial
- 📈 Analytics
- 🌎 Internacionalização
- 📄 Emissão Fiscal
- 📦 Integração com Marketplaces

---

# 🤝 Como Contribuir

Contribuições são muito bem-vindas!

Antes de abrir uma Pull Request, leia:

- 📖 [CONTRIBUTING.md](CONTRIBUTING.md)
- 🔒 [SECURITY.md](SECURITY.md)

Também utilize os templates disponíveis:

- Pull Request Template
- Bug Report
- Feature Request
- Task

---

# 🧪 Qualidade do Projeto

O projeto utiliza práticas modernas de desenvolvimento para garantir qualidade, segurança e estabilidade.

## Integração Contínua

- ✅ GitHub Actions
- ✅ Build Automatizado
- ✅ Docker Build
- ✅ CodeQL
- ✅ Dependency Review

## Testes

- ✅ Testes Unitários
- ✅ Testes REST
- ✅ Testes de Integração
- ✅ Business Flow
- ✅ Testes de Segurança
- ✅ Testes de Concorrência
- ✅ Testes de Performance

---

# 📈 Estatísticas do Projeto

| Indicador | Valor |
|-----------|------:|
| Java | 25 |
| Spring Boot | 3.x |
| Migrations Flyway | 119+ |
| Testes Automatizados | 657 |
| Controllers REST | 60+ |
| Entidades | 70+ |
| DTOs | 200+ |
| Services | 70+ |
| Arquitetura | Modular |
| Banco de Dados | PostgreSQL |
| Integração Contínua | GitHub Actions |

---

# 📄 Licença

Este projeto está licenciado sob os termos da **Licença MIT**.

Consulte o arquivo [LICENSE](LICENSE) para mais informações.

---

# 📬 Contato

**Autor**

Josiel Amorim

GitHub

https://github.com/JotakuGr

LinkedIn

> Adicione aqui o link do seu perfil do LinkedIn.

---

<div align="center">

## ⭐ Gostou do projeto?

Se este projeto foi útil para você:

⭐ Deixe uma estrela no repositório.

🐛 Reporte problemas utilizando as Issues.

💡 Sugira novas funcionalidades.

🤝 Contribua com melhorias.

---

### Desenvolvido com ❤️ utilizando Java, Spring Boot e boas práticas de engenharia de software.

<img src="docs/images/logo-invplus.png" width="110">

**invPlus ERP**

*Modern ERP Platform*

</div>
