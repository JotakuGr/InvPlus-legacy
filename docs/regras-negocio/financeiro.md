# Regras de Negócio - Financeiro

- ContaReceber inicia aberta, sem valor pago.
- `valorAberto = valorTotal - valorPago`.
- Conta paga ou cancelada não pode ser alterada.
- ParcelaReceber representa cada vencimento da conta.
- Baixa de parcela atualiza parcela e consolida a conta.
- Recebimento registra o evento financeiro da entrada.
- Cancelamento de recebimento estorna a parcela e reconsolida a conta.
- Recebimento cancelado permanece como histórico.
- ContaPagar inicia `ABERTA`, com `valorPago = 0` e `valorAberto = valorTotal`.
- O numero da ContaPagar e unico por empresa entre registros ativos.
- Empresa e Fornecedor devem estar ativos; Filial deve pertencer a Empresa.
- EntradaMercadoria e opcional, mas deve pertencer a mesma Empresa, Filial e Fornecedor.
- ContaPagar `PAGA` ou `CANCELADA` nao pode ser alterada.
- O cancelamento preserva o titulo para rastreabilidade financeira.
- ParcelaPagar inicia `ABERTA`, com saldo aberto igual ao valor e encargos zerados.
- Numero da parcela e unico por ContaPagar entre registros ativos.
- A geracao usa CondicaoPagamento e ajusta a ultima parcela para fechar o total.
- A baixa separa principal, juros, multa e desconto e pode ser parcial ou total.
- Toda baixa ou cancelamento reconsolida valor pago, valor aberto e status da ContaPagar.
- Parcela paga ou cancelada nao pode ser alterada ou cancelada novamente.
- Pagamento registra o evento financeiro e delega a baixa para ParcelaPagarService.
- Conta, Empresa, Filial e Fornecedor do pagamento sao copiados da parcela.
- `valorLiquido = valorPago + juros + multa - desconto`.
- Pagamento cancelado permanece no historico com status `CANCELADO`.

## Movimento financeiro

- Cada Recebimento gera uma `ENTRADA` com origem `RECEBIMENTO`.
- Cada Pagamento gera uma `SAIDA` com origem `PAGAMENTO`.
- O movimento copia empresa, filial, forma de pagamento e a contraparte para preservar o contexto.
- Origens `RECEBIMENTO` e `PAGAMENTO` não podem ser informadas pelo endpoint manual.
- Ajustes manuais, transferências e estornos exigem valor positivo e data de movimento.
- Entradas podem referenciar Cliente; saídas podem referenciar Fornecedor.
- O cancelamento do evento de origem cancela o movimento na mesma transação, sem exclusão física.
- Um evento automático pode possuir somente um movimento ativo por `origem + origemId`.

## Relatórios financeiros

- O fluxo de caixa soma apenas movimentos `ATIVO`, não excluídos e dentro do período inclusivo.
- Entradas menos saídas formam o saldo do período.
- Movimentos cancelados não participam dos totais.
- O dashboard soma o saldo aberto de contas `ABERTA`, `PARCIALMENTE_PAGA` e `VENCIDA`.
- A quantidade de vencidos considera somente contas com status `VENCIDA`, ativas e não excluídas.
- A data final não pode ser anterior à data inicial.
- Fluxo de caixa e dashboard podem ser filtrados por centro de custo, plano de conta ou ambos.
- A DRE financeira considera apenas movimentos `ATIVO` com PlanoConta informado.
- Na DRE, `ENTRADA` compõe receitas e `SAIDA` compõe despesas.
- A DRE agrupa receitas e despesas por código e nome do PlanoConta.
- A DRE pode ser filtrada por Empresa, Filial, CentroCusto e PlanoConta.

## Centro de custo

