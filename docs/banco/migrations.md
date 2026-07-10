# Migrations Flyway

As migrations ficam em `database/migrations/database` e são executadas pelo Flyway.

## Lista Atual

| Versão | Arquivo |
|---|---|
| V1 | `V1__init_extensions.sql` |
| V2 | `V2__schema_seguranca.sql` |
| V3 | `V3__seed_seguranca_inicial.sql` |
| V4 | `V4__refresh_token.sql` |
| V5 | `V5__auditoria_refresh_token.sql` |
| V6 | `V6__refresh_token_ip_varchar.sql` |
| V7 | `V7__schema_cadastros.sql` |
| V8 | `V8__endereco_uf_varchar.sql` |
| V9 | `V9__cadastros_fundamentais.sql` |
| V10 | `V10__cadastros_fase_2.sql` |
| V11 | `V11__permissoes_cadastros_fase_2.sql` |
| V12 | `V12__categoria_hierarquia_nome_unico.sql` |
| V13 | `V13__permissao_categoria_wildcard.sql` |
| V14 | `V14__categoria_ordem_marca_campos.sql` |
| V15 | `V15__permissao_marca_wildcard.sql` |
| V16 | `V16__empresa_melhorias.sql` |
| V17 | `V17__permissao_empresa_wildcard.sql` |
| V18 | `V18__arquivo_melhorias.sql` |
| V19 | `V19__permissao_arquivo_wildcard.sql` |
| V20 | `V20__arquivo_colunas_legadas_opcionais.sql` |
| V21 | `V21__schema_pessoa.sql` |
| V22 | `V22__permissoes_pessoa.sql` |
| V23 | `V23__schema_cliente.sql` |
| V24 | `V24__permissoes_cliente.sql` |
| V25 | `V25__schema_fornecedor.sql` |
| V26 | `V26__permissoes_fornecedor.sql` |
| V27 | `V27__schema_funcionario.sql` |
| V28 | `V28__permissoes_funcionario.sql` |
| V29 | `V29__schema_transportadora.sql` |
| V30 | `V30__permissoes_transportadora.sql` |
| V31 | `V31__pessoa_role.sql` |
| V32 | `V32__schema_contato_pessoa.sql` |
| V33 | `V33__permissoes_contato_pessoa.sql` |
| V34 | `V34__schema_documento_pessoa.sql` |
| V35 | `V35__permissoes_documento_pessoa.sql` |
| V36 | `V36__schema_produto.sql` |
| V37 | `V37__permissoes_produto.sql` |
| V38 | `V38__schema_produto_fornecedor.sql` |
| V39 | `V39__permissoes_produto_fornecedor.sql` |
| V40 | `V40__produto_fornecedor_melhorias.sql` |
| V41 | `V41__permissoes_produto_fornecedor_preco.sql` |
| V42 | `V42__schema_produto_fiscal.sql` |
| V43 | `V43__permissoes_produto_fiscal.sql` |
| V44 | `V44__produto_tipo_natureza.sql` |
| V45 | `V45__produto_status_controles.sql` |
| V46 | `V46__arquivo_cloudflare_r2.sql` |
| V47 | `V47__schema_produto_imagem.sql` |
| V48 | `V48__permissoes_produto_imagem.sql` |
| V49 | `V49__schema_produto_codigo_barras.sql` |
| V50 | `V50__permissoes_produto_codigo_barras.sql` |
| V51 | `V51__schema_produto_preco.sql` |
| V52 | `V52__permissoes_produto_preco.sql` |
| V53 | `V53__schema_produto_unidade.sql` |
| V54 | `V54__permissoes_produto_unidade.sql` |
| V55 | `V55__produto_unidade_fracionamento.sql` |
| V56 | `V56__schema_deposito.sql` |
| V57 | `V57__permissoes_deposito.sql` |
| V58 | `V58__deposito_controles_operacionais.sql` |
| V59 | `V59__schema_localizacao_estoque.sql` |
| V60 | `V60__permissoes_localizacao_estoque.sql` |
| V61 | `V61__schema_estoque.sql` |
| V62 | `V62__permissoes_estoque.sql` |
| V63 | `V63__schema_movimentacao_estoque.sql` |
| V64 | `V64__permissoes_movimentacao_estoque.sql` |
| V65 | `V65__schema_reserva_estoque.sql` |
| V66 | `V66__permissoes_reserva_estoque.sql` |
| V67 | `V67__schema_pedido_compra.sql` |
| V68 | `V68__permissoes_pedido_compra.sql` |
| V69 | `V69__schema_pedido_compra_item.sql` |
| V70 | `V70__permissoes_pedido_compra_item.sql` |
| V71 | `V71__schema_entrada_mercadoria.sql` |
| V72 | `V72__permissoes_entrada_mercadoria.sql` |
| V73 | `V73__schema_entrada_mercadoria_item.sql` |
| V74 | `V74__permissoes_entrada_mercadoria_item.sql` |
| V75 | `V75__compras_consolidacao.sql` |
| V76 | `V76__schema_pedido_venda.sql` |
| V77 | `V77__permissoes_pedido_venda.sql` |
| V78 | `V78__schema_pedido_venda_item.sql` |
| V79 | `V79__permissoes_pedido_venda_item.sql` |
| V80 | `V80__ajuste_reserva_estoque_origem_unica.sql` |
| V81 | `V81__schema_venda_faturamento.sql` |
| V82 | `V82__permissoes_venda_faturamento.sql` |
| V83 | `V83__schema_forma_pagamento.sql` |
| V84 | `V84__permissoes_forma_pagamento.sql` |
| V85 | `V85__schema_condicao_pagamento.sql` |
| V86 | `V86__permissoes_condicao_pagamento.sql` |
| V87 | `V87__condicao_pagamento_parcelas.sql` |
| V88 | `V88__schema_conta_receber.sql` |
| V89 | `V89__permissoes_conta_receber.sql` |
| V90 | `V90__schema_parcela_receber.sql` |
| V91 | `V91__permissoes_parcela_receber.sql` |
| V92 | `V92__schema_recebimento.sql` |
| V93 | `V93__permissoes_recebimento.sql` |
| V94 | `V94__venda_faturamento_conta_receber.sql` |
| V95 | `V95__schema_conta_pagar.sql` |
| V96 | `V96__permissoes_conta_pagar.sql` |
| V97 | `V97__schema_parcela_pagar.sql` |
| V98 | `V98__permissoes_parcela_pagar.sql` |
| V99 | `V99__schema_pagamento.sql` |
| V100 | `V100__permissoes_pagamento.sql` |
| V101 | `V101__schema_movimento_financeiro.sql` |
| V102 | `V102__permissoes_movimento_financeiro.sql` |
| V103 | `V103__permissoes_relatorios_financeiros.sql` |
| V104 | `V104__schema_centro_custo.sql` |
| V105 | `V105__permissoes_centro_custo.sql` |
| V106 | `V106__financeiro_centro_custo.sql` |
| V107 | `V107__schema_plano_conta.sql` |
| V108 | `V108__permissoes_plano_conta.sql` |
| V109 | `V109__financeiro_plano_conta.sql` |
| V110 | `V110__schema_conta_bancaria.sql` |
| V111 | `V111__permissoes_conta_bancaria.sql` |
| V112 | `V112__schema_extrato_bancario.sql` |
| V113 | `V113__permissoes_extrato_bancario.sql` |
| V114 | `V114__schema_extrato_bancario_lancamento.sql` |
| V115 | `V115__permissoes_extrato_bancario_lancamento.sql` |
| V116 | `V116__schema_conciliacao_bancaria.sql` |
| V117 | `V117__permissoes_conciliacao_bancaria.sql` |

## Política de Inserts

- Seeds estruturais de segurança e permissões podem ficar em migrations.
- Dados operacionais devem ser criados pela aplicação, testes ou scripts específicos.
- Migrations devem ser idempotentes quando tratarem permissões e vínculos administrativos.
