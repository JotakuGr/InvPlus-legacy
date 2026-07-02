-- Ajusta Categoria para a regra da fase 2: nome unico ativo e hierarquia opcional.
ALTER TABLE categoria ADD COLUMN IF NOT EXISTS categoria_pai_id UUID;

ALTER TABLE categoria DROP CONSTRAINT IF EXISTS uk_categoria_nome_tipo;

CREATE UNIQUE INDEX IF NOT EXISTS uk_categoria_nome_ativo
    ON categoria (LOWER(nome))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_categoria_pai
    ON categoria (categoria_pai_id)
    WHERE categoria_pai_id IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_categoria_pai'
    ) THEN
        ALTER TABLE categoria
            ADD CONSTRAINT fk_categoria_pai
            FOREIGN KEY (categoria_pai_id)
            REFERENCES categoria(id)
            ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_categoria_nao_pai_dela_mesma'
    ) THEN
        ALTER TABLE categoria
            ADD CONSTRAINT chk_categoria_nao_pai_dela_mesma
            CHECK (categoria_pai_id IS NULL OR categoria_pai_id <> id);
    END IF;
END $$;

COMMENT ON COLUMN categoria.categoria_pai_id IS 'Categoria pai opcional para montar hierarquia sem ciclos';
