# Regras de Negócio - Compras

- PedidoCompra nasce em `ABERTO`.
- Pedido cancelado ou recebido não pode ser alterado.
- Itens recalculam o valor total do pedido.
- EntradaMercadoria só movimenta estoque quando é efetivada.
- Efetivação usa `EstoqueFacade.entrada(...)`.
- EntradaMercadoria efetivada não deve ser alterada.
- Histórico de status deve preservar transições relevantes.

## Contexto empresarial

- A empresa de pedidos e entradas e obtida exclusivamente pelo `EmpresaContext`.
- Requests e filtros publicos nao aceitam `empresaId`.
- Pedidos, entradas e itens sao buscados por identificador mais empresa.
- Filiais, depositos, localizacoes e saldos devem pertencer a empresa ativa.
- A efetivacao propaga a mesma empresa para Estoque e Movimento de Estoque.
- Recursos de outra empresa sao tratados como nao encontrados (`404`).
- Nenhuma migration foi necessaria para os agregados de Compras existentes.

`Fornecedor`, `Produto` e `ProdutoUnidade` permanecem cadastros globais no modelo atual.
A empresarializacao desses cadastros exige decisao de dominio e migration propria; ela nao
foi misturada nesta refatoracao.
