# Regras de Negócio - Vendas

- PedidoVenda nasce em `ABERTO`.
- Aprovação exige itens.
- Itens usam preço vindo de ProdutoPreco.
- Aprovação reserva estoque automaticamente.
- Cancelamento de pedido aprovado cancela reservas ativas.
- Faturamento baixa reservas e muda PedidoVenda para `FATURADO`.
- Pedido faturado não deve ser alterado.
- VendaAggregateService entrega leitura completa para o frontend.

## Contexto empresarial

- A empresa e obtida exclusivamente pelo `EmpresaContext`.
- Requests, filtros e controllers de Vendas nao aceitam `empresaId`.
- PedidoVenda, itens, faturamento, ProdutoPreco, estoque e reservas sao consultados no contexto da empresa ativa.
- Recursos empresariais de outra empresa sao tratados como inexistentes (`404`).
- A aprovacao e o faturamento preservam a empresa na reserva, movimentacao de estoque e ContaReceber.

Cliente, Produto, ProdutoUnidade e CondicaoPagamento ainda sao cadastros globais no schema
atual. A vinculacao empresarial desses cadastros exige uma evolucao de banco separada e nao
faz parte desta refatoracao de contrato.
