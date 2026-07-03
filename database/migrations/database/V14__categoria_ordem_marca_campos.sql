-- Refina Categoria e Marca para o fluxo completo da fase 2.
ALTER TABLE categoria ADD COLUMN IF NOT EXISTS ordem INTEGER NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_categoria_pai_ordem_nome
    ON categoria (categoria_pai_id, ordem, nome)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON COLUMN categoria.ordem IS 'Ordem manual para exibicao previsivel da arvore de categorias';

ALTER TABLE marca ADD COLUMN IF NOT EXISTS site VARCHAR(255);
ALTER TABLE marca ADD COLUMN IF NOT EXISTS fabricante VARCHAR(120);

ALTER TABLE marca DROP CONSTRAINT IF EXISTS uk_marca_nome;

CREATE UNIQUE INDEX IF NOT EXISTS uk_marca_nome_ativo
    ON marca (LOWER(nome))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_marca_ativo_nome
    ON marca (ativo, nome)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON COLUMN marca.site IS 'Site opcional da marca';
COMMENT ON COLUMN marca.fabricante IS 'Fabricante associado como texto livre inicial';
