-- Separa os dados tributarios do cadastro comercial do produto.
CREATE TABLE IF NOT EXISTS produto_fiscal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL,
    ncm VARCHAR(8) NOT NULL,
    cest VARCHAR(7),
    origem_mercadoria VARCHAR(60) NOT NULL,
    cfop_padrao VARCHAR(10),
    cst_icms VARCHAR(10),
    csosn VARCHAR(10),
    cst_pis VARCHAR(10),
    cst_cofins VARCHAR(10),
    aliquota_icms NUMERIC(7, 4) NOT NULL DEFAULT 0,
    aliquota_ipi NUMERIC(7, 4) NOT NULL DEFAULT 0,
    aliquota_pis NUMERIC(7, 4) NOT NULL DEFAULT 0,
    aliquota_cofins NUMERIC(7, 4) NOT NULL DEFAULT 0,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_produto_fiscal_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT chk_produto_fiscal_ncm CHECK (ncm ~ '^[0-9]{8}$'),
    CONSTRAINT chk_produto_fiscal_cest CHECK (cest IS NULL OR cest ~ '^[0-9]{7}$'),
    CONSTRAINT chk_produto_fiscal_origem CHECK (
        origem_mercadoria IN (
            'NACIONAL',
            'ESTRANGEIRA_IMPORTACAO_DIRETA',
            'ESTRANGEIRA_ADQUIRIDA_MERCADO_INTERNO'
        )
    ),
    CONSTRAINT chk_produto_fiscal_aliquotas CHECK (
        aliquota_icms >= 0
        AND aliquota_ipi >= 0
        AND aliquota_pis >= 0
        AND aliquota_cofins >= 0
    ),
    CONSTRAINT chk_produto_fiscal_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_fiscal_produto_ativo
    ON produto_fiscal (produto_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_fiscal_produto
    ON produto_fiscal (produto_id);

CREATE INDEX IF NOT EXISTS idx_produto_fiscal_ncm
    ON produto_fiscal (ncm)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_fiscal_ativo
    ON produto_fiscal (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;
