-- Adiciona controles operacionais para depositos fisicos, virtuais ou bloqueados.
ALTER TABLE deposito
    ADD COLUMN IF NOT EXISTS controla_estoque BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS bloqueado BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_deposito_controla_estoque
    ON deposito (controla_estoque)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_deposito_bloqueado
    ON deposito (bloqueado)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON COLUMN deposito.controla_estoque IS 'Indica se o deposito participa do saldo fisico de estoque';
COMMENT ON COLUMN deposito.bloqueado IS 'Indica bloqueio operacional temporario para movimentacoes';
