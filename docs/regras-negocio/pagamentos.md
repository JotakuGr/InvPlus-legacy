# Regras de Negócio - Pagamentos

- FormaPagamento representa o meio operacional.
- Troco só pode ser habilitado para dinheiro.
- Parcelamento só pode ser habilitado para tipos compatíveis.
- CondicaoPagamento deve possuir parcelas.
- A soma das parcelas deve fechar 100%.
- Regra de vencimento ajusta finais de semana.
- Simulação de parcelas não persiste dados financeiros.
