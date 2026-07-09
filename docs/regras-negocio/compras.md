# Regras de Negócio - Compras

- PedidoCompra nasce em `ABERTO`.
- Pedido cancelado ou recebido não pode ser alterado.
- Itens recalculam o valor total do pedido.
- EntradaMercadoria só movimenta estoque quando é efetivada.
- Efetivação usa `EstoqueFacade.entrada(...)`.
- EntradaMercadoria efetivada não deve ser alterada.
- Histórico de status deve preservar transições relevantes.
