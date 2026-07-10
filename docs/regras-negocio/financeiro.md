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

## Lançamento de extrato bancário

- Todo lançamento pertence obrigatoriamente a um ExtratoBancario ativo.
- ExtratoBancario com status `CANCELADO` não pode receber lançamentos.
- `dataLancamento`, `descricao`, `tipo` e `valor` são obrigatórios.
- `valor` deve ser maior que zero.
- `tipo` aceita `CREDITO` ou `DEBITO`.
- `categoriaBancaria` assume `OUTRO` quando não informada.
- `conciliado` inicia sempre como `false`.
- O identificador bancário é opcional, mas deve ser único dentro do mesmo extrato ativo quando informado.
- O mesmo identificador bancário pode existir em extratos diferentes.
- Lançamento conciliado não pode ser editado nem excluído logicamente.
- O service já expõe métodos internos para marcar e desmarcar conciliação, preparando a futura ConciliacaoBancaria.
- As consultas aceitam filtros por extrato, conta bancária, tipo, categoria, conciliação, período e ativo.

## Conciliação bancária

- Uma conciliação vincula exatamente um `ExtratoBancarioLancamento` a um `MovimentoFinanceiro`.
- `CREDITO` bancário somente concilia com movimento `ENTRADA`; `DEBITO` somente com `SAIDA`.
- A primeira versão exige igualdade exata entre `valorExtrato` e `valorMovimento`; a diferença deve ser zero.
- Lançamento, extrato, movimento e conta bancária precisam estar ativos e o extrato não pode estar cancelado.
- Um lançamento e um movimento podem possuir somente uma conciliação com status `CONCILIADA`.
- A proteção contra duplicidade combina validação de domínio, optimistic lock e índices únicos parciais.
- A conciliação não usa soft delete. O cancelamento muda o status para `CANCELADA` e preserva usuário, data e motivo.
- Ao conciliar, o lançamento fica bloqueado para edição/exclusão; ao cancelar, ele volta a ficar disponível.
- O extrato fica `IMPORTADO` sem conciliações, `PROCESSANDO` quando parcial e `CONCILIADO` quando todas as linhas ativas estão conciliadas.
- Criação, marcação do lançamento e recálculo do extrato acontecem na mesma transação.

## Importação de extratos OFX

- O processo de importação não substitui `ExtratoBancario` nem `ExtratoBancarioLancamento`.
- `OfxParser` converte OFX 1.x/2.x para DTOs intermediários independentes do domínio.
- O conteúdo é lido uma única vez em memória, limitado a 10 MB e nunca salvo em pasta pública.
- O nome original é sanitizado e o SHA-256 completo impede reimportação do mesmo arquivo.
- Banco, agência e conta são normalizados antes da comparação com `ContaBancaria`.
- Valores positivos viram `CREDITO`; negativos viram `DEBITO` com valor absoluto no domínio.
- `FEE` e `SRVCHG` mapeiam para `TARIFA`, `INT` para `JUROS`, `XFER` para `TRANSFERENCIA`; demais tipos usam `OUTRO`.
- FITID duplicado dentro do arquivo é ignorado e registrado como alerta.
- A criação de extrato e lançamentos é atômica; falha em uma linha desfaz todo o domínio bancário criado.
- O saldo final importado pertence ao extrato e não altera `ContaBancaria.saldoAtual`.
- Todos os lançamentos entram com `conciliado = false`; conciliação permanece responsabilidade de `ConciliacaoBancaria`.
