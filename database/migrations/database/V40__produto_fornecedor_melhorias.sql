-- Evolui ProdutoFornecedor para compras e cria historico de custo por vigencia.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'produto_fornecedor'
          AND column_name = 'prazo_entrega_dias'
    ) THEN
        ALTER TABLE produto_fornecedor RENAME COLUMN prazo_entrega_dias TO lead_time_dias;
    END IF;
END $$;

ALTER TABLE produto_fornecedor
    ADD COLUMN IF NOT EXISTS prioridade INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'EVENTUAL';

ALTER TABLE produto_fornecedor
    DROP CONSTRAINT IF EXISTS chk_produto_fornecedor_prazo,
    ADD CONSTRAINT chk_produto_fornecedor_lead_time CHECK (lead_time_dias >= 0),
    ADD CONSTRAINT chk_produto_fornecedor_prioridade CHECK (prioridade >= 0),
    ADD CONSTRAINT chk_produto_fornecedor_status CHECK (status IN ('HOMOLOGADO', 'PREFERENCIAL', 'EVENTUAL', 'BLOQUEADO'));

CREATE INDEX IF NOT EXISTS idx_produto_fornecedor_prioridade
    ON produto_fornecedor (produto_id, prioridade)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_fornecedor_status
    ON produto_fornecedor (status)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS produto_fornecedor_preco (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_fornecedor_id UUID NOT NULL,
    preco NUMERIC(15, 4) NOT NULL,
    vigencia_inicio DATE NOT NULL,
    vigencia_fim DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_produto_fornecedor_preco_vinculo FOREIGN KEY (produto_fornecedor_id) REFERENCES produto_fornecedor (id),
    CONSTRAINT chk_produto_fornecedor_preco_valor CHECK (preco >= 0),
    CONSTRAINT chk_produto_fornecedor_preco_vigencia CHECK (vigencia_fim IS NULL OR vigencia_fim >= vigencia_inicio),
    CONSTRAINT chk_produto_fornecedor_preco_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_fornecedor_preco_inicio_ativo
    ON produto_fornecedor_preco (produto_fornecedor_id, vigencia_inicio)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_fornecedor_preco_vinculo
    ON produto_fornecedor_preco (produto_fornecedor_id);

CREATE INDEX IF NOT EXISTS idx_produto_fornecedor_preco_vigencia
    ON produto_fornecedor_preco (produto_fornecedor_id, vigencia_inicio, vigencia_fim)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_fornecedor_preco_ativo
    ON produto_fornecedor_preco (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;
