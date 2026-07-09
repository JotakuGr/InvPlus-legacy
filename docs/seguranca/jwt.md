# JWT e Autenticação

O invPlus usa Spring Security com autenticação stateless por JWT.

## Fluxo

```text
POST /api/v1/auth/login
  ↓
Access token + refresh token
  ↓
Authorization: Bearer <access_token>
  ↓
POST /api/v1/auth/refresh quando necessário
  ↓
POST /api/v1/auth/logout para revogar refresh token
```

## Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/auth/login` | Autenticação |
| POST | `/api/v1/auth/refresh` | Renovação |
| POST | `/api/v1/auth/logout` | Logout |
| GET | `/api/v1/auth/me` | Usuário autenticado |

## Variáveis

```text
INVPLUS_JWT_SECRET
INVPLUS_JWT_EXPIRATION_MINUTES
INVPLUS_JWT_REFRESH_EXPIRATION_DAYS
```

## Erros Esperados

| Caso | Status |
|---|---|
| Sem token | 401 |
| Token inválido | 401 |
| Sem permissão | 403 |
| Recurso inexistente | 404 |
| Conflito de regra | 409 |

## Observações

- `INVPLUS_JWT_SECRET` deve ser forte e obrigatório fora de ambiente local.
- Refresh tokens são auditados e podem ser revogados.
- Rotas de upload público local podem ser liberadas apenas em desenvolvimento.
