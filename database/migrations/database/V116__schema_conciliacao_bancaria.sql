CREATE TABLE conciliacao_bancaria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    extrato_bancario_lancamento_id UUID NOT NULL REFERENCES extrato_bancario_lancamento(id),
    movimento_financeiro_id UUID NOT NULL REFERENCES movimento_financeiro(id),
    conta_bancaria_id UUID NOT NULL REFERENCES conta_bancaria(id),
    tipo_conciliacao VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    valor_extrato NUMERIC(15, 2) NOT NULL,
    valor_movimento NUMERIC(15, 2) NOT NULL,
    diferenca NUMERIC(15, 2) NOT NULL,
    conciliado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    conciliado_por UUID,
    cancelado_em TIMESTAMP WITH TIME ZONE,
    cancelado_por UUID,
    motivo_cancelamento VARCHAR(500),
    observacao TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT chk_conciliacao_bancaria_tipo CHECK (tipo_conciliacao IN ('MANUAL', 'AUTOMATICA')),
    CONSTRAINT chk_conciliacao_bancaria_status CHECK (status IN ('CONCILIADA', 'CANCELADA')),
    CONSTRAINT chk_conciliacao_bancaria_valores CHECK (
        valor_extrato > 0 AND valor_movimento > 0 AND diferenca >= 0
    )
);

CREATE INDEX idx_conciliacao_bancaria_lancamento
    ON conciliacao_bancaria (extrato_bancario_lancamento_id);
CREATE INDEX idx_conciliacao_bancaria_movimento
    ON conciliacao_bancaria (movimento_financeiro_id);
CREATE INDEX idx_conciliacao_bancaria_conta
    ON conciliacao_bancaria (conta_bancaria_id);
CREATE INDEX idx_conciliacao_bancaria_status
    ON conciliacao_bancaria (status);
CREATE INDEX idx_conciliacao_bancaria_data
    ON conciliacao_bancaria (conciliado_em);

CREATE UNIQUE INDEX uk_conciliacao_lancamento_ativa
    ON conciliacao_bancaria (extrato_bancario_lancamento_id)
    WHERE status = 'CONCILIADA';

CREATE UNIQUE INDEX uk_conciliacao_movimento_ativa
    ON conciliacao_bancaria (movimento_financeiro_id)
    WHERE status = 'CONCILIADA';
