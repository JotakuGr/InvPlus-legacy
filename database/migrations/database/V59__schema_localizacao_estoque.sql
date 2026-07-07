-- Cria localizacoes internas de estoque dentro de cada deposito.
CREATE TABLE IF NOT EXISTS localizacao_estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deposito_id UUID NOT NULL,
    codigo CITEXT NOT NULL,
    descricao VARCHAR(160),
    rua VARCHAR(60),
    corredor VARCHAR(60),
    prateleira VARCHAR(60),
    nivel VARCHAR(60),
    posicao VARCHAR(60),
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    bloqueada BOOLEAN NOT NULL DEFAULT FALSE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT fk_localizacao_estoque_deposito FOREIGN KEY (deposito_id) REFERENCES deposito (id),
    CONSTRAINT chk_localizacao_estoque_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_localizacao_estoque_deposito_codigo_ativo
    ON localizacao_estoque (deposito_id, LOWER(codigo))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_localizacao_estoque_deposito_principal_ativo
    ON localizacao_estoque (deposito_id)
    WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_localizacao_estoque_deposito
    ON localizacao_estoque (deposito_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_localizacao_estoque_bloqueada
    ON localizacao_estoque (bloqueada)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_localizacao_estoque_ativo
    ON localizacao_estoque (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON TABLE localizacao_estoque IS 'Posicoes fisicas ou logicas dentro de um deposito';
COMMENT ON COLUMN localizacao_estoque.principal IS 'Indica a localizacao padrao do deposito';
COMMENT ON COLUMN localizacao_estoque.bloqueada IS 'Indica bloqueio operacional temporario da localizacao';
