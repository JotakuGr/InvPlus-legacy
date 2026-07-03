-- Registra os papeis exercidos por uma Pessoa sem substituir as tabelas especificas de cada papel.
CREATE TABLE IF NOT EXISTS pessoa_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL,
    role VARCHAR(40) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_pessoa_role_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT chk_pessoa_role_valida CHECK (role IN ('CLIENTE', 'FORNECEDOR', 'FUNCIONARIO', 'TRANSPORTADORA')),
    CONSTRAINT chk_pessoa_role_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_pessoa_role_ativo
    ON pessoa_role (pessoa_id, role)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_pessoa_role_pessoa
    ON pessoa_role (pessoa_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_pessoa_role_role
    ON pessoa_role (role)
    WHERE ativo = TRUE AND deleted_at IS NULL;
