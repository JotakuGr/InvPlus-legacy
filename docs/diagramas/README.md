# Diagramas Mermaid

Os diagramas abaixo renderizam diretamente no GitHub.

## Diagrama Geral

```mermaid
graph TD
    Angular[Angular] --> API[Spring Boot REST API]
    API --> Domain[Domain Services]
    Domain --> JPA[Spring Data JPA]
    JPA --> PostgreSQL[(PostgreSQL)]
    API --> Flyway[Flyway]
    API --> JWT[Spring Security + JWT]
    API --> Storage[Storage Local / Cloudflare R2]
    API -. preparado .-> Redis[(Redis)]
    API -. preparado .-> RabbitMQ[(RabbitMQ)]
```

## Fluxo de Venda

```mermaid
graph TD
    Cliente --> PedidoVenda[PedidoVenda]
    PedidoVenda --> PedidoVendaItem[PedidoVendaItem]
    PedidoVendaItem --> Produto
    PedidoVenda --> ReservaEstoque[ReservaEstoque]
    ReservaEstoque --> Estoque
    Estoque --> MovimentacaoEstoque[MovimentacaoEstoque]
    PedidoVenda --> VendaFaturamento[VendaFaturamento]
    VendaFaturamento --> ContaReceber[ContaReceber]
    ContaReceber --> ParcelaReceber[ParcelaReceber]
    ParcelaReceber --> Recebimento
    VendaFaturamento -. futuro .-> Fiscal
```

## Fluxo de Compra

```mermaid
graph TD
    Fornecedor --> PedidoCompra[PedidoCompra]
    PedidoCompra --> PedidoCompraItem[PedidoCompraItem]
    PedidoCompraItem --> Produto
    PedidoCompra --> EntradaMercadoria[EntradaMercadoria]
    EntradaMercadoria --> EntradaMercadoriaItem[EntradaMercadoriaItem]
    EntradaMercadoriaItem --> Estoque
    Estoque --> MovimentacaoEstoque[MovimentacaoEstoque]
    EntradaMercadoria -. futuro .-> ContaPagar[ContaPagar]
```

## Fluxo de Estoque

```mermaid
graph TD
    Produto --> Estoque
    Empresa --> Estoque
    Deposito --> LocalizacaoEstoque[LocalizacaoEstoque]
    LocalizacaoEstoque --> Estoque
    Estoque --> ReservaEstoque[ReservaEstoque]
    Estoque --> MovimentacaoEstoque[MovimentacaoEstoque]
    Estoque -. futuro .-> Inventario
```

## Fluxo Financeiro

```mermaid
graph TD
    VendaFaturamento[VendaFaturamento] --> ContaReceber[ContaReceber]
    ContaReceber --> ParcelaReceber[ParcelaReceber]
    ParcelaReceber --> Recebimento
    Recebimento --> FormaPagamento[FormaPagamento]
    ParcelaReceber -. futuro .-> Caixa
    Caixa -. futuro .-> FluxoCaixa[FluxoCaixa]
```

## Conciliação Bancária

```mermaid
graph LR
    MovimentoFinanceiro[MovimentoFinanceiro] --> ConciliacaoBancaria[ConciliacaoBancaria]
    ExtratoBancarioLancamento[ExtratoBancarioLancamento] --> ConciliacaoBancaria
    ExtratoBancario[ExtratoBancario] --> ExtratoBancarioLancamento
    ContaBancaria[ContaBancaria] --> ExtratoBancario
    ConciliacaoBancaria --> StatusExtrato[IMPORTADO / PROCESSANDO / CONCILIADO]
```

## Produto

```mermaid
graph TD
    Produto --> Categoria
    Produto --> Marca
    Produto --> UnidadeMedida[UnidadeMedida]
    Produto --> ProdutoPreco[ProdutoPreco]
    Produto --> ProdutoFiscal[ProdutoFiscal]
    Produto --> ProdutoFornecedor[ProdutoFornecedor]
    ProdutoFornecedor --> Fornecedor
    ProdutoFornecedor --> ProdutoFornecedorPreco[ProdutoFornecedorPreco]
    Produto --> ProdutoImagem[ProdutoImagem]
    ProdutoImagem --> Arquivo
    Produto --> ProdutoCodigoBarras[ProdutoCodigoBarras]
    Produto --> ProdutoUnidade[ProdutoUnidade]
```
