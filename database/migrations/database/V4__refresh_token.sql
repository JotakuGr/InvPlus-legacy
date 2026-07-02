CREATE TABLE refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    usuario_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,

    expira_em TIMESTAMPTZ NOT NULL,
    revogado BOOLEAN NOT NULL DEFAULT FALSE,

    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revogado_em TIMESTAMPTZ,

    ip INET,
    user_agent VARCHAR(255),

    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),

    CONSTRAINT fk_refresh_token_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_refresh_token_revogado_em
        CHECK (
            (revogado = FALSE AND revogado_em IS NULL)
            OR
            (revogado = TRUE AND revogado_em IS NOT NULL)
        )
);

CREATE INDEX idx_refresh_token_usuario_ativo
    ON refresh_token (usuario_id, expira_em)
    WHERE revogado = FALSE;

CREATE INDEX idx_refresh_token_expira_em
    ON refresh_token (expira_em);

COMMENT ON TABLE refresh_token IS 'Tokens de renovacao de sessao armazenados como hash';
COMMENT ON COLUMN refresh_token.token_hash IS 'Hash SHA-256 do refresh token. O token puro nunca deve ser persistido';
