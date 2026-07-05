-- Cria o vinculo entre Produto e Fornecedor para compras, cotacoes e historico de custo.
CREATE TABLE IF NOT EXISTS produto_fornecedor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL,
    fornecedor_id UUID NOT NULL,
    codigo_fornecedor VARCHAR(120),
    preco_fornecedor NUMERIC(15, 4) NOT NULL DEFAULT 0,
    prazo_entrega_dias INTEGER NOT NULL DEFAULT 0,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_produto_fornecedor_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_produto_fornecedor_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor (id),
    CONSTRAINT chk_produto_fornecedor_preco CHECK (preco_fornecedor >= 0),
    CONSTRAINT chk_produto_fornecedor_prazo CHECK (prazo_entrega_dias >= 0),
    CONSTRAINT chk_produto_fornecedor_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_fornecedor_ativo
    ON produto_fornecedor (produto_id, fornecedor_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_fornecedor_principal_ativo
    ON produto_fornecedor (produto_id)
    WHERE principal = TRUE
      AND ativo = TRUE
      AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_fornecedor_produto
    ON produto_fornecedor (produto_id);

CREATE INDEX IF NOT EXISTS idx_produto_fornecedor_fornecedor
    ON produto_fornecedor (fornecedor_id);

CREATE INDEX IF NOT EXISTS idx_produto_fornecedor_ativo
    ON produto_fornecedor (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;
