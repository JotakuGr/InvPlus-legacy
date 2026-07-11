# Contexto empresarial

- Um usuario pode acessar varias empresas por `UsuarioEmpresa`.
- Cada access token possui no maximo uma empresa e uma filial ativa.
- Sem `acessoTodasFiliais`, apenas filiais explicitamente vinculadas sao permitidas.
- `acessoTodasFiliais=true` dispensa e bloqueia vinculos especificos.
- Vínculos inativos sao revalidados e bloqueados em cada request contextualizado.
- Selection tokens duram cinco minutos e nao possuem acesso operacional.
- Refresh preserva empresa e filial e nao permite troca de contexto.
- Durante a migracao gradual, usuarios sem vinculo continuam com token legado sem contexto; APIs migradas exigem contexto.
