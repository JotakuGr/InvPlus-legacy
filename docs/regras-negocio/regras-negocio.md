# Regras de Negócio

Este documento concentra as regras principais do domínio. Detalhes específicos continuam nos validators, services e testes.

## Documentos por Módulo

- `docs/regras-negocio/seguranca.md`
- `docs/regras-negocio/cadastros.md`
- `docs/regras-negocio/pessoas.md`
- `docs/regras-negocio/produtos.md`
- `docs/regras-negocio/estoque.md`
- `docs/regras-negocio/compras.md`
- `docs/regras-negocio/vendas.md`
- `docs/regras-negocio/pagamentos.md`
- `docs/regras-negocio/financeiro.md`

## Regras Gerais

- Registros excluídos logicamente não devem aparecer por padrão.
- Duplicidades são validadas apenas entre registros ativos.
- Operações inválidas retornam erro padronizado.
- Conflitos de duplicidade ou concorrência retornam `409`.
- Entidades auditáveis usam `@Version` para concorrência otimista.

## Pessoas

- Pessoa é o agregado central do módulo.
- CPF/CNPJ identifica a pessoa e deve ser único entre registros ativos.
- Cliente, Fornecedor, Funcionário e Transportadora são papéis de Pessoa.
- Uma Pessoa pode exercer vários papéis ao mesmo tempo.
- Serviços de papéis não devem alterar dados cadastrais principais da Pessoa.

## Produtos

- Produto é o centro do catálogo.
- Preços, fiscal, imagem, unidade, fornecedor e códigos de barras ficam em entidades próprias.
- Novos módulos devem consumir `ProdutoPreco` e `ProdutoCodigoBarras`, não campos legados do Produto.
- ProdutoFiscal isola dados tributários do cadastro comercial.
- ProdutoUnidade controla conversão, compra, venda, estoque e fracionamento.

## Estoque

- Nunca armazenar estoque apenas por produto.
- O saldo é controlado por Produto, Empresa, Depósito e Localização.
- Nenhum módulo externo deve alterar saldo diretamente via repository.
- Alterações de saldo passam por `EstoqueDomainService` ou `EstoqueFacade`.
- Reservas reduzem disponibilidade, mas não baixam saldo físico.
- Baixa de reserva reduz saldo físico e zera quantidade reservada correspondente.
- Movimentações de estoque são histórico imutável.

## Compras

- Pedido de compra nasce em `ABERTO`.
- Pedido cancelado ou recebido não deve ser alterado.
- Itens recalculam o valor total do pedido.
- Entrada de mercadoria só movimenta estoque quando é efetivada.
- Efetivação usa `EstoqueFacade.entrada(...)`.

## Vendas

- Pedido de venda nasce em `ABERTO`.
- Aprovação exige itens.
- Aprovação reserva estoque automaticamente.
- Cancelamento de pedido aprovado cancela reservas ativas.
- Faturamento exige condição de pagamento, baixa reservas, movimenta estoque, muda pedido para `FATURADO`, cria ContaReceber e gera ParcelasReceber.
- Venda faturada não pode gerar mais de uma ContaReceber.
- Itens de venda não recebem `valorUnitario` do frontend; o preço vem de `ProdutoPreco`.

## Pagamentos

- Forma de pagamento define meio operacional.
- Troco só é permitido para dinheiro.
- Parcelamento é permitido apenas para tipos compatíveis.
- Condição de pagamento deve possuir parcelas que somem 100%.
- Vencimentos podem ser ajustados por regra de dia útil.

## Financeiro

- ContaReceber inicia com `valorPago = 0` e `status = ABERTA`.
- ContaReceber gerada por venda copia Empresa, Filial, Cliente, valor e data do VendaFaturamento.
- `valorAberto = valorTotal - valorPago`.
- Conta paga ou cancelada não deve ser alterada.
- Parcelas geradas por condição de pagamento não devem duplicar quando já houver parcelas ativas.
- Baixa de parcela consolida status e valores da ContaReceber.
- Recebimento registra o evento financeiro e delega a baixa para ParcelaReceberService.
- Cancelamento de recebimento estorna a parcela e reconsolida a ContaReceber, mantendo o histórico com status `CANCELADO`.
