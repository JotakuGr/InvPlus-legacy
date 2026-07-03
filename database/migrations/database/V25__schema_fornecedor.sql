-- Cria o papel Fornecedor vinculado a uma Pessoa existente.
CREATE TABLE IF NOT EXISTS fornecedor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL,
    condicao_pagamento_id UUID,
    prazo_medio_entrega INTEGER NOT NULL DEFAULT 0,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_fornecedor_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT chk_fornecedor_prazo_medio_entrega CHECK (prazo_medio_entrega >= 0),
    CONSTRAINT chk_fornecedor_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_fornecedor_pessoa_ativo
    ON fornecedor (pessoa_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_fornecedor_pessoa
    ON fornecedor (pessoa_id);

CREATE INDEX IF NOT EXISTS idx_fornecedor_ativo
    ON fornecedor (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;
