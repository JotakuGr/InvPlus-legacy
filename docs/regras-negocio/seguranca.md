# Seguranca

- `ACCESS` autentica APIs operacionais.
- `COMPANY_SELECTION` e aceito somente para selecionar contexto.
- Refresh tokens sao opacos, persistidos como hash e preservam o contexto empresarial.
- Troca de empresa gera novos tokens e nunca recebe `usuarioId` do cliente.
- Contexto empresarial complementa authorities e nao substitui permissoes.
