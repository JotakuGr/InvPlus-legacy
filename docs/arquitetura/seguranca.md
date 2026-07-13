# Arquitetura de seguranca

```text
Usuario -> UsuarioEmpresa -> Empresa
                    |
                    -> UsuarioEmpresaFilial -> Filial

JWT ACCESS -> JwtAuthenticationFilter -> UsuarioEmpresaAccessValidator
           -> AuthenticatedUserDetails -> EmpresaContext
```

O principal armazena somente UUIDs. Entidades JPA nao sao mantidas no `SecurityContext`.
# Acesso empresarial a recursos

Controllers e services dependem de `EmpresaContext`; somente `SecurityEmpresaContext` consulta o
Spring Security diretamente. Repositories filtram por empresa e `EmpresaResourceAccessValidator`
atua como defesa adicional para recursos ja carregados.

No Estoque, o filtro contextual e aplicado antes de qualquer busca de deposito,
localizacao, saldo, movimentacao ou reserva. UUIDs de outra empresa nao revelam
a existencia do recurso e retornam `404`.

Em Vendas, o mesmo filtro protege pedidos, itens e faturamentos. A empresa nao faz parte
do contrato de entrada e e propagada internamente para Estoque e Financeiro.

O mesmo limite protege pedidos de compra, itens, entradas de mercadoria e itens recebidos.
Tokens operacionais sem `empresa_id` sao rejeitados pelo filtro antes do controller.
