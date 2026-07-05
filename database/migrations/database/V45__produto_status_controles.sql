-- Adiciona estado comercial/operacional do produto e flags explicitas de compra, venda e estoque.
ALTER TABLE produto
    ADD COLUMN IF NOT EXISTS status_produto VARCHAR(30) NOT NULL DEFAULT 'ATIVO',
    ADD COLUMN IF NOT EXISTS controla_estoque BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS permite_compra BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS permite_venda BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE produto
    DROP CONSTRAINT IF EXISTS chk_produto_status_produto;

ALTER TABLE produto
    ADD CONSTRAINT chk_produto_status_produto CHECK (
        status_produto IN (
            'EM_DESENVOLVIMENTO',
            'ATIVO',
            'DESCONTINUADO',
            'BLOQUEADO',
            'SUSPENSO'
        )
    );

CREATE INDEX IF NOT EXISTS idx_produto_status
    ON produto (status_produto)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_controles_operacionais
    ON produto (controla_estoque, permite_compra, permite_venda)
    WHERE ativo = TRUE AND deleted_at IS NULL;
