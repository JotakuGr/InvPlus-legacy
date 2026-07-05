-- Cria unidades alternativas do Produto para compra, venda, estoque e conversao.
CREATE TABLE IF NOT EXISTS produto_unidade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL,
    unidade_medida_id UUID NOT NULL,
    fator_conversao NUMERIC(18, 6) NOT NULL,
    unidade_padrao BOOLEAN NOT NULL DEFAULT FALSE,
    permite_compra BOOLEAN NOT NULL DEFAULT FALSE,
    permite_venda BOOLEAN NOT NULL DEFAULT FALSE,
    permite_estoque BOOLEAN NOT NULL DEFAULT FALSE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT fk_produto_unidade_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_produto_unidade_unidade FOREIGN KEY (unidade_medida_id) REFERENCES unidade_medida (id),
    CONSTRAINT chk_produto_unidade_fator CHECK (fator_conversao > 0),
    CONSTRAINT chk_produto_unidade_padrao_fator CHECK (unidade_padrao = FALSE OR fator_conversao = 1),
    CONSTRAINT chk_produto_unidade_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_unidade_produto_unidade_ativo
    ON produto_unidade (produto_id, unidade_medida_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_unidade_padrao_ativo
    ON produto_unidade (produto_id)
    WHERE unidade_padrao = TRUE AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_unidade_produto
    ON produto_unidade (produto_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_unidade_unidade
    ON produto_unidade (unidade_medida_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_unidade_ativo
    ON produto_unidade (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON TABLE produto_unidade IS 'Unidades alternativas do Produto para compra, venda, estoque e conversao';
COMMENT ON COLUMN produto_unidade.fator_conversao IS 'Fator em relacao a unidade padrao do produto';
COMMENT ON COLUMN produto_unidade.unidade_padrao IS 'Indica a unidade base de conversao do produto';