- Todo centro de custo pertence obrigatoriamente a uma Empresa ativa.
- O código é único por Empresa entre registros ativos e não excluídos.
- O tipo pode ser `RECEITA`, `DESPESA` ou `AMBOS`.
- O pai é opcional, mas deve pertencer à mesma Empresa.
- Autorreferência e ciclos hierárquicos diretos ou indiretos são bloqueados.
- O caminho completo é calculado pela hierarquia, por exemplo `Administrativo > Comercial > Marketing`.
- A exclusão é lógica e registros excluídos não aparecem nas consultas padrão.
- ContaReceber aceita centros do tipo `RECEITA` ou `AMBOS`.
- ContaPagar aceita centros do tipo `DESPESA` ou `AMBOS`.
- Movimentos de entrada aceitam `RECEITA` ou `AMBOS`; movimentos de saída aceitam `DESPESA` ou `AMBOS`.
- O centro deve pertencer à mesma Empresa da conta ou do movimento.
- Recebimentos e pagamentos propagam o centro da conta para o movimento financeiro gerado.
- Contas, movimentos, fluxo de caixa e dashboard podem ser filtrados por centro de custo.
- Cancelar pagamento estorna a parcela e reconsolida a ContaPagar na mesma transacao.

## Plano de contas

- Toda conta pertence obrigatoriamente a uma Empresa ativa.
- O código é único por Empresa entre registros ativos e não excluídos.
- Contas `SINTETICA` agrupam outras contas e não recebem lançamentos.
- Contas `ANALITICA` podem receber lançamentos financeiros.
- A natureza pode ser `RECEITA`, `DESPESA`, `ATIVO`, `PASSIVO` ou `PATRIMONIO_LIQUIDO`.
- O pai é opcional, mas deve pertencer à mesma Empresa.
- Autorreferência e ciclos hierárquicos diretos ou indiretos são bloqueados.
- O caminho completo é calculado a partir da hierarquia.
- A exclusão é lógica e as consultas padrão não retornam registros excluídos.
- ContaReceber aceita somente planos `ANALITICA` de natureza `RECEITA`.
- ContaPagar aceita somente planos `ANALITICA` de natureza `DESPESA`.
- Movimento financeiro de entrada aceita somente plano `ANALITICA` de natureza `RECEITA`.
- Movimento financeiro de saída aceita somente plano `ANALITICA` de natureza `DESPESA`.
- O plano deve pertencer à mesma Empresa da conta ou do movimento.
- Recebimentos e pagamentos propagam o plano da conta para o movimento financeiro gerado.
- Contas, movimentos, fluxo de caixa, dashboard e DRE podem ser filtrados por plano de conta.

## Conta bancária

- Toda ContaBancaria pertence obrigatoriamente a uma Empresa ativa.
- Filial é opcional, mas quando informada deve estar ativa e pertencer à mesma Empresa.
- Banco código, banco nome, agência, número da conta, tipo e titular são obrigatórios.
- `saldoInicial` assume zero quando não informado.
- `saldoAtual` inicia sempre igual ao `saldoInicial`.
- Documento do titular é opcional, mas quando informado deve possuir 11 ou 14 dígitos numéricos.
- A combinação Empresa + banco + agência + número da conta é única entre registros ativos.
- Apenas uma ContaBancaria principal ativa é permitida por Empresa.
- A exclusão é lógica e registros excluídos não aparecem nas consultas padrão.

## Extrato bancário

- Todo ExtratoBancario pertence obrigatoriamente a uma ContaBancaria ativa.
- `dataInicio` e `dataFim` são obrigatórias, e `dataInicio` não pode ser posterior a `dataFim`.
- `saldoInicial`, `saldoFinal` e `origem` são obrigatórios.
- O status inicial é `IMPORTADO` e `importadoEm` é preenchido automaticamente.
- O hash do arquivo é opcional, mas quando informado deve ser SHA-256 com 64 caracteres hexadecimais.
- Não pode existir outro extrato ativo com o mesmo hash de arquivo.
- O cancelamento não exclui o registro: o extrato permanece ativo com status `CANCELADO` para preservar histórico.
- Extrato `CANCELADO` não pode ser cancelado novamente.
- As consultas aceitam filtros por conta bancária, origem, status, período e ativo.
