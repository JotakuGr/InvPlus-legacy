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

## Importação OFX

```mermaid
graph TD
    ArquivoOFX[Arquivo OFX] --> ImportacaoExtratoService
    ImportacaoExtratoService --> OfxParser
    OfxParser --> ExtratoImportado[Modelo intermediário]
    ExtratoImportado --> ContaBancaria[Identificação da ContaBancaria]
    ContaBancaria --> ExtratoBancario
    ExtratoBancario --> ExtratoBancarioLancamento
    ExtratoBancarioLancamento --> ConciliacaoBancaria
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

## Sugestao de conciliacao

```mermaid
flowchart LR
    L[Lancamento bancario] --> F[Filtros obrigatorios no banco]
    F --> P[Pontuacao explicavel]
    P --> U[Analise do usuario]
    U --> C[Confirmacao manual]
    C --> V[Revalidacao do dominio]
```

```mermaid
flowchart TD
    P[Painel sob demanda] --> E[Extratos resumidos]
    P --> L[Pendencias paginadas]
    L --> S[Melhores sugestoes]
    S --> B[Confirmacao em lote]
    B --> I1[Item REQUIRES_NEW]
    B --> I2[Item REQUIRES_NEW]
    I1 --> D[Servico de conciliacao]
    I2 --> D
```

```mermaid
flowchart LR
    U[Usuario] --> UE[UsuarioEmpresa]
    UE --> E[Empresa]
    UE --> UEF[UsuarioEmpresaFilial]
    UEF --> F[Filial]
    UE --> J[JWT ACCESS]
    J --> C[EmpresaContext]
```
```mermaid
flowchart LR
    JWT[Access token com empresa_id] --> Contexto[EmpresaContext]
    Contexto --> Service[Service empresarial]
    Service --> Repository[Repository filtrado por empresa]
    Repository --> Banco[(PostgreSQL)]
```

No fluxo de Estoque, `EmpresaContext` antecede as consultas de deposito, localizacao,
saldo, movimentacao e reserva. Compras e Vendas continuam consumindo `EstoqueFacade`,
sem escolher a empresa da operacao.

No fluxo de Vendas, o contexto acompanha PedidoVenda -> ReservaEstoque -> VendaFaturamento
-> ContaReceber. A troca de empresa altera todas as consultas sem receber `empresaId`.
