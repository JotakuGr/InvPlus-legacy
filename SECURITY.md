# Security Policy

Obrigado por ajudar a manter o **invPlus ERP** seguro.

Este documento descreve como reportar vulnerabilidades de segurança e quais versões do projeto recebem suporte.

---

# Índice

- Versões Suportadas
- Reportando uma Vulnerabilidade
- O que Reportar
- O que Não Reportar
- Processo de Tratamento
- Tempo de Resposta
- Boas Práticas para Contribuidores
- Dependências
- Ferramentas de Segurança
- Escopo

---

# Versões Suportadas

Atualmente apenas a versão mais recente da branch **main** recebe atualizações de segurança.

| Versão | Suporte |
|---------|:-------:|
| main | ✅ |
| versões antigas | ❌ |

---

# Reportando uma Vulnerabilidade

Caso encontre uma vulnerabilidade de segurança:

- **Não abra uma Issue pública.**
- **Não publique a vulnerabilidade em Pull Requests.**
- Entre em contato diretamente com o mantenedor do projeto.

Contato:

- GitHub: **@JotakuGr**

Após o recebimento do relatório:

1. O problema será reproduzido.
2. Será avaliada sua severidade.
3. Uma correção será desenvolvida.
4. A correção será validada por testes.
5. Após a publicação da correção, a vulnerabilidade poderá ser divulgada.

---

# O que Reportar

São exemplos de vulnerabilidades relevantes:

## Autenticação

- Bypass de autenticação
- Login indevido
- JWT inválido aceito
- Escalada de privilégios

---

## Autorização

- Usuário acessando recursos sem permissão
- Quebra do controle RBAC
- Endpoints protegidos acessíveis

---

## Banco de Dados

- SQL Injection
- Alteração indevida de registros
- Vazamento de dados

---

## API

- Exposição de informações sensíveis
- Mass Assignment
- Validação insuficiente
- Enumeração de usuários

---

## Arquivos

- Upload inseguro
- Path Traversal
- Download não autorizado

---

## Infraestrutura

- Docker mal configurado
- Secrets expostos
- Configurações inseguras

---

## Dependências

- Bibliotecas vulneráveis
- CVEs conhecidos

---

# O que Não Reportar

Os itens abaixo normalmente não são considerados vulnerabilidades:

- Problemas de formatação
- Melhorias de performance
- Refatorações
- Alterações visuais
- Sugestões de arquitetura
- Problemas já conhecidos no Roadmap

Esses casos devem ser abertos como Issues.

---

# Processo de Tratamento

Após o recebimento do relatório:

```
Recebimento
        ↓
Validação
        ↓
Reprodução
        ↓
Correção
        ↓
Testes
        ↓
Publicação
        ↓
Divulgação
```

Sempre que possível, a vulnerabilidade será corrigida antes da divulgação pública.

---

# Tempo de Resposta

Objetivos do projeto:

| Etapa | Prazo |
|--------|--------|
| Confirmação do recebimento | até 5 dias úteis |
| Análise inicial | até 10 dias úteis |
| Correção (quando possível) | conforme severidade |

Os prazos podem variar conforme a complexidade do problema.

---

# Boas Práticas para Contribuidores

Ao contribuir com o projeto:

- Nunca commite senhas.
- Nunca commite tokens.
- Nunca commite chaves privadas.
- Nunca exponha credenciais em exemplos.
- Nunca desabilite validações de segurança.
- Sempre utilize validações no backend.
- Sempre utilize permissões (`@PreAuthorize`) quando necessário.
- Sempre escreva testes para regras críticas.

---

# Dependências

As dependências do projeto são verificadas por:

- GitHub Dependency Review
- Dependabot (quando habilitado)
- CodeQL
- GitHub Security Advisories

Sempre utilize versões estáveis e suportadas.

---

# Ferramentas de Segurança

O projeto utiliza:

- GitHub Actions
- CodeQL
- Dependency Review
- Spring Security
- JWT
- Flyway
- PostgreSQL
- Docker

Essas ferramentas ajudam na identificação de vulnerabilidades e manutenção da segurança do projeto.

---

# Escopo

Esta política cobre:

- Backend Java
- API REST
- Banco de Dados
- Docker
- GitHub Actions
- Documentação
- Configurações do projeto

Não cobre:

- Sistemas de terceiros
- Ambiente local dos desenvolvedores
- Infraestrutura externa não mantida pelo projeto

---

# Divulgação Responsável

Solicitamos que qualquer vulnerabilidade seja mantida em sigilo até que uma correção esteja disponível.

Isso protege os usuários e evita exploração indevida.

---

# Agradecimentos

Agradecemos a todos que contribuem para tornar o **invPlus ERP** mais seguro.

A colaboração responsável é essencial para manter a qualidade e a confiabilidade do projeto.