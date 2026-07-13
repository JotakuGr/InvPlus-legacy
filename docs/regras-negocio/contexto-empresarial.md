# Contexto empresarial

- Um usuario pode acessar varias empresas por `UsuarioEmpresa`.
- Cada access token possui no maximo uma empresa e uma filial ativa.
- Sem `acessoTodasFiliais`, apenas filiais explicitamente vinculadas sao permitidas.
- `acessoTodasFiliais=true` dispensa e bloqueia vinculos especificos.
- Vínculos inativos sao revalidados e bloqueados em cada request contextualizado.
- Selection tokens duram cinco minutos e nao possuem acesso operacional.
- Refresh preserva empresa e filial e nao permite troca de contexto.
- Durante a migracao gradual, usuarios sem vinculo continuam com token legado sem contexto; APIs migradas exigem contexto.
# Isolamento de recursos

- Services empresariais consomem `EmpresaContext`; somente sua implementacao acessa o
  `SecurityContextHolder`.
- A protecao principal ocorre no repository por `id + empresa` e nas Specifications obrigatorias.
- `EmpresaResourceAccessValidator` fornece uma segunda verificacao para recursos ja carregados.
- UUID de recurso pertencente a outra empresa retorna `404`, sem revelar sua existencia.
- Troca para empresa ou filial sem vinculo retorna `403`.
- Relatorios e agregacoes sempre usam a empresa do access token.

## Frontend

O Angular executa login real na API e trata `SELECAO_EMPRESA_NECESSARIA`. A selecao envia o
token temporario apenas para `/auth/contexto/selecionar`; depois disso, o interceptor utiliza o
access token contextualizado. Trocas futuras de empresa devem usar `/auth/contexto/trocar`, sem
enviar `empresaId` nos recursos de negocio.

O modulo Estoque aplica esse contrato em depositos, localizacoes, saldos,
movimentacoes e reservas. A empresa nunca e um filtro opcional nessas consultas.

O modulo Compras segue o mesmo contrato para pedidos, entradas e itens. A integracao com
Estoque preserva a empresa autenticada durante a efetivacao da entrada.

O modulo Vendas aplica o contexto em pedidos, itens, faturamentos e leituras agregadas.
A aprovacao reserva apenas estoque da empresa ativa; o faturamento baixa essas reservas
e gera ContaReceber na mesma empresa.
