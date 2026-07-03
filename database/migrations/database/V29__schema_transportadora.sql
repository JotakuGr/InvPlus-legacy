-- Cria o papel Transportadora vinculado a uma Pessoa existente.
CREATE TABLE IF NOT EXISTS transportadora (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL,
    antt VARCHAR(40),
    tipo_veiculo_principal VARCHAR(80),
    capacidade_carga_kg NUMERIC(15,3) NOT NULL DEFAULT 0,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_transportadora_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT chk_transportadora_capacidade_carga CHECK (capacidade_carga_kg >= 0),
    CONSTRAINT chk_transportadora_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_transportadora_pessoa_ativo
    ON transportadora (pessoa_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_transportadora_pessoa
    ON transportadora (pessoa_id);

CREATE INDEX IF NOT EXISTS idx_transportadora_ativo
    ON transportadora (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;
