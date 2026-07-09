# Regras de Negócio - Produtos

- Produto é o centro do catálogo.
- Preço, fiscal, fornecedor, imagem, código de barras e unidade ficam em entidades próprias.
- ProdutoPreco deve ser usado por vendas e PDV.
- ProdutoFornecedorPreco registra histórico de custo.
- ProdutoFiscal isola dados tributários.
- ProdutoCodigoBarras permite múltiplos códigos por produto.
- ProdutoUnidade controla conversão e flags de compra, venda, estoque e fracionamento.
- Campos legados de preço e código no Produto devem ser evitados em novos fluxos.
