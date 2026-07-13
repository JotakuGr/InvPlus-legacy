# Pendencias da refatoracao do contexto empresarial

Documento temporario de acompanhamento. As categorias refletem a suite executada em 2026-07-12.

## Autenticacao

| Teste | Categoria | Status | Correcao |
|---|---|---|---|
| `AuthControllerTests` - login com uma empresa | `LOGIN_SEM_VINCULO_EMPRESARIAL` | CONCLUIDO | Fixture cria `UsuarioEmpresa` ativo e valida access/refresh contextualizados |
| `AuthControllerTests` - login sem empresa | `LOGIN_SEM_VINCULO_EMPRESARIAL` | CONCLUIDO | Login operacional e negado com 403 |
| `AuthControllerTests` - access sem empresa | `TOKEN_ACCESS_SEM_EMPRESA` | CONCLUIDO | Token e aceito apenas como fixture negativa e endpoint operacional retorna 401 |
| `AuthControllerTests` - multiplas empresas | `TOKEN_DE_SELECAO` | CONCLUIDO | Login emite apenas selection token e exige selecao explicita |
| `AuthControllerTests` - selection token operacional | `TOKEN_DE_SELECAO` | CONCLUIDO | Estoque, Filiais e ProdutoPreco retornam 401 |
| `AuthControllerTests` - refresh contextual | `REFRESH_SEM_CONTEXTO` | CONCLUIDO | Refresh preserva usuario/empresa e revalida o vinculo ativo |
| Helpers locais de token | `TESTE_COM_HELPER_ANTIGO` | CONCLUIDO | Cenarios operacionais usam tokens contextualizados e vinculo empresarial ativo |
| Login operacional sem empresa | `EXPECTATIVA_DE_CONTRATO_ANTIGO` | CONCLUIDO | Expectativa removida; usuario sem empresa nao recebe access token |

## Modulos

| Teste ou grupo | Categoria | Modulo | Correcao |
|---|---|---|---|
| FinanceiroDreBusinessFlowIntegrationTest | C | Financeiro | CONCLUIDO: contexto ativado com a empresa do cenario |
| FinanceiroDashboardBusinessFlowIntegrationTest | C | Financeiro | CONCLUIDO: `TestEmpresaContext` configurado antes das chamadas diretas |
| Pagamento/Recebimento BusinessFlow | C | Financeiro | CONCLUIDO: empresa da conta/parcela preservada no token e no contexto |
| Painel/Pendencias ServiceTest | C/G | Conciliacao | CONCLUIDO: `EmpresaContext` configurado e overloads legados removidos |
| Controllers financeiros antigos | A/B | Financeiro | CONCLUIDO: access token contextual e vinculo `UsuarioEmpresa` ativo |
| Deposito/Estoque BusinessFlow | A/D | Estoque | CONCLUIDO: token contextual e contratos sem `empresaId` |
| Controllers de estoque | A/D/E | Estoque | CONCLUIDO: repositories e Specifications isolados por empresa |
| ComprasBusinessFlowIntegrationTest | A/D/E | Compras | CONCLUIDO: pedido, entrada, itens e estoque isolados por empresa |
| PedidoVenda/VendaFaturamento flows | A/D/E | Vendas | CONCLUIDO: contratos contextuais, estoque/financeiro propagados e isolamento A/B |
| ProdutoPreco tests | A/D/E | Produtos | CONCLUIDO: request contextual e isolamento por empresa ativa |
| Controllers legados remanescentes | A/B | API | CONCLUIDO: inicializador de contexto mantem as fixtures antigas contextualizadas |

## Categorias

- A: token sem `empresa_id`
- B: usuario sem `UsuarioEmpresa`
- C: service sem `EmpresaContext` configurado
- D: assinatura antiga com `empresaId`
- E: entidade de outra empresa
- F: filial sem acesso
- G: overload legado
- H: falha funcional real

## Infraestrutura concluida

- `TestEmpresaContext` primario no profile `test`, isolado por `ThreadLocal`.
- Fallback para o `AuthenticatedUserDetails` em testes MockMvc.
- `AbstractContextoEmpresarialIntegrationTest` com limpeza automatica.
- `JwtTestFactory` para access token contextual, token sem empresa e selection token.

## Blocos

- Autenticacao e tokens antigos: CONCLUIDO (`23` testes focados, sem falhas).
- Filiais: CONCLUIDO (`5` testes de controller, incluindo isolamento multiempresa).
- ProdutoPreco: CONCLUIDO (`20` testes focados e isolamento A/B no bloco combinado).
- Estoque: CONCLUIDO (deposito, localizacao, saldo, movimentacao, reserva e carga concorrente).
- Compras: CONCLUIDO (`7` cenarios de fluxo, seguranca, isolamento e concorrencia).
- Vendas: CONCLUIDO (`121` testes no bloco combinado Venda, Estoque e ContaReceber, incluindo fluxo empresarial A/B).
- Suite completa: CONCLUIDO (`147` suites, `692` testes, sem falhas, erros ou testes ignorados).
