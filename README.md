<div align="center">

<h1>
  <img src="https://img.shields.io/badge/inv-Plus-4F46E5?style=for-the-badge&labelColor=1E1B4B" alt="invPlus"/>
</h1>

<p><strong>Sistema ERP completo, modular e escalável para gestão empresarial</strong></p>

<p>
  <img src="https://img.shields.io/badge/status-em%20desenvolvimento-yellow?style=flat-square"/>
  <img src="https://img.shields.io/badge/versão-0.1.0--alpha-orange?style=flat-square"/>
  <img src="https://img.shields.io/badge/maturidade-pré--alpha-red?style=flat-square"/>
  <img src="https://img.shields.io/badge/licença-MIT-blue?style=flat-square"/>
</p>

<p>
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Angular-20-DD0031?style=flat-square&logo=angular&logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-18-336791?style=flat-square&logo=postgresql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-compose-2496ED?style=flat-square&logo=docker&logoColor=white"/>
</p>

</div>

> ⚠️ **Aviso de maturidade:** Este projeto está em fase **pré-alpha**. A arquitetura está sendo definida e nenhuma funcionalidade está em produção. Não utilize em ambiente produtivo.

---

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Objetivos](#objetivos)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Princípios arquiteturais](#princípios-arquiteturais)
- [Stack tecnológica](#stack-tecnológica)
- [Módulos](#módulos)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Banco de dados](#banco-de-dados)
- [Como executar](#como-executar)
- [Segurança e autenticação](#segurança-e-autenticação)
- [Testes](#testes)
- [Mudanças locais ainda não commitadas](#mudanças-locais-ainda-não-commitadas)
- [Ordem de desenvolvimento](#ordem-de-desenvolvimento)
- [Roadmap](#roadmap)
- [Padrão de desenvolvimento](#padrão-de-desenvolvimento)
- [Contribuindo](#contribuindo)
- [Licença](#licença)
- [Autor](#autor)

---

## Sobre o projeto

O **invPlus** é um sistema ERP em desenvolvimento, criado para centralizar a gestão empresarial em uma única plataforma. O sistema cobre desde o cadastro de produtos e controle de estoque até faturamento, financeiro, fiscal e CRM.

A arquitetura inicial é um **monólito modular**, organizado por domínios de negócio, com evolução planejada para microsserviços conforme o sistema amadurece.

---

## Objetivos

- ✦ Arquitetura modular, escalável e de fácil manutenção
- ✦ Aplicação rigorosa de boas práticas de engenharia de software
- ✦ Código limpo, testável e bem documentado
- ✦ Cobertura de testes automatizados desde o início
- ✦ Evolução planejada e controlada para microsserviços
- ✦ Documentação completa de domínio, API e banco de dados
- ✦ Base sólida para um ERP robusto e confiável

---

## Funcionalidades

| # | Funcionalidade | Status |
|---|---|---|
| 01 | Cadastro de produtos | ✅ Planejado |
| 02 | Controle de estoque | ✅ Planejado |
| 03 | Gestão de compras | ✅ Planejado |
| 04 | Gestão de vendas | ✅ Planejado |
| 05 | Faturamento | ✅ Planejado |
| 06 | Controle financeiro | ✅ Planejado |
| 07 | Emissão fiscal (NF-e) | ✅ Planejado |
| 08 | CRM | ✅ Planejado |
| 09 | Dashboard gerencial | ⬜ Futuro |
| 10 | Relatórios e BI | ⬜ Futuro |
| 11 | API Pública | ⬜ Futuro |
| 12 | Integração bancária | ⬜ Futuro |
| 13 | Integração com marketplace | ⬜ Futuro |
| 14 | Aplicativo mobile | ⬜ Futuro |

---

## Arquitetura

O invPlus adota um **monólito modular** como ponto de partida, garantindo organização e coesão interna antes de qualquer decisão de distribuição.

```
┌─────────────────────────────────────────────┐
│                  Usuário                    │
└─────────────────────┬───────────────────────┘
                      │ HTTPS
┌─────────────────────▼───────────────────────┐
│              Angular 20                     │
│         (SPA — TypeScript/SCSS)             │
└─────────────────────┬───────────────────────┘
                      │ REST / JSON
┌─────────────────────▼───────────────────────┐
│            Spring Boot 4.1                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │Controllers│  │ Services │  │  DTOs   │  │
│  └────┬─────┘  └────┬─────┘  └──────────┘  │
│       │              │                      │
│  ┌────▼──────────────▼──────────────────┐   │
│  │           Repositories (JPA)         │   │
│  └──────────────────────────────────────┘   │
│  ┌──────────┐  ┌──────────┐                 │
│  │ Security │  │  Flyway  │                 │
│  │   JWT    │  │Migrations│                 │
│  └──────────┘  └──────────┘                 │
└─────────────────────┬───────────────────────┘
                      │ JDBC
┌─────────────────────▼───────────────────────┐
│             PostgreSQL 18                   │
└─────────────────────────────────────────────┘

        Roadmap de infraestrutura:
        ┌────────┐    ┌───────────┐
        │ Redis  │    │ RabbitMQ  │
        │(cache) │    │(mensageria│
        └────────┘    └───────────┘
```

### Fluxo de uma requisição

```
Request → Controller → Service → Repository → Database
                   ↑         ↑
                  DTO      Domain Entity
```

---

## Princípios arquiteturais

| Princípio | Aplicação no projeto |
|---|---|
| **Domain-Driven Design (DDD)** | Organização por domínios de negócio (módulos) com linguagem ubíqua |
| **Clean Architecture** | Separação clara entre Controller, Service, Repository e Domain |
| **SOLID** | Aplicado na criação de classes, interfaces e injeção de dependência |
| **Modular Monolith** | Módulos independentes dentro de uma única aplicação |
| **RESTful API** | Endpoints seguindo convenções REST (verbos, status codes, recursos) |
| **Repository Pattern** | Abstração da camada de persistência via Spring Data JPA |
| **DTO Pattern** | Separação entre entidades de domínio e objetos de transferência |
| **Service Layer** | Toda regra de negócio centralizada na camada de serviço |
| **JWT Authentication** | Autenticação stateless com tokens assinados |

---

## Stack tecnológica

| Camada | Tecnologia | Responsabilidade |
|---|---|---|
| Backend | Java 25 + Spring Boot 4.1 | API REST e regras de negócio |
| Frontend | Angular 20 + TypeScript | Interface do usuário (SPA) |
| Banco de dados | PostgreSQL 18 | Persistência relacional |
| ORM | Spring Data JPA + Hibernate | Mapeamento objeto-relacional |
| Segurança | Spring Security + JWT | Login stateless, autenticação e autorização |
| Migrações | Flyway | Versionamento do schema e seeds iniciais |
| Validação | Bean Validation | Validação de dados de entrada |
| Cache | Spring Cache | Cache local inicial para cadastros de baixa alteração |
| Logs | SLF4J + Logback | Logging padronizado da API |
| Build | Maven | Gerenciamento de dependências |
| Containerização | Docker + Docker Compose | Ambiente isolado e reproduzível |
| Cache distribuído *(roadmap)* | Redis | Cache compartilhado entre instâncias |
| Mensageria *(roadmap)* | RabbitMQ | Comunicação assíncrona entre módulos |

---

## Módulos

| Módulo | Responsabilidade | Principais entidades |
|---|---|---|
| 🔐 **Segurança** | Autenticação, perfis e auditoria | Usuário, Perfil, Permissão, Auditoria |
| 🏢 **Cadastros** | Dados base utilizados por outros módulos | País, Estado, Cidade, Endereço, Empresa, Filial, Categoria, Marca, UoM, Arquivo |
| 👥 **Pessoas** | Cadastro unificado de pessoas | Pessoa, PessoaRole, Cliente, Fornecedor, Transportadora, Funcionário, ContatoPessoa, DocumentoPessoa |
| 📦 **Produtos** | Catálogo e precificação | Produto, Tabela de Preço, Produto Fornecedor |
| 🏪 **Estoque** | Saldos, movimentações e inventário | Depósito, Estoque, Movimentação, Lote, Inventário |
| 🛒 **Compras** | Aquisição de produtos | Pedido de Compra, Itens, Recebimento, Histórico |
| 💼 **Vendas** | Processo comercial | Pedido de Venda, Itens, Comissão, Histórico |
| 💳 **Pagamentos** | Formas e condições de pagamento | Forma de Pagamento, Condição, Parcelas |
| 🧾 **Faturamento** | Geração de faturas | Fatura, Parcelas da Fatura |
| 💰 **Financeiro** | Fluxo de caixa e contas | Conta Receber/Pagar, Movimento Financeiro, Centro de Custo |
| 🗂️ **Fiscal** | Notas fiscais e tributação | Nota Fiscal, Itens, Impostos |
| 🤝 **CRM** | Relacionamento com clientes | Lead, Oportunidade, Atendimento, Ordem de Serviço |

---

## Estrutura do projeto

```
invPlus/
├── backend/
│   └── src/
│       ├── main/
│       │   ├── java/br/com/invplus/
│       │   │   ├── config/            # Configurações globais
│       │   │   ├── common/            # Constantes e enums comuns
│       │   │   ├── shared/            # BaseEntity, respostas e exceções
│       │   │   ├── security/          # JWT, filtros, entidades e login
│       │   │   └── modules/
│       │   │       ├── cadastros/
│       │   │       ├── pessoas/
│       │   │       ├── produtos/
│       │   │       ├── estoque/
│       │   │       ├── compras/
│       │   │       ├── vendas/
│       │   │       ├── pagamentos/
│       │   │       ├── faturamento/
│       │   │       ├── financeiro/
│       │   │       ├── fiscal/
│       │   │       └── crm/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── META-INF/
│       └── test/
│           ├── java/                  # Testes de contexto, auth e repositories
│           └── resources/
├── frontend/
│   └── src/
│       ├── app/
│       │   └── modules/               # Espelho dos módulos backend
│       ├── assets/
│       └── environments/
├── database/
│   ├── migrations/database/           # Migrations Flyway executadas pelo backend
│   ├── seeds/                         # Reservado para seeds auxiliares
│   └── scripts/                       # Scripts utilitários
├── docker/
│   ├── postgres/
│   ├── redis/
│   ├── rabbitmq/
│   └── compose/
├── docs/
│   ├── arquitetura/
│   ├── api/
│   ├── der/                           # DER em PDF
│   ├── requisitos/
│   ├── regras-negocio/
│   └── sprint/
├── docker/compose/docker-compose.yml
└── README.md
```

### Estrutura interna padrão de cada módulo

```
modules/
├── cadastros/
├── pessoas/
├── produtos/
├── estoque/
├── compras/
├── vendas/
├── pagamentos/
├── faturamento/
├── financeiro/
├── fiscal/
└── crm/
```

Cada módulo segue o mesmo contrato:

```text
controller/       # Endpoints REST
dto/request/      # Payloads de entrada
dto/response/     # Payloads de saída
entity/           # Entidades JPA
mapper/           # Conversões entre entity/dto
repository/       # Repositories Spring Data
service/          # Regras de negócio
specification/    # Filtros dinâmicos e consultas compostas
validator/        # Validações específicas do módulo
exception/        # Exceções específicas do módulo
```

### Estrutura atual do módulo de segurança

```
security/
├── config/               # SecurityConfig
├── controller/           # AuthController
├── dto/                  # LoginRequest e LoginResponse
├── entity/               # Usuario, Perfil, Permissao e Auditoria
├── enums/                # Acoes de permissao e auditoria
├── filter/               # JwtAuthenticationFilter
├── repository/           # Repositories Spring Data JPA
└── service/              # Login, JWT e UserDetailsService
```

---

## Banco de dados

O PostgreSQL é o banco principal, evoluído com **Flyway** via migrations versionadas em `database/migrations/database`.

```
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

### Migration V1

Habilita extensões PostgreSQL usadas pelo módulo de segurança:

- `pgcrypto`, para geração de UUID e hash BCrypt no seed inicial.
- `citext`, para email case-insensitive em `usuario.email`.

### Migration V2

Cria o schema inicial de segurança:

- `usuario`
- `perfil`
- `permissao`
- `usuario_perfil`
- `perfil_permissao`
- `auditoria`

Também cria índices, constraints, comentários e trigger para atualização automática de `usuario.atualizado_em`.

### Migration V3

Cria o seed inicial de segurança:

- perfil `ADMIN`;
- permissões básicas para `SEGURANCA`, `USUARIO`, `PERFIL` e `PERMISSAO`;
- usuário inicial `admin@invplus.com`;
- vínculos entre usuário, perfil e permissões.

> O seed usa a senha inicial `admin123`, gravada como hash BCrypt no banco. Antes de produção, a estratégia recomendada é exigir troca de senha no primeiro login ou substituir esse bootstrap por variável de ambiente segura.

### Migrations V4, V5 e V6

Evoluem o fluxo de autenticação com refresh token:

- `refresh_token`, persistindo apenas hash SHA-256 do token opaco;
- auditoria de eventos de refresh token;
- ajuste de compatibilidade do campo `ip`.

### Migration V7

Cria o schema inicial do módulo Cadastros:

- `empresa`
- `endereco`
- `categoria`
- `marca`
- `unidade_medida`
- `arquivo`

Também cria índices, constraints, triggers de `atualizado_em`, comentários SQL e permissões `CADASTROS` vinculadas ao perfil `ADMIN`.

### Migration V8

Ajusta `endereco.uf` para `VARCHAR(2)`, alinhando o tipo do banco ao mapeamento JPA validado pelo Hibernate.

### Migration V9

Consolida a primeira fase do Sprint 4 - Cadastros Fundamentais:

- adiciona `version` nas tabelas auditadas para controle de concorrência otimista com `@Version`;
- cria `pais`, `estado`, `cidade` e `filial`;
- relaciona `endereco` com `cidade`;
- cria índices simples e compostos para consultas por hierarquia e registros ativos;
- adiciona triggers de `atualizado_em` nas novas tabelas.

### Migration V10

Consolida a base da fase 2 de Cadastros com tabelas idempotentes para:

- `unidade_medida`
- `categoria`
- `marca`
- `empresa`
- `arquivo`

No fluxo atual, `UnidadeMedida` foi a primeira entidade concluída com checklist completa.

### Migration V11

Adiciona permissões específicas por entidade da fase 2:

- `UNIDADE_MEDIDA:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`
- `CATEGORIA:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`
- `MARCA:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`
- `EMPRESA:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`
- `ARQUIVO:CRIAR`, `VISUALIZAR`, `EDITAR`, `EXCLUIR`

### Migration V12

Ajusta `categoria` para as regras finais da fase 2:

- adiciona `categoria_pai_id` para hierarquia opcional;
- remove a unicidade antiga por `nome/tipo`;
- cria índice único parcial por `LOWER(nome)` apenas para registros ativos e não excluídos;
- cria índice para consultas por categoria pai;
- adiciona FK para a própria tabela e check para impedir auto-relacionamento direto.

### Migration V13

Adiciona a permissão wildcard `CATEGORIA:*` e vincula ao perfil `ADMIN`, mantendo também as permissões granulares de criar, visualizar, editar e excluir.

### Migration V14

Refina `categoria` e `marca`:

- adiciona `categoria.ordem` para ordenação previsível da árvore;
- cria índice parcial para listar categorias por pai, ordem e nome;
- adiciona `marca.site` e `marca.fabricante`;
- troca a unicidade antiga de `marca.nome` por índice único parcial em `LOWER(nome)` apenas para marcas ativas e não excluídas.

### Migration V15

Adiciona a permissão wildcard `MARCA:*` e vincula ao perfil `ADMIN`.

### Migration V16

Refina `empresa` para a fase 2:

- adiciona `inscricao_municipal`, `regime_tributario` e `crt`;
- garante `version` para concorrência otimista;
- troca a unicidade antiga de `cnpj` por índice único parcial apenas para empresas ativas e não excluídas;
- adiciona índices para `cnpj` e registros ativos;
- adiciona constraint para CRT válido.

### Migration V17

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `EMPRESA:*`
- `EMPRESA:CRIAR`
- `EMPRESA:VISUALIZAR`
- `EMPRESA:EDITAR`
- `EMPRESA:EXCLUIR`

### Migration V18

Refina `arquivo` para a fase 2:

- adiciona `tipo_mime`, `extensao`, `hash_sha256`, `url` e `storage`;
- prepara armazenamento em `LOCAL`, `S3`, `MINIO`, `AZURE` e `GOOGLE_CLOUD`;
- adiciona índices para registros ativos, hash SHA-256 e vínculo `entidade/entidade_id`;
- adiciona checks para tamanho positivo, storage válido e formato do hash SHA-256.

O upload fisico de arquivos foi preparado com a camada `shared/storage`, provider local inicial e suporte futuro para Cloudflare R2. Em desenvolvimento, arquivos locais podem ser publicados em `/uploads/**`.

### Migration V19

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `ARQUIVO:*`
- `ARQUIVO:CRIAR`
- `ARQUIVO:VISUALIZAR`
- `ARQUIVO:EDITAR`
- `ARQUIVO:EXCLUIR`

### Migration V20

Torna opcionais as colunas legadas `content_type` e `caminho`, preservando compatibilidade com bases antigas e permitindo o contrato novo com `tipo_mime` e `url`.

### Migration V21

Cria a tabela `pessoa`, base do módulo Pessoas:

- `tipo_pessoa` obrigatório com valores `FISICA` ou `JURIDICA`;
- `cpf_cnpj` obrigatório, normalizado apenas com números;
- unicidade parcial de `cpf_cnpj` apenas entre registros ativos;
- índices para registros ativos e busca por documento;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V22

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `PESSOAS:*`
- `PESSOA:*`
- `PESSOA:CRIAR`
- `PESSOA:VISUALIZAR`
- `PESSOA:EDITAR`
- `PESSOA:EXCLUIR`

### Migration V23

Cria a tabela `cliente`, representando o papel comercial de uma `pessoa`:

- `pessoa_id` obrigatório com vínculo para `pessoa`;
- `limite_credito` monetário com valor mínimo zero;
- `condicao_pagamento_id` e `tabela_preco_id` como UUIDs preparados para relacionamentos futuros;
- unicidade parcial de `pessoa_id` apenas entre clientes ativos e não excluídos;
- índices para busca por pessoa e listagem de registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V24

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `CLIENTE:*`
- `CLIENTE:CRIAR`
- `CLIENTE:VISUALIZAR`
- `CLIENTE:EDITAR`
- `CLIENTE:EXCLUIR`

### Migration V25

Cria a tabela `fornecedor`, representando o papel comercial de fornecimento de uma `pessoa`:

- `pessoa_id` obrigatório com vínculo para `pessoa`;
- `prazo_medio_entrega` com valor mínimo zero;
- `condicao_pagamento_id` como UUID preparado para relacionamento futuro;
- unicidade parcial de `pessoa_id` apenas entre fornecedores ativos e não excluídos;
- índices para busca por pessoa e listagem de registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V26

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `FORNECEDOR:*`
- `FORNECEDOR:CRIAR`
- `FORNECEDOR:VISUALIZAR`
- `FORNECEDOR:EDITAR`
- `FORNECEDOR:EXCLUIR`

### Migration V27

Cria a tabela `funcionario`, representando o papel trabalhista de uma `pessoa`:

- `pessoa_id` obrigatório com vínculo para `pessoa`;
- `usuario_id` opcional com vínculo para `usuario`;
- unicidade parcial de `pessoa_id` apenas entre funcionários ativos e não excluídos;
- unicidade parcial de `usuario_id` apenas quando informado e ativo;
- `cargo` obrigatório, `salario` não negativo e `data_admissao` obrigatória;
- `data_demissao` opcional, mas nunca anterior à admissão;
- índices para pessoa, usuário e registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V28

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `FUNCIONARIO:*`
- `FUNCIONARIO:CRIAR`
- `FUNCIONARIO:VISUALIZAR`
- `FUNCIONARIO:EDITAR`
- `FUNCIONARIO:EXCLUIR`

### Migration V29

Cria a tabela `transportadora`, representando o papel logístico de uma `pessoa`:

- `pessoa_id` obrigatório com vínculo para `pessoa`;
- `antt` e `tipo_veiculo_principal` opcionais;
- `capacidade_carga_kg` com valor mínimo zero;
- unicidade parcial de `pessoa_id` apenas entre transportadoras ativas e não excluídas;
- índices para busca por pessoa e listagem de registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V30

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `TRANSPORTADORA:*`
- `TRANSPORTADORA:CRIAR`
- `TRANSPORTADORA:VISUALIZAR`
- `TRANSPORTADORA:EDITAR`
- `TRANSPORTADORA:EXCLUIR`

### Migration V31

Cria a tabela auxiliar `pessoa_role`, deixando explicito que `Pessoa` e o aggregate root do modulo Pessoas:

- registra papeis ativos como `CLIENTE`, `FORNECEDOR`, `FUNCIONARIO` e `TRANSPORTADORA`;
- mantem coexistencia com as tabelas especificas de cada papel;
- aplica unicidade parcial por `pessoa_id` e `role` apenas para registros ativos;
- usa auditoria, `version`, `ativo` e `deleted_at`.

### Migration V32

Cria a tabela `contato_pessoa`, permitindo multiplos contatos operacionais por Pessoa:

- vinculo obrigatorio com `pessoa`;
- campos para nome, telefone, celular, email, cargo, tipo, principal e observacao;
- email com `citext`;
- indices para pessoa, email e contato principal;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V33

Adiciona e vincula ao perfil `ADMIN` as permissoes:

- `CONTATO_PESSOA:*`
- `CONTATO_PESSOA:CRIAR`
- `CONTATO_PESSOA:VISUALIZAR`
- `CONTATO_PESSOA:EDITAR`
- `CONTATO_PESSOA:EXCLUIR`

### Migration V34

Cria a tabela `documento_pessoa`, permitindo registrar documentos cadastrais de uma Pessoa:

- vinculo obrigatorio com `pessoa`;
- vinculo opcional com `arquivo`;
- tipo do documento com valores controlados por enum;
- numero obrigatorio;
- orgao emissor, data de emissao, data de validade e observacao opcionais;
- bloqueio de validade anterior a emissao;
- unicidade parcial por pessoa, tipo e numero apenas entre registros ativos;
- indices para pessoa, arquivo e registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V35

Adiciona e vincula ao perfil `ADMIN` as permissoes:

- `DOCUMENTO_PESSOA:*`
- `DOCUMENTO_PESSOA:CRIAR`
- `DOCUMENTO_PESSOA:VISUALIZAR`
- `DOCUMENTO_PESSOA:EDITAR`
- `DOCUMENTO_PESSOA:EXCLUIR`

### Migration V36

Cria a tabela `produto`, centro do modulo Produtos:

- codigo e nome obrigatorios;
- vinculo obrigatorio com `categoria` e `unidade_medida`;
- vinculo opcional com `marca`;
- campos de SKU, codigo interno, codigo de barras, descricao, precos, pesos, dimensoes e estoque minimo;
- controles `controla_lote`, `controla_validade` e `permite_fracionamento`;
- unicidade parcial de codigo, SKU e codigo de barras apenas entre produtos ativos;
- checks para precos, pesos, dimensoes e estoque minimo nao negativos;
- indices para categoria, marca, unidade e registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V37

Adiciona e vincula ao perfil `ADMIN` as permissoes:

- `PRODUTO:*`
- `PRODUTO:CRIAR`
- `PRODUTO:VISUALIZAR`
- `PRODUTO:EDITAR`
- `PRODUTO:EXCLUIR`

### Migration V38

Cria a tabela `produto_fornecedor`, usada por compras, cotacoes, entrada de mercadoria e historico de custo:

- vinculo obrigatorio com `produto`;
- vinculo obrigatorio com `fornecedor`;
- codigo do produto no fornecedor;
- preco do fornecedor;
- prazo de entrega em dias;
- marcador de fornecedor principal;
- unicidade ativa para `produto_id + fornecedor_id`;
- unicidade parcial permitindo apenas um fornecedor principal ativo por produto;
- checks para preco e prazo nao negativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V39

Adiciona e vincula ao perfil `ADMIN` as permissoes:

- `PRODUTO_FORNECEDOR:*`
- `PRODUTO_FORNECEDOR:CRIAR`
- `PRODUTO_FORNECEDOR:VISUALIZAR`
- `PRODUTO_FORNECEDOR:EDITAR`
- `PRODUTO_FORNECEDOR:EXCLUIR`

### Migration V40

Melhora `produto_fornecedor` para processos de compras e cria historico de custos:

- renomeia `prazo_entrega_dias` para `lead_time_dias`;
- adiciona `prioridade`;
- adiciona `status` com `HOMOLOGADO`, `PREFERENCIAL`, `EVENTUAL` e `BLOQUEADO`;
- cria indice por prioridade para selecao do proximo fornecedor;
- cria tabela `produto_fornecedor_preco`;
- registra `preco`, `vigencia_inicio` e `vigencia_fim`;
- impede duplicidade ativa de preco na mesma vigencia inicial;
- valida preco nao negativo e vigencia final posterior ou igual a inicial.

### Migration V41

Adiciona e vincula ao perfil `ADMIN` as permissoes:

- `PRODUTO_FORNECEDOR_PRECO:*`
- `PRODUTO_FORNECEDOR_PRECO:CRIAR`
- `PRODUTO_FORNECEDOR_PRECO:VISUALIZAR`
- `PRODUTO_FORNECEDOR_PRECO:EDITAR`
- `PRODUTO_FORNECEDOR_PRECO:EXCLUIR`

### Migration V42

Cria a tabela `produto_fiscal`, separando dados tributarios do cadastro comercial do produto:

- vinculo obrigatorio e unico ativo com `produto`;
- `ncm` obrigatorio com 8 digitos;
- `cest` opcional com 7 digitos quando informado;
- `origem_mercadoria` controlada por enum;
- campos fiscais para CFOP, CST ICMS, CSOSN, CST PIS e CST COFINS;
- aliquotas de ICMS, IPI, PIS e COFINS nao negativas;
- indices para produto, NCM e registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V43

Adiciona e vincula ao perfil `ADMIN` as permissoes:

- `PRODUTO_FISCAL:*`
- `PRODUTO_FISCAL:CRIAR`
- `PRODUTO_FISCAL:VISUALIZAR`
- `PRODUTO_FISCAL:EDITAR`
- `PRODUTO_FISCAL:EXCLUIR`

### Migration V44

Adiciona classificacao operacional ao cadastro de Produto:

- `tipo_produto`, com valores `PRODUTO`, `SERVICO`, `MATERIA_PRIMA`, `INSUMO`, `EMBALAGEM`, `COMBUSTIVEL`, `ATIVO_IMOBILIZADO` e `USO_CONSUMO`;
- `natureza_produto`, com valores `VENDA`, `COMPRA` e `AMBOS`;
- defaults conservadores `PRODUTO` e `AMBOS` para manter compatibilidade com registros e payloads existentes;
- constraint de domínio para os dois enums;
- índice parcial `tipo_produto/natureza_produto` para listagens de catálogo.

### Migration V45

Adiciona estado operacional e controles explicitos ao Produto:

- `status_produto`, com valores `EM_DESENVOLVIMENTO`, `ATIVO`, `DESCONTINUADO`, `BLOQUEADO` e `SUSPENSO`;
- `controla_estoque`, separando produto/servico que movimenta estoque de item cadastral que nao movimenta;
- `permite_compra`, para habilitar fluxos de compras independentemente da natureza macro;
- `permite_venda`, para habilitar fluxos de venda independentemente da natureza macro;
- defaults `ATIVO`, `true`, `true` e `true` para manter compatibilidade com produtos existentes;
- indices para status e controles operacionais.

### Migration V46

Prepara `arquivo.storage` para Cloudflare R2:

- adiciona `CLOUDFLARE_R2` aos provedores aceitos pela constraint `chk_arquivo_storage`;
- mantém compatibilidade com `LOCAL`, `S3`, `MINIO`, `AZURE` e `GOOGLE_CLOUD`;
- atualiza o comentário da coluna `storage`.

### Migration V47

Cria a tabela `produto_imagem` para vincular Produto e Arquivo:

- `produto_id` obrigatório;
- `arquivo_id` obrigatório;
- `principal` para imagem principal do produto;
- `ordem` para ordenação visual;
- `descricao` opcional;
- unicidade ativa para `produto_id + arquivo_id`;
- unicidade parcial permitindo apenas uma imagem principal ativa por produto;
- check para `ordem >= 0`;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V48

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `PRODUTO_IMAGEM:*`
- `PRODUTO_IMAGEM:CRIAR`
- `PRODUTO_IMAGEM:VISUALIZAR`
- `PRODUTO_IMAGEM:EDITAR`
- `PRODUTO_IMAGEM:EXCLUIR`

### Migration V49

Cria a tabela `produto_codigo_barras` para permitir múltiplos códigos por produto:

- `produto_id` obrigatório;
- `tipo_codigo` com valores `EAN_UNIDADE`, `EAN_CAIXA`, `EAN_PACOTE`, `GTIN_LOGISTICO` e `CODIGO_INTERNO_ALTERNATIVO`;
- `codigo` obrigatório;
- `principal` para indicar o código preferencial do produto;
- `observacao` opcional;
- unicidade ativa global para `codigo`;
- unicidade ativa para `produto_id + codigo`;
- unicidade parcial permitindo apenas um código principal ativo por produto;
- índices para produto, tipo e registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V50

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `PRODUTO_CODIGO_BARRAS:*`
- `PRODUTO_CODIGO_BARRAS:CRIAR`
- `PRODUTO_CODIGO_BARRAS:VISUALIZAR`
- `PRODUTO_CODIGO_BARRAS:EDITAR`
- `PRODUTO_CODIGO_BARRAS:EXCLUIR`

### Migration V51

Cria a tabela `produto_preco` para histórico de preços comerciais:

- `produto_id` obrigatório;
- `empresa_id` opcional, preparando precificação multiempresa;
- `tipo_preco` com valores `CUSTO`, `VENDA`, `ATACADO`, `VAREJO`, `PROMOCIONAL` e `ESPECIAL`;
- `preco` com `NUMERIC(15,4)`;
- `vigencia_inicio` obrigatória;
- `vigencia_fim` opcional;
- `principal` para preço preferencial por produto/tipo/empresa;
- unicidade ativa para produto + tipo + empresa + vigência inicial;
- unicidade parcial permitindo apenas um preço principal ativo por produto/tipo/empresa;
- índices para produto, empresa, tipo, vigência e registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V52

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `PRODUTO_PRECO:*`
- `PRODUTO_PRECO:CRIAR`
- `PRODUTO_PRECO:VISUALIZAR`
- `PRODUTO_PRECO:EDITAR`
- `PRODUTO_PRECO:EXCLUIR`

### Migration V53

Cria a tabela `produto_unidade` para conversão de unidades do produto:

- `produto_id` obrigatório;
- `unidade_medida_id` obrigatório;
- `fator_conversao` maior que zero;
- `unidade_padrao` para unidade base de conversão;
- flags `permite_compra`, `permite_venda` e `permite_estoque`;
- `observacao` opcional;
- unicidade ativa para produto + unidade;
- unicidade parcial permitindo apenas uma unidade padrão ativa por produto;
- check exigindo fator `1` quando `unidade_padrao = true`;
- índices para produto, unidade e registros ativos;
- auditoria, `version`, `ativo` e `deleted_at`.

### Migration V54

Adiciona e vincula ao perfil `ADMIN` as permissões:

- `PRODUTO_UNIDADE:*`
- `PRODUTO_UNIDADE:CRIAR`
- `PRODUTO_UNIDADE:VISUALIZAR`
- `PRODUTO_UNIDADE:EDITAR`
- `PRODUTO_UNIDADE:EXCLUIR`

### Migration V55

Refina `produto_unidade` com a flag `permite_fracionamento`, separando a regra de venda fracionada das flags de compra, venda e estoque:

- adiciona `permite_fracionamento` com default `false`;
- cria índice parcial para consultas de unidades fracionáveis ativas;
- mantém compatibilidade com registros já existentes.

Convenções do banco:
- PKs do tipo `UUID` com `gen_random_uuid()`
- Campos de auditoria em todas as tabelas: `criado_em`, `atualizado_em`, `criado_por`, `atualizado_por`
- Controle de concorrência otimista com `version` nas entidades que herdam `BaseEntity`
- Auditoria automática via Spring Data JPA Auditing, usando o UUID do usuário autenticado
- Soft delete em entidades que exigem exclusão lógica, com `ativo BOOLEAN` e/ou `deleted_at`
- Consultas padrão de entidades com soft delete retornam apenas `ativo = true` e `deleted_at IS NULL`
- Listagens REST retornam `Page<T>` e aceitam `page`, `size` e `sort`
- `NUMERIC(15,4)` para preços e `NUMERIC(14,3)` para quantidades
- `TIMESTAMPTZ` para todos os campos de data/hora

### Infraestrutura compartilhada de domínio

O backend possui bases reutilizáveis para evitar repetição nos próximos módulos:

- `BaseRepository<T>`: herda `JpaRepository` e `JpaSpecificationExecutor`, além de métodos padrão para registros ativos.
- `BaseCrudService<T, REQ, RES>`: concentra listar, buscar, criar, atualizar e excluir logicamente.
- `BaseCrudController`: centraliza helpers de resposta REST.
- `CrudMapper<T, REQ, RES>`: contrato para mapeamento entre request, entidade e response.
- `shared/events`: ponto inicial para eventos de domínio.
- `shared/domain/valueobject`: objetos de valor iniciais como `Money`, `Email`, `Cnpj` e `Cep`.

---

## Como executar

### Pré-requisitos

- Docker e Docker Compose instalados
- Java 25
- Maven 3.9+
- Node.js 20+ (para o frontend)

### Ambiente completo

1. Prepare as variáveis de infraestrutura local:

```bash
cd docker/compose
cp .env.example .env
```

Edite `docker/compose/.env` e substitua os valores `change-me`.

2. Suba os serviços de infraestrutura:

```bash
docker compose --env-file docker/compose/.env -f docker/compose/docker-compose.yml up -d
```

3. Configure as variáveis usadas pelo backend:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/invplus
SPRING_DATASOURCE_USERNAME=invplus_user
SPRING_DATASOURCE_PASSWORD=<senha_do_postgres>
INVPLUS_JWT_SECRET=<segredo_forte_para_jwt>
INVPLUS_JWT_EXPIRATION_MINUTES=60
INVPLUS_JWT_REFRESH_EXPIRATION_DAYS=7
```

No PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/invplus"
$env:SPRING_DATASOURCE_USERNAME="invplus_user"
$env:SPRING_DATASOURCE_PASSWORD="<senha_do_postgres>"
$env:INVPLUS_JWT_SECRET="<segredo_forte_para_jwt>"
$env:INVPLUS_JWT_EXPIRATION_MINUTES="60"
$env:INVPLUS_JWT_REFRESH_EXPIRATION_DAYS="7"
```

4. Execute o backend:

```bash
# Clonar o repositório
git clone https://github.com/seu-usuario/invplus.git
cd invplus

# Backend
cd backend
./mvnw spring-boot:run
```

5. Execute o frontend quando ele estiver configurado:

```bash
cd frontend
npm install
ng serve
```

### Acessos locais (após configuração)

| Serviço | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API REST | http://localhost:8080 |
| PostgreSQL | localhost:5433 |
| pgAdmin | http://localhost:8081 |
| RabbitMQ Management | http://localhost:15672 |

---

## Segurança e autenticação

O backend já possui um fluxo inicial de autenticação real com Spring Security + JWT.

### Endpoint de login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Payload:

```json
{
  "email": "admin@invplus.com",
  "senha": "admin123"
}
```

Resposta esperada:

```json
{
  "tokenType": "Bearer",
  "accessToken": "...",
  "refreshToken": "...",
  "expiresAt": "2026-07-01T00:00:00Z",
  "usuario": {
    "id": "...",
    "nome": "Administrador",
    "email": "admin@invplus.com",
    "perfis": ["ADMIN"],
    "permissoes": ["SEGURANCA:*"]
  }
}
```

Para acessar rotas protegidas:

```http
Authorization: Bearer <accessToken>
```

### Refresh token

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "..."
}
```

O refresh token e salvo no banco apenas como hash SHA-256. O token puro aparece somente na resposta da API.

### Logout

```http
POST /api/v1/auth/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "..."
}
```

O logout revoga o refresh token informado.

### Usuario autenticado

```http
GET /api/v1/auth/me
Authorization: Bearer <accessToken>
```

Esse endpoint exige a permissao `SEGURANCA:*` e serve como primeira validacao real de `@PreAuthorize`.

### Componentes implementados

- `SecurityConfig`: configuração stateless, rotas públicas e rotas autenticadas.
- `AuthController`: endpoints `/login`, `/refresh`, `/logout` e `/me`.
- `SecurityService`: autenticação, refresh token, logout e atualização de `ultimoLogin`.
- `RefreshTokenService`: cria, valida e revoga refresh tokens persistidos como hash.
- `UsuarioDetailsService`: carrega usuários reais do banco.
- `JwtService`: geração e validação de JWT HMAC SHA-256.
- `JwtAuthenticationFilter`: autenticação por header `Authorization`.
- `GlobalExceptionHandler`: trata credenciais inválidas com `401`, acesso negado com `403`, duplicidades com `409` e validações com `400`.

### Tratamento de erros

O backend centraliza erros em `GlobalExceptionHandler` e responde sempre no formato `ErrorResponse`.

- `400 Bad Request`: validações de entrada, `BusinessException` e `InvalidOperationException`.
- `409 Conflict`: duplicidades e conflitos de estado via `ConflictException`.
- `409 Conflict`: conflito de concorrência otimista via `OptimisticLockException` ou `ObjectOptimisticLockingFailureException`.
- `404 Not Found`: registros não encontrados ou excluídos logicamente.
- `401 Unauthorized`: token ausente/inválido ou credenciais inválidas.
- `403 Forbidden`: usuário autenticado sem permissão.

### Observações de segurança

- `INVPLUS_JWT_SECRET` é obrigatório no profile principal.
- `INVPLUS_JWT_REFRESH_EXPIRATION_DAYS` controla a validade do refresh token.
- O seed inicial usa `admin123` apenas para bootstrap.
- O `docker-compose.yml` não contém mais senhas fixas; use `docker/compose/.env`.

---

## Testes

Testes existentes:

- `BackendApplicationTests`: valida o carregamento do contexto Spring.
- `SecuritySeedRepositoryTests`: consulta o seed inicial via repositories.
- `AuthControllerTests`: valida login, senha inválida, usuário inexistente, refresh token, logout, token inválido, token expirado, rota protegida e permissão negada.
- `CadastrosRepositoryTests`: valida persistência das entidades base, permissão `CADASTROS:*` e ocultação de registros excluídos logicamente.
- `CadastrosControllerTests`: valida criação/consulta de empresa via API, bloqueio sem token, ocultação de empresa excluída logicamente e criação da hierarquia País → Estado → Cidade → Endereço → Filial.
- `UnidadeMedidaServiceTest`: valida regras unitárias de criação, duplicidade, paginação e exclusão lógica.
- `UnidadeMedidaControllerTests`: valida CRUD completo, sigla duplicada, paginação, soft delete, `401` sem token e `403` sem permissão.
- `CategoriaServiceTest`: valida criação com pai opcional, ordem, caminho completo, nome único ativo, paginação, soft delete, bloqueio de auto-relacionamento e ciclo hierárquico.
- `CategoriaControllerTests`: valida CRUD completo, hierarquia, nome duplicado, paginação, soft delete, `401` sem token e `403` sem permissão.
- `MarcaServiceTest`: valida criação, campos opcionais, nome único ativo, paginação e exclusão lógica.
- `MarcaControllerTests`: valida CRUD completo, nome duplicado, paginação, soft delete, `401` sem token e `403` sem permissão.
- `EmpresaServiceTest`: valida criação, CNPJ único ativo, CNPJ inválido, email inválido, paginação e exclusão lógica.
- `EmpresaControllerTests`: valida CRUD completo, CNPJ duplicado, paginação, soft delete, `401` sem token, `403` sem permissão, email inválido e CNPJ inválido.
- `ArquivoServiceTest`: valida criação, hash SHA-256 único ativo, hash inválido, paginação, exclusão lógica, upload válido, arquivo vazio, tipo MIME não permitido, limite de 10 MB e metadados salvos.
- `ArquivoControllerTests`: valida CRUD completo, upload multipart, hash duplicado, paginação, filtro por entidade, soft delete, `401` sem token, `403` sem permissão, hash inválido, arquivo vazio, tipo não permitido e limite de tamanho.
- `PessoaServiceTest`: valida pessoa física, pessoa jurídica, CPF/CNPJ único ativo, CPF/CNPJ imutável após criação, documento incompatível com tipo, paginação e exclusão lógica.
- `PessoaControllerTests`: valida CRUD completo, pessoa jurídica, CPF/CNPJ duplicado, bloqueio de alteração de CPF/CNPJ, paginação, soft delete, `401` sem token, `403` sem permissão e documentos incompatíveis com o tipo.
- `ClienteServiceTest`: valida criação de cliente, pessoa inexistente/inativa, duplicidade por pessoa, limite de crédito negativo, busca, paginação, atualização e exclusão lógica.
- `ClienteControllerTests`: valida CRUD completo, paginação, soft delete, `401` sem token, `403` sem permissão, pessoa inexistente e limite de crédito inválido.
- `FornecedorServiceTest`: valida criação de fornecedor, pessoa inexistente/inativa, duplicidade por pessoa, prazo médio negativo, busca, paginação, atualização e exclusão lógica.
- `FornecedorControllerTests`: valida CRUD completo, paginação, soft delete, `401` sem token, `403` sem permissão, pessoa inexistente e prazo médio inválido.
- `FuncionarioServiceTest`: valida criação com e sem usuário, duplicidade por pessoa/usuário, salário negativo, datas inválidas, busca, paginação, atualização e exclusão lógica.
- `FuncionarioControllerTests`: valida CRUD completo, paginação, soft delete, `401` sem token, `403` sem permissão, duplicidade por pessoa/usuário, pessoa inexistente, salário inválido e datas inválidas.
- `TransportadoraServiceTest`: valida criação de transportadora, pessoa inexistente/inativa, duplicidade por pessoa, capacidade de carga negativa, busca, paginação, atualização e exclusão lógica.
- `TransportadoraControllerTests`: valida CRUD completo, paginação, soft delete, `401` sem token, `403` sem permissão, pessoa inexistente e capacidade de carga inválida.
- `ContatoPessoaServiceTest`: valida criação de contato, pessoa inexistente/inativa, nome obrigatório, email inválido, busca, paginação, atualização e exclusão lógica.
- `ContatoPessoaControllerTests`: valida CRUD completo, paginação, soft delete, `401` sem token, `403` sem permissão, pessoa inexistente, nome obrigatório e email inválido.
- `DocumentoPessoaServiceTest`: valida criação sem arquivo, criação com arquivo, pessoa inexistente/inativa, arquivo inexistente/inativo, documento duplicado, datas inválidas, busca, paginação, atualização e exclusão lógica.
- `DocumentoPessoaControllerTests`: valida CRUD completo, criação com arquivo, paginação, soft delete, `401` sem token, `403` sem permissão, pessoa inexistente, arquivo inexistente, duplicidade e validade anterior à emissão.
- `ProdutoServiceTest`: valida criação de produto, referências inexistentes/inativas, duplicidades de código/SKU/código de barras, código de barras inválido, filtros avançados, busca rápida, preço negativo, busca, paginação, atualização e exclusão lógica.
- `ProdutoControllerTests`: valida CRUD completo, filtros por nome/código/SKU/código de barras/categoria/marca/ativo, busca rápida, paginação, soft delete, `401` sem token, `403` sem permissão, referências inexistentes, duplicidades, código de barras inválido e preço inválido.
- `ProdutoFornecedorServiceTest`: valida criação de vínculo produto-fornecedor, produto/fornecedor inexistente ou inativo, duplicidade, lead time negativo, prioridade negativa, segundo fornecedor principal, atualização mantendo principal, filtros, busca e exclusão lógica.
- `ProdutoFornecedorControllerTests`: valida CRUD completo, filtros por produto, fornecedor, principal, status e código do fornecedor, paginação, soft delete, `401` sem token e `403` sem permissão.
- `ProdutoFornecedorPrecoServiceTest`: valida criação de preço histórico, duplicidade por vigência inicial, vigência inválida, filtros e exclusão lógica.
- `ProdutoFiscalServiceTest`: valida criação fiscal, produto inexistente/inativo, duplicidade por produto, NCM inválido, CEST inválido, alíquota negativa, busca, filtros, atualização e exclusão lógica.
- `ProdutoFiscalControllerTests`: valida CRUD completo, filtros por produto/NCM/CEST/origem/ativo, paginação, soft delete, `401` sem token, `403` sem permissão, produto inexistente, duplicidade, NCM inválido, CEST inválido e alíquota inválida.
- `ProdutoBusinessFlowIntegrationTest`: valida o fluxo completo Pessoa Jurídica -> Fornecedor -> Categoria/Marca/UnidadeMedida -> Produto -> ProdutoFornecedor -> ProdutoFornecedorPreco -> ProdutoFiscal, confirmando vínculos e buscas principais entre Cadastros, Pessoas, Produtos e Fiscal básico.
- `ProdutoImagemServiceTest`: valida criação de imagem, produto/arquivo inexistente, duplicidade produto+arquivo, ordem negativa, segunda imagem principal, filtros, atualização mantendo principal e exclusão lógica.
- `ProdutoImagemControllerTests`: valida CRUD completo, filtros por produto/arquivo/principal/ativo, upload prévio de arquivo, duplicidade, ordem negativa, segunda imagem principal, `401` sem token e `403` sem permissão.
- `ProdutoImagemBusinessFlowIntegrationTest`: valida o fluxo real Produto -> upload de Arquivo -> ProdutoImagem, confirmando vínculos e filtros principais.
- `ProdutoCodigoBarrasServiceTest`: valida criação de código, produto inexistente, código duplicado, código obrigatório, segundo código principal, filtros, atualização mantendo principal e exclusão lógica.
- `ProdutoCodigoBarrasControllerTests`: valida CRUD completo, filtros por produto/tipo/código/principal/ativo, duplicidade, segundo principal, código vazio, `401` sem token e `403` sem permissão.
- `ProdutoCodigoBarrasBusinessFlowIntegrationTest`: valida o fluxo real Categoria/Marca/UnidadeMedida -> Produto -> ProdutoCodigoBarras, confirmando vínculos e filtros principais.
- `ProdutoPrecoServiceTest`: valida criação de preço global e por empresa, produto/empresa inexistente, duplicidade por vigência, preço negativo, vigência inválida, segundo preço principal, filtros, atualização mantendo principal e exclusão lógica.
- `ProdutoPrecoControllerTests`: valida CRUD completo, filtros por produto/tipo/principal/ativo/vigência, busca de preço vigente, duplicidade, segundo principal, preço negativo, vigência inválida, `401` sem token e `403` sem permissão.
- `ProdutoPrecoBusinessFlowIntegrationTest`: valida o fluxo real Categoria/Marca/UnidadeMedida -> Produto -> ProdutoPreco, confirmando vínculos, filtros principais e leitura agregada do produto completo.
- `ProdutoUnidadeServiceTest`: valida criação de unidade, produto/unidade inexistente, duplicidade produto+unidade, fator zero/negativo, unidade padrão com fator diferente de 1, segunda unidade padrão, fracionamento, filtros, atualização mantendo padrão e exclusão lógica.
- `ProdutoUnidadeControllerTests`: valida CRUD completo, filtros por produto/unidade/padrão/flags/fracionamento/ativo, duplicidade, fatores inválidos, segunda unidade padrão, `401` sem token e `403` sem permissão.
- `ProdutoUnidadeBusinessFlowIntegrationTest`: valida o fluxo real Produto -> ProdutoUnidade, confirmando vínculos, filtros principais e leitura agregada do produto completo.
- `MigrationIdempotencyTest`: valida que migrations com `INSERT INTO` tenham proteção idempotente com `ON CONFLICT` ou `WHERE NOT EXISTS`, evitando duplicidade em seeds técnicos.

Para executar:

```powershell
cd backend
.\mvnw.cmd test
```

Resultado esperado no estado atual:

```text
BUILD SUCCESS
Tests run: 324, Failures: 0, Errors: 0
```

Os testes usam o profile `test`, configurado em `backend/src/test/resources/application-test.yml`.

### Politica de inserts em migrations

- Migrations de schema devem focar em tabelas, colunas, indices, constraints, triggers e comentarios.
- Inserts em migrations devem ser usados apenas para dados tecnicos do sistema, como permissoes, perfis, usuario administrador inicial e vinculos de seguranca.
- Todo `INSERT INTO` deve ser idempotente, usando `ON CONFLICT` ou `WHERE NOT EXISTS`.
- Dados operacionais ou demonstrativos nao devem entrar nas migrations versionadas de producao.

### Storage e upload

Configuracoes principais:

```yaml
invplus:
  storage:
    provider: ${INVPLUS_STORAGE_PROVIDER:LOCAL}
    local-path: ${INVPLUS_STORAGE_LOCAL_PATH:./uploads}
    public-base-url: ${INVPLUS_STORAGE_PUBLIC_BASE_URL:http://localhost:8080/uploads}
  cdn:
    enabled: ${INVPLUS_CDN_ENABLED:false}
    base-url: ${INVPLUS_CDN_BASE_URL:http://localhost:8080/uploads}
```

Endpoint de upload:

```text
POST /api/v1/arquivos/upload
multipart/form-data:
  file
  entidade
  entidadeId
```

Tipos permitidos inicialmente: `image/jpeg`, `image/png`, `image/webp` e `application/pdf`. Tamanho maximo: 10 MB. O provider local grava em `./uploads` por padrao e `/uploads/**` fica liberado no Spring Security para desenvolvimento.

---

## Mudanças locais ainda não commitadas

Esta seção documenta o estado atual do workspace antes do próximo commit.

### Backend

- Adicionada arquitetura base em `br.com.invplus`:
  - `common`
  - `shared`
  - `security`
  - `modules`
- Adicionadas entidades compartilhadas:
  - `BaseEntity`
  - `SoftDeleteEntity`
- Adicionadas respostas e exceções compartilhadas:
  - `ApiResponse`
  - `ErrorResponse`
  - `BusinessException`
  - `ConflictException`
  - `InvalidOperationException`
  - `ResourceNotFoundException`
  - `GlobalExceptionHandler`
- Implementado módulo inicial de segurança:
  - `Usuario`
  - `Perfil`
  - `Permissao`
  - `Auditoria`
  - repositories JPA
  - DTOs de login
  - serviço de autenticação
  - filtro JWT
  - configuração Spring Security
- Implementado módulo inicial de Cadastros:
  - entidades JPA `Pais`, `Estado`, `Cidade`, `Endereco`, `Empresa`, `Filial`, `Categoria`, `Marca`, `UnidadeMedida` e `Arquivo`;
  - repositories Spring Data herdando `BaseRepository` e `JpaSpecificationExecutor`;
  - DTOs de request/response para cada recurso;
  - mappers, specifications e validators para a fase fundamental;
  - services com criação, listagem paginada, busca, atualização e exclusão lógica;
  - controllers REST protegidos por permissões `CADASTROS`, retornando `Page<T>` nos GETs;
  - cache inicial com `@Cacheable` para Categoria, Marca, Arquivo, UnidadeMedida, Pais, Estado e Cidade;
  - comentários em classes e métodos para facilitar leitura do código.
- Adicionadas bases compartilhadas `BaseRepository`, `BaseCrudService`, `BaseCrudController` e `CrudMapper`.
- Adicionada auditoria automática com `AuditorAware<UUID>` e principal autenticado com UUID.
- Adicionadas bases para eventos de domínio em `shared/events`.
- Adicionados objetos de valor iniciais `Money`, `Email`, `Cnpj` e `Cep`.
- Adicionado `logback-spring.xml` para padronizar logs via SLF4J/Logback.
- Adicionado `spring-boot-starter-json` ao `pom.xml`, necessário para JSON/Jackson no Spring Boot 4.
- Adicionado `spring-boot-starter-cache` ao `pom.xml`.
- Atualizado `application.yml` para:
  - remover credenciais fixas;
  - exigir variáveis de ambiente;
  - configurar JWT;
  - desligar `open-in-view`;
  - desligar SQL formatado/verboso no profile principal;
  - configurar pool Hikari.
- Adicionado metadata de configuração em `META-INF/additional-spring-configuration-metadata.json`.

### Banco de dados

- `V1__init_extensions.sql` agora habilita `pgcrypto` e `citext`.
- `V2__schema_seguranca.sql` foi expandida para criar o schema real de segurança.
- `V3__seed_seguranca_inicial.sql` adiciona seed inicial:
  - perfil `ADMIN`;
  - permissões básicas;
  - usuário `admin@invplus.com`;
  - vínculos entre usuário, perfil e permissões.
- `V7__schema_cadastros.sql` cria as tabelas base do módulo Cadastros e permissões `CADASTROS`.
- `V8__endereco_uf_varchar.sql` ajusta `endereco.uf` para compatibilidade com validação JPA.
- `V9__cadastros_fundamentais.sql` adiciona `version`, cria `pais`, `estado`, `cidade` e `filial`, e reforça índices para consultas por registros ativos.
- `V10__cadastros_fase_2.sql` consolida as tabelas da fase 2 de Cadastros.
- `V11__permissoes_cadastros_fase_2.sql` adiciona permissões granulares por entidade.
- `V12__categoria_hierarquia_nome_unico.sql` adiciona hierarquia em Categoria e unicidade por nome ativo.
- `V13__permissao_categoria_wildcard.sql` adiciona `CATEGORIA:*`.
- `V14__categoria_ordem_marca_campos.sql` adiciona ordenação de Categoria e campos/índices finais de Marca.
- `V15__permissao_marca_wildcard.sql` adiciona `MARCA:*`.
- `V16__empresa_melhorias.sql` adiciona melhorias finais de Empresa, índices e constraint de CRT.
- `V17__permissao_empresa_wildcard.sql` adiciona `EMPRESA:*` e reforça permissões de Empresa.
- `V18__arquivo_melhorias.sql` adiciona storage, hash SHA-256, URL, extensão e índices finais de Arquivo.
- `V19__permissao_arquivo_wildcard.sql` adiciona `ARQUIVO:*` e reforça permissões de Arquivo.
- `V20__arquivo_colunas_legadas_opcionais.sql` libera colunas legadas após a migração para o contrato novo.
- `V21__schema_pessoa.sql` cria a tabela base `pessoa`.
- `V22__permissoes_pessoa.sql` adiciona permissões `PESSOAS:*`, `PESSOA:*` e CRUD de Pessoa.
- `V23__schema_cliente.sql` cria a tabela `cliente` como papel comercial vinculado a `pessoa`.
- `V24__permissoes_cliente.sql` adiciona permissões `CLIENTE:*` e CRUD de Cliente.
- `V25__schema_fornecedor.sql` cria a tabela `fornecedor` como papel comercial vinculado a `pessoa`.
- `V26__permissoes_fornecedor.sql` adiciona permissões `FORNECEDOR:*` e CRUD de Fornecedor.
- `V27__schema_funcionario.sql` cria a tabela `funcionario` como papel trabalhista vinculado a `pessoa` e opcionalmente a `usuario`.
- `V28__permissoes_funcionario.sql` adiciona permissões `FUNCIONARIO:*` e CRUD de Funcionário.
- `V29__schema_transportadora.sql` cria a tabela `transportadora` como papel logístico vinculado a `pessoa`.
- `V30__permissoes_transportadora.sql` adiciona permissões `TRANSPORTADORA:*` e CRUD de Transportadora.
- `V31__pessoa_role.sql` cria o índice auxiliar de papéis de Pessoa.
- `V32__schema_contato_pessoa.sql` cria contatos operacionais vinculados a Pessoa.
- `V33__permissoes_contato_pessoa.sql` adiciona permissões `CONTATO_PESSOA:*` e CRUD de ContatoPessoa.
- `V34__schema_documento_pessoa.sql` cria documentos cadastrais vinculados a Pessoa e opcionalmente a Arquivo.
- `V35__permissoes_documento_pessoa.sql` adiciona permissões `DOCUMENTO_PESSOA:*` e CRUD de DocumentoPessoa.
- `V36__schema_produto.sql` cria a tabela central `produto`.
- `V37__permissoes_produto.sql` adiciona permissões `PRODUTO:*` e CRUD de Produto.
- `V38__schema_produto_fornecedor.sql` cria o vínculo entre Produto e Fornecedor.
- `V39__permissoes_produto_fornecedor.sql` adiciona permissões `PRODUTO_FORNECEDOR:*` e CRUD de ProdutoFornecedor.
- `V40__produto_fornecedor_melhorias.sql` adiciona prioridade, status, lead time e histórico de preços de fornecedor.
- `V41__permissoes_produto_fornecedor_preco.sql` adiciona permissões `PRODUTO_FORNECEDOR_PRECO:*` e CRUD do histórico de preços.
- `V42__schema_produto_fiscal.sql` cria dados fiscais separados do cadastro comercial do produto.
- `V43__permissoes_produto_fiscal.sql` adiciona permissões `PRODUTO_FISCAL:*` e CRUD de ProdutoFiscal.
- `V44__produto_tipo_natureza.sql` adiciona `tipo_produto` e `natureza_produto` ao cadastro de Produto.
- `V45__produto_status_controles.sql` adiciona status e controles operacionais de estoque, compra e venda ao Produto.
- `V46__arquivo_cloudflare_r2.sql` adiciona `CLOUDFLARE_R2` aos storages aceitos em Arquivo.
- `V47__schema_produto_imagem.sql` cria o vínculo entre Produto e Arquivo para imagens.
- `V48__permissoes_produto_imagem.sql` adiciona permissões `PRODUTO_IMAGEM:*` e CRUD de ProdutoImagem.
- `V49__schema_produto_codigo_barras.sql` cria múltiplos códigos de barras por Produto.
- `V50__permissoes_produto_codigo_barras.sql` adiciona permissões `PRODUTO_CODIGO_BARRAS:*` e CRUD de ProdutoCodigoBarras.
- `V51__schema_produto_preco.sql` cria histórico de preços comerciais por produto, tipo, vigência e empresa opcional.
- `V52__permissoes_produto_preco.sql` adiciona permissões `PRODUTO_PRECO:*` e CRUD de ProdutoPreco.
- `V53__schema_produto_unidade.sql` cria unidades alternativas e fatores de conversão por Produto.
- `V54__permissoes_produto_unidade.sql` adiciona permissões `PRODUTO_UNIDADE:*` e CRUD de ProdutoUnidade.
- `V55__produto_unidade_fracionamento.sql` adiciona `permite_fracionamento` em ProdutoUnidade e índice parcial para consultas ativas.

### Docker

- `docker/compose/docker-compose.yml` agora usa variáveis de ambiente para senhas e usuários.
- PostgreSQL local expõe a porta `5433`.
- Criado `docker/compose/.env.example`.

### Testes

- Adicionado profile de teste em `backend/src/test/resources/application-test.yml`.
- Atualizado `BackendApplicationTests` para usar `@ActiveProfiles("test")`.
- Adicionados testes de repository para o seed de segurança.
- Adicionados testes de login real via `AuthControllerTests`.
- Adicionados testes de repository e controller para o módulo Cadastros.

### Scripts locais

- `criar-arquitetura-backend.ps1`: script usado para criar a arquitetura base.
- `criar-estrutura-security.ps1`: script usado para criar a estrutura inicial do módulo `security`.

> Observação: esses scripts refletem etapas de bootstrap e podem ficar fora do commit final caso o objetivo seja versionar apenas o estado resultante do projeto.

---

## Ordem de desenvolvimento

```
 Fase 1 — Infraestrutura
 ├── [x] 1. Preparação do ambiente
 ├── [ ] 2. Configuração do Git
 ├── [x] 3. Configuração do Docker
 ├── [x] 4. PostgreSQL
 ├── [x] 5. Projeto Spring Boot
 └── [x] 6. Flyway

 Fase 2 — Backend (módulos)
 ├── [x] 7.  Segurança (JWT + Perfis)
 ├── [x] 8.  Cadastros - fases 1 e 2
 ├── [ ] 9.  Pessoas - iniciado com Pessoa, PessoaRole, Cliente, Fornecedor, Funcionário, Transportadora, ContatoPessoa e DocumentoPessoa
 ├── [ ] 10. Produtos - iniciado com Produto, ProdutoFornecedor, ProdutoFornecedorPreco e ProdutoFiscal
 ├── [ ] 11. Estoque
 ├── [ ] 12. Compras
 ├── [ ] 13. Vendas
 ├── [ ] 14. Pagamentos
 ├── [ ] 15. Faturamento
 ├── [ ] 16. Financeiro
 ├── [ ] 17. Fiscal
 └── [ ] 18. CRM

 Fase 3 — Frontend e entrega
 ├── [ ] 19. Relatórios
 └── [ ] 20. Frontend Angular
```

---

## Roadmap

### ✅ Concluído
- [x] Definição da arquitetura
- [x] Modelagem do domínio (DDD)
- [x] DER completo com tipos PostgreSQL e restrições
- [x] Estrutura base do backend Spring Boot
- [x] Docker Compose para infraestrutura local
- [x] Flyway com migrations iniciais
- [x] Schema inicial de segurança
- [x] Seed inicial de segurança
- [x] Login real com Spring Security + JWT
- [x] Testes de contexto, seed e login
- [x] Módulo de Cadastros base com API REST
- [x] Sprint 4 - Fase 1: País, Estado, Cidade, Endereço, Empresa e Filial
- [x] Sprint 4 - Fase 2: UnidadeMedida concluída com checklist completa
- [x] Sprint 4 - Fase 2: Categoria concluída com checklist completa
- [x] Sprint 4 - Fase 2: Marca concluída com checklist completa
- [x] Sprint 4 - Fase 2: Empresa concluída com checklist completa
- [x] Sprint 4 - Fase 2: Arquivo concluída com checklist completa
- [x] Sprint 4 - Fase 2 concluída e revisada
- [x] Testes de repository e controller para Cadastros

### 🔄 Em andamento
- [ ] Consolidação do módulo de Segurança
- [x] Módulo Pessoas iniciado com entidade Pessoa
- [x] Entidade Cliente concluída como papel comercial de Pessoa
- [x] Entidade Fornecedor concluída como papel comercial de Pessoa
- [x] Entidade Funcionário concluída como papel trabalhista de Pessoa
- [x] Entidade Transportadora concluída como papel logístico de Pessoa
- [x] PessoaRole criado para registrar papeis ativos do aggregate Pessoa
- [x] Entidade ContatoPessoa concluída para contatos operacionais da Pessoa
- [x] Entidade DocumentoPessoa concluída para documentos cadastrais da Pessoa
- [x] Módulo Produtos iniciado com entidade Produto
- [x] Entidade ProdutoFiscal concluída para dados tributários separados do produto
- [x] Produto classificado por `TipoProduto` e `NaturezaProduto`
- [x] Produto com `StatusProduto`, `controlaEstoque`, `permiteCompra` e `permiteVenda`

### ⬜ Backend
- [x] Módulo de Segurança (JWT)
- [x] Módulo de Cadastros - fases 1 e 2
- [ ] Módulo de Pessoas iniciado com Pessoa, PessoaRole, Cliente, Fornecedor, Funcionário, Transportadora, ContatoPessoa e DocumentoPessoa
- [x] Módulo de Produtos iniciado com Produto, ProdutoFornecedor, ProdutoFornecedorPreco e ProdutoFiscal
- [ ] Módulo de Estoque
- [ ] Módulo de Compras
- [ ] Módulo de Vendas
- [ ] Módulo Financeiro
- [ ] Módulo Fiscal
- [ ] Módulo CRM

### ⬜ Qualidade
- [ ] Testes unitários (JUnit 5 + Mockito)
- [ ] Testes de integração
- [ ] Documentação da API (OpenAPI/Swagger)
- [ ] Cobertura de testes ≥ 80%

### ⬜ Frontend e entrega
- [ ] Frontend Angular 20
- [ ] Dashboard gerencial
- [ ] Deploy (Docker + CI/CD)
- [ ] **Versão 1.0** 🎯

### ⬜ Futuro
- [ ] Redis (cache)
- [ ] RabbitMQ (mensageria)
- [ ] Filtros avançados no módulo Pessoas
- [ ] Histórico de alterações para cadastros sensíveis
- [ ] Endereços vinculados à Pessoa por tipo
- [ ] Endpoint resumido de Pessoa com papéis, contatos e documentos
- [ ] Validação real de CPF/CNPJ com dígitos verificadores
- [ ] Busca global de Pessoas por nome, documento ou email
- [ ] Integração bancária
- [ ] Integração com marketplace
- [ ] API Pública
- [ ] Aplicativo mobile
- [ ] Microsserviços

---

## Padrão de desenvolvimento

### Branches

```
main              → produção (protegida)
develop           → integração contínua
feature/nome      → nova funcionalidade
fix/nome          → correção de bug
refactor/nome     → refatoração
docs/nome         → documentação
```

**Exemplos:**
```
feature/cadastro-produtos
feature/autenticacao-jwt
fix/correcao-calculo-imposto
docs/atualiza-der
```

### Commits — Conventional Commits

```
<tipo>(<escopo>): <descrição curta>
```

| Tipo | Uso |
|---|---|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `docs` | Documentação |
| `style` | Formatação (sem mudança lógica) |
| `refactor` | Refatoração sem nova feature ou fix |
| `test` | Adição ou correção de testes |
| `chore` | Configuração, dependências, build |
| `perf` | Melhoria de performance |

**Exemplos:**
```
feat(produtos): adiciona endpoint de cadastro de produto
feat(seguranca): implementa autenticação com JWT
fix(estoque): corrige cálculo de saldo após movimentação
docs(der): atualiza diagrama com módulo fiscal
test(produtos): adiciona testes unitários para ProdutoService
chore(docker): configura docker-compose para ambiente local
```

### Padrão de endpoint REST

```
GET    /api/v1/produtos          → listar
GET    /api/v1/produtos/search   → busca rápida por termo
GET    /api/v1/produtos/{id}     → buscar por id
GET    /api/v1/produtos/{id}/completo → buscar visão agregada do produto
POST   /api/v1/produtos          → criar
PUT    /api/v1/produtos/{id}     → atualizar
DELETE /api/v1/produtos/{id}     → remover (soft delete)
GET    /api/v1/produtos-fornecedores      → listar vínculos produto-fornecedor
GET    /api/v1/produtos-fornecedores/{id} → buscar vínculo por id
POST   /api/v1/produtos-fornecedores      → criar vínculo
PUT    /api/v1/produtos-fornecedores/{id} → atualizar vínculo
DELETE /api/v1/produtos-fornecedores/{id} → remover vínculo (soft delete)
GET    /api/v1/produtos-fornecedores-precos      → listar histórico de preços
GET    /api/v1/produtos-fornecedores-precos/{id} → buscar preço por id
POST   /api/v1/produtos-fornecedores-precos      → criar preço
PUT    /api/v1/produtos-fornecedores-precos/{id} → atualizar preço
DELETE /api/v1/produtos-fornecedores-precos/{id} → remover preço (soft delete)
GET    /api/v1/produtos-fiscais      → listar dados fiscais
GET    /api/v1/produtos-fiscais/{id} → buscar dado fiscal por id
POST   /api/v1/produtos-fiscais      → criar dado fiscal
PUT    /api/v1/produtos-fiscais/{id} → atualizar dado fiscal
DELETE /api/v1/produtos-fiscais/{id} → remover dado fiscal (soft delete)
POST   /api/v1/arquivos/upload        → upload multipart e registro de metadados
GET    /api/v1/produtos-imagens      → listar imagens de produto
GET    /api/v1/produtos-imagens/{id} → buscar imagem por id
POST   /api/v1/produtos-imagens      → vincular Produto e Arquivo
PUT    /api/v1/produtos-imagens/{id} → atualizar vínculo de imagem
DELETE /api/v1/produtos-imagens/{id} → remover vínculo de imagem (soft delete)
GET    /api/v1/produtos-codigos-barras      → listar códigos de barras
GET    /api/v1/produtos-codigos-barras/{id} → buscar código de barras por id
POST   /api/v1/produtos-codigos-barras      → criar código de barras
PUT    /api/v1/produtos-codigos-barras/{id} → atualizar código de barras
DELETE /api/v1/produtos-codigos-barras/{id} → remover código de barras (soft delete)
GET    /api/v1/produtos-precos      → listar preços de produto
GET    /api/v1/produtos-precos/vigente → buscar preço vigente por produto, tipo e data
GET    /api/v1/produtos-precos/{id} → buscar preço por id
POST   /api/v1/produtos-precos      → criar preço
PUT    /api/v1/produtos-precos/{id} → atualizar preço
DELETE /api/v1/produtos-precos/{id} → remover preço (soft delete)
GET    /api/v1/produtos-unidades      → listar unidades do produto
GET    /api/v1/produtos-unidades/{id} → buscar unidade por id
POST   /api/v1/produtos-unidades      → criar unidade do produto
PUT    /api/v1/produtos-unidades/{id} → atualizar unidade do produto
DELETE /api/v1/produtos-unidades/{id} → remover unidade do produto (soft delete)
```

Filtros aceitos em `GET /api/v1/produtos`:

```text
nome
codigo
sku
codigoBarras
categoriaId
marcaId
tipoProduto
naturezaProduto
statusProduto
controlaEstoque
permiteCompra
permiteVenda
ativo
page
size
sort
```

Busca rápida:

```text
GET /api/v1/produtos/search?termo=...
```

A busca rápida pesquisa em nome, código, SKU e código de barras. O código de barras é validado de forma flexível para GTIN, aceitando 8, 12, 13 ou 14 dígitos numéricos.

Enums de Produto:

```text
TipoProduto:
PRODUTO
SERVICO
MATERIA_PRIMA
INSUMO
EMBALAGEM
COMBUSTIVEL
ATIVO_IMOBILIZADO
USO_CONSUMO

NaturezaProduto:
VENDA
COMPRA
AMBOS

StatusProduto:
EM_DESENVOLVIMENTO
ATIVO
DESCONTINUADO
BLOQUEADO
SUSPENSO
```

Filtros aceitos em `GET /api/v1/produtos-fornecedores`:

```text
produtoId
fornecedorId
principal
ativo
codigoFornecedor
status
page
size
sort
```

Filtros aceitos em `GET /api/v1/produtos-fornecedores-precos`:

```text
produtoFornecedorId
vigenciaInicioDe
vigenciaFimAte
ativo
page
size
sort
```

Filtros aceitos em `GET /api/v1/produtos-fiscais`:

```text
produtoId
ncm
cest
origemMercadoria
ativo
page
size
sort
```

Filtros aceitos em `GET /api/v1/produtos-imagens`:

```text
produtoId
arquivoId
principal
ativo
page
size
sort
```

Filtros aceitos em `GET /api/v1/produtos-codigos-barras`:

```text
produtoId
tipoCodigo
codigo
principal
ativo
page
size
sort
```

Filtros aceitos em `GET /api/v1/produtos-precos`:

```text
produtoId
empresaId
tipoPreco
principal
vigenciaInicioDe
vigenciaFimAte
ativo
page
size
sort
```

Filtros aceitos em `GET /api/v1/produtos-unidades`:

```text
produtoId
unidadeMedidaId
unidadePadrao
permiteCompra
permiteVenda
permiteEstoque
permiteFracionamento
ativo
page
size
sort
```

### Padrão de resposta da API

```json
{
  "success": true,
  "data": { },
  "message": "Operação realizada com sucesso",
  "timestamp": "2025-01-01T00:00:00Z"
}
```

---

## Contribuindo

Este é um projeto pessoal em desenvolvimento. Contribuições, sugestões e feedbacks são bem-vindos.

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/minha-feature`)
3. Commit suas mudanças seguindo o padrão de commits (`git commit -m 'feat: adiciona minha feature'`)
4. Push para a branch (`git push origin feature/minha-feature`)
5. Abra um Pull Request para a branch `develop`

### Diretrizes

- Siga os princípios arquiteturais do projeto (DDD, Clean Architecture, SOLID)
- Escreva testes para novas funcionalidades
- Mantenha a documentação atualizada
- Respeite a estrutura modular existente

---

## Licença

Este projeto está licenciado sob a **MIT License** — veja o arquivo [LICENSE](LICENSE) para detalhes.

```
MIT License — Copyright (c) 2025 Josiel Amorim
```

---

## Autor

<div align="center">

Desenvolvido com dedicação por **Josiel Amorim**

<br/>

*"A arquitetura atual prioriza organização, clareza e facilidade de manutenção,*
*deixando microsserviços e recursos avançados para versões futuras."*

</div>
