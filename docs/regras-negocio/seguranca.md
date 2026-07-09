# Regras de Negócio - Segurança

- Autenticação usa JWT com access token e refresh token.
- Rotas protegidas exigem `Authorization: Bearer <token>`.
- Usuário sem token recebe `401`.
- Usuário autenticado sem permissão recebe `403`.
- Permissões seguem o padrão `RECURSO:ACAO` e `RECURSO:*`.
- Refresh tokens podem ser revogados no logout.
- Seeds estruturais de segurança devem ser idempotentes.
