# Regras de Negócio - Estoque

- Estoque nunca deve ser controlado apenas por produto.
- Saldo é controlado por Produto, Empresa, Depósito e Localização.
- Nenhum módulo externo deve alterar saldo diretamente por repository.
- Alterações passam por `EstoqueDomainService` ou `EstoqueFacade`.
- Reserva reduz disponibilidade sem baixar saldo físico.
- Baixa de reserva reduz saldo físico.
- MovimentacaoEstoque é histórico imutável.
- Depósito e Localização podem ser bloqueados sem exclusão lógica.
