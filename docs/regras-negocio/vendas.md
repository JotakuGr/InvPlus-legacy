# Regras de Negócio - Vendas

- PedidoVenda nasce em `ABERTO`.
- Aprovação exige itens.
- Itens usam preço vindo de ProdutoPreco.
- Aprovação reserva estoque automaticamente.
- Cancelamento de pedido aprovado cancela reservas ativas.
- Faturamento baixa reservas e muda PedidoVenda para `FATURADO`.
- Pedido faturado não deve ser alterado.
- VendaAggregateService entrega leitura completa para o frontend.
