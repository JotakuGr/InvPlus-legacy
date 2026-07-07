-- Registra historico imutavel de tudo que alterou o saldo de estoque.
CREATE TABLE IF NOT EXISTS movimentacao_estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estoque_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    empresa_id UUID NOT NULL,
    filial_id UUID,
    deposito_id UUID NOT NULL,
    localizacao_id UUID NOT NULL,
    tipo_movimentacao VARCHAR(40) NOT NULL,
    quantidade NUMERIC(14,3) NOT NULL,
    saldo_anterior NUMERIC(14,3) NOT NULL,
    saldo_posterior NUMERIC(14,3) NOT NULL,
    origem VARCHAR(60),
    origem_id UUID,
    documento VARCHAR(120),
    observacao TEXT,
    data_movimentacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    CONSTRAINT fk_movimentacao_estoque_estoque FOREIGN KEY (estoque_id) REFERENCES estoque (id),
    CONSTRAINT fk_movimentacao_estoque_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_movimentacao_estoque_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_movimentacao_estoque_filial FOREIGN KEY (filial_id) REFERENCES filial (id),
    CONSTRAINT fk_movimentacao_estoque_deposito FOREIGN KEY (deposito_id) REFERENCES deposito (id),
    CONSTRAINT fk_movimentacao_estoque_localizacao FOREIGN KEY (localizacao_id) REFERENCES localizacao_estoque (id),
    CONSTRAINT chk_movimentacao_estoque_quantidade_positiva CHECK (quantidade > 0),
    CONSTRAINT chk_movimentacao_estoque_tipo CHECK (tipo_movimentacao IN (
        'ENTRADA',
        'SAIDA',
        'AJUSTE_ENTRADA',
        'AJUSTE_SAIDA',
        'TRANSFERENCIA_ENTRADA',
        'TRANSFERENCIA_SAIDA',
        'RESERVA',
        'CANCELAMENTO_RESERVA',
        'BAIXA_RESERVA',
        'DEVOLUCAO',
        'AVARIA'
    ))
);

CREATE INDEX IF NOT EXISTS idx_movimentacao_estoque_estoque
    ON movimentacao_estoque (estoque_id, data_movimentacao DESC);

CREATE INDEX IF NOT EXISTS idx_movimentacao_estoque_produto
    ON movimentacao_estoque (produto_id, data_movimentacao DESC);

CREATE INDEX IF NOT EXISTS idx_movimentacao_estoque_empresa
    ON movimentacao_estoque (empresa_id, data_movimentacao DESC);

CREATE INDEX IF NOT EXISTS idx_movimentacao_estoque_deposito
    ON movimentacao_estoque (deposito_id, data_movimentacao DESC);

CREATE INDEX IF NOT EXISTS idx_movimentacao_estoque_localizacao
    ON movimentacao_estoque (localizacao_id, data_movimentacao DESC);

CREATE INDEX IF NOT EXISTS idx_movimentacao_estoque_periodo
    ON movimentacao_estoque (data_movimentacao DESC);

CREATE INDEX IF NOT EXISTS idx_movimentacao_estoque_origem
    ON movimentacao_estoque (origem, origem_id)
    WHERE origem IS NOT NULL AND origem_id IS NOT NULL;

COMMENT ON TABLE movimentacao_estoque IS 'Historico imutavel de movimentacoes que alteram estoque';
COMMENT ON COLUMN movimentacao_estoque.saldo_anterior IS 'Saldo fisico antes da movimentacao';
COMMENT ON COLUMN movimentacao_estoque.saldo_posterior IS 'Saldo fisico apos a movimentacao';
