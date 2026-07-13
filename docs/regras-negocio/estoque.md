# Regras de Negócio - Estoque

- Estoque nunca deve ser controlado apenas por produto.
- Saldo é controlado por Produto, Empresa, Depósito e Localização.
- Nenhum módulo externo deve alterar saldo diretamente por repository.
- Alterações passam por `EstoqueDomainService` ou `EstoqueFacade`.
- Reserva reduz disponibilidade sem baixar saldo físico.
- Baixa de reserva reduz saldo físico.
- MovimentacaoEstoque é histórico imutável.
- Depósito e Localização podem ser bloqueados sem exclusão lógica.

## Contexto empresarial

- A empresa das operacoes e obtida exclusivamente pelo `EmpresaContext`.
- Requests operacionais e filtros publicos nao aceitam `empresaId`.
- Depositos e localizacoes sao resolvidos dentro da empresa ativa.
- Saldos, movimentacoes e reservas possuem filtro empresarial obrigatorio.
- Recursos de outra empresa sao tratados como nao encontrados (`404`).
- O teste de carga ativa um contexto independente em cada thread concorrente.
- Esta refatoracao nao exigiu migration: as tabelas existentes ja possuem caminho confiavel ate a empresa.
