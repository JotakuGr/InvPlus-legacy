-- Cria o papel comercial Cliente vinculado ao cadastro base Pessoa.
CREATE TABLE IF NOT EXISTS cliente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL,
    limite_credito NUMERIC(15,2) NOT NULL DEFAULT 0,
    condicao_pagamento_id UUID,
    tabela_preco_id UUID,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_cliente_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT chk_cliente_limite_credito CHECK (limite_credito >= 0),
    CONSTRAINT chk_cliente_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_cliente_pessoa_ativo
    ON cliente (pessoa_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_cliente_pessoa
    ON cliente (pessoa_id);

CREATE INDEX IF NOT EXISTS idx_cliente_ativo
    ON cliente (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON TABLE cliente IS 'Papel comercial de cliente associado a uma pessoa existente';
COMMENT ON COLUMN cliente.pessoa_id IS 'Pessoa vinculada ao papel de cliente';
COMMENT ON COLUMN cliente.limite_credito IS 'Limite de credito comercial do cliente';
