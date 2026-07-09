# Padrão de Implementação de Módulos

## Estrutura

Cada módulo deve seguir a organização:

```text
controller/
dto/request/
dto/response/
entity/
enums/
events/
mapper/
repository/
service/
specification/
validator/
```

## Responsabilidades

| Camada | Responsabilidade |
|---|---|
| Entity | Estado e regras simples do domínio |
| Repository | Persistência e consultas específicas |
| DTO Request | Entrada da API |
| DTO Response | Saída da API |
| Mapper | Conversão sem acessar banco |
| Validator | Regras de validação e conflitos |
| Specification | Filtros dinâmicos |
| Service | Orquestração de caso de uso |
| Controller | Contrato HTTP e permissões |

## Quando Criar DomainService

Crie um DomainService quando a regra:

- For usada por mais de um módulo.
- Alterar estado crítico.
- Precisar manter múltiplas entidades sincronizadas.

Exemplo: `EstoqueDomainService`.

## Quando Criar AggregateService

Crie um AggregateService quando o frontend precisar de leitura consolidada.

Exemplos:

- `ProdutoAggregateService`
- `EstoqueAggregateService`
- `VendaAggregateService`
- `CompraAggregateService`

## Quando Criar Facade

Crie uma Facade quando outros módulos precisarem chamar operações de alto nível sem conhecer services internos.

Exemplos:

- `EstoqueFacade`
- `VendaFacade`
- `CompraFacade`

## Quando Publicar Eventos

Publique evento quando houver mudança relevante de estado:

- Pedido aprovado.
- Estoque alterado.
- Reserva criada.
- Venda faturada.
- Recebimento cancelado.

## Quando Usar Specification

Use Specification em listagens paginadas com filtros opcionais. Evite criar múltiplos métodos no repository para cada combinação de filtro.

## Ordem Padrão

```text
Migration
Entity
Repository
DTO Request
DTO Response
Mapper
Specification
Validator
Service
Controller
Testes
Documentação
```
