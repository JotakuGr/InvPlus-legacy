-- Adiciona classificacao operacional do produto para reduzir regras condicionais nos modulos futuros.
ALTER TABLE produto
    ADD COLUMN IF NOT EXISTS tipo_produto VARCHAR(40) NOT NULL DEFAULT 'PRODUTO',
    ADD COLUMN IF NOT EXISTS natureza_produto VARCHAR(20) NOT NULL DEFAULT 'AMBOS';

ALTER TABLE produto
    DROP CONSTRAINT IF EXISTS chk_produto_tipo_produto;

ALTER TABLE produto
    ADD CONSTRAINT chk_produto_tipo_produto CHECK (
        tipo_produto IN (
            'PRODUTO',
            'SERVICO',
            'MATERIA_PRIMA',
            'INSUMO',
            'EMBALAGEM',
            'COMBUSTIVEL',
            'ATIVO_IMOBILIZADO',
            'USO_CONSUMO'
        )
    );

ALTER TABLE produto
    DROP CONSTRAINT IF EXISTS chk_produto_natureza_produto;

ALTER TABLE produto
    ADD CONSTRAINT chk_produto_natureza_produto CHECK (
        natureza_produto IN ('VENDA', 'COMPRA', 'AMBOS')
    );

CREATE INDEX IF NOT EXISTS idx_produto_tipo_natureza
    ON produto (tipo_produto, natureza_produto)
    WHERE ativo = TRUE AND deleted_at IS NULL;
