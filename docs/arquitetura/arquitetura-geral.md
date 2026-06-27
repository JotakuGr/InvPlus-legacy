# Arquitetura Geral — invPlus ERP

## Visão Geral

O invPlus ERP será desenvolvido inicialmente como um monólito modular, organizado por domínios de negócio.

A escolha por monólito modular permite iniciar o desenvolvimento com menor complexidade, mantendo uma estrutura limpa e preparada para uma possível evolução futura para microsserviços.

## Tipo de Arquitetura

- Monólito Modular
- API REST
- Backend em Java com Spring Boot
- Frontend em Angular
- Banco de dados PostgreSQL
- Docker para ambiente de desenvolvimento

## Visão em Camadas

```text
Frontend Angular
      
API REST Spring Boot
      
Camada de Serviços
      
Camada de Repositórios
      
PostgreSQL

Módulos Principais
Segurança
Cadastros
Pessoas
Produtos
Estoque
Compras
Vendas
Pagamentos
Faturamento
Financeiro
Fiscal
CRM

Objetivo Arquitetural

Criar uma aplicação organizada, escalável e de fácil manutenção, separando responsabilidades por módulos de negócio.