-- Refina Empresa para a fase 2 de Cadastros.
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS inscricao_municipal VARCHAR(30);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS regime_tributario VARCHAR(60);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS crt INTEGER NOT NULL DEFAULT 3;
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE empresa DROP CONSTRAINT IF EXISTS uk_empresa_cnpj;

CREATE UNIQUE INDEX IF NOT EXISTS uk_empresa_cnpj_ativo
    ON empresa (cnpj)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_empresa_cnpj
    ON empresa (cnpj);

CREATE INDEX IF NOT EXISTS idx_empresa_ativo
    ON empresa (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_empresa_crt_valido'
    ) THEN
        ALTER TABLE empresa
            ADD CONSTRAINT chk_empresa_crt_valido
            CHECK (crt IN (1, 2, 3, 4));
    END IF;
END $$;

COMMENT ON COLUMN empresa.inscricao_municipal IS 'Inscricao municipal da empresa quando aplicavel';
COMMENT ON COLUMN empresa.regime_tributario IS 'Regime tributario declarado para uso fiscal';
COMMENT ON COLUMN empresa.crt IS 'Codigo de Regime Tributario: 1, 2, 3 ou 4';
