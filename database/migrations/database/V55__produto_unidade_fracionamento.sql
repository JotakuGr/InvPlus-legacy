-- Adiciona controle de fracionamento por unidade alternativa do produto.
ALTER TABLE produto_unidade
    ADD COLUMN IF NOT EXISTS permite_fracionamento BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_produto_unidade_fracionamento
    ON produto_unidade (permite_fracionamento)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON COLUMN produto_unidade.permite_fracionamento IS 'Indica se esta unidade permite operacoes fracionadas';
