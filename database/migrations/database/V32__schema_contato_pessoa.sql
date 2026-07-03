-- Cria contatos vinculados a Pessoa para areas como Financeiro, Compras, Comercial e RH.
CREATE TABLE IF NOT EXISTS contato_pessoa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL,
    nome VARCHAR(160) NOT NULL,
    telefone VARCHAR(30),
    celular VARCHAR(30),
    email CITEXT,
    cargo VARCHAR(120),
    tipo VARCHAR(60),
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_contato_pessoa_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT chk_contato_pessoa_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE INDEX IF NOT EXISTS idx_contato_pessoa_pessoa
    ON contato_pessoa (pessoa_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_contato_pessoa_email
    ON contato_pessoa (email)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_contato_pessoa_principal
    ON contato_pessoa (pessoa_id, principal)
    WHERE ativo = TRUE AND deleted_at IS NULL;
