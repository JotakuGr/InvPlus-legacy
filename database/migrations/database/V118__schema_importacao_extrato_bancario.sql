CREATE TABLE importacao_extrato_bancario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_bancaria_id UUID REFERENCES conta_bancaria(id),
    extrato_bancario_id UUID REFERENCES extrato_bancario(id),
    arquivo_nome VARCHAR(255) NOT NULL,
    arquivo_hash VARCHAR(64) NOT NULL,
    formato VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    quantidade_lancamentos_lidos INTEGER NOT NULL DEFAULT 0,
    quantidade_lancamentos_importados INTEGER NOT NULL DEFAULT 0,
    quantidade_lancamentos_ignorados INTEGER NOT NULL DEFAULT 0,
    mensagem_erro VARCHAR(1000),
    iniciada_em TIMESTAMP WITH TIME ZONE NOT NULL,
    finalizada_em TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT chk_importacao_extrato_formato CHECK (formato IN ('OFX', 'CSV')),
    CONSTRAINT chk_importacao_extrato_status CHECK (
        status IN ('RECEBIDA', 'PROCESSANDO', 'CONCLUIDA', 'CONCLUIDA_COM_ALERTAS', 'FALHOU')
    ),
    CONSTRAINT chk_importacao_extrato_contadores CHECK (
        quantidade_lancamentos_lidos >= 0
        AND quantidade_lancamentos_importados >= 0
        AND quantidade_lancamentos_ignorados >= 0
    )
);

CREATE UNIQUE INDEX uk_importacao_extrato_hash
    ON importacao_extrato_bancario (arquivo_hash)
    WHERE status <> 'FALHOU';
CREATE INDEX idx_importacao_extrato_conta ON importacao_extrato_bancario (conta_bancaria_id);
CREATE INDEX idx_importacao_extrato_extrato ON importacao_extrato_bancario (extrato_bancario_id);
CREATE INDEX idx_importacao_extrato_status ON importacao_extrato_bancario (status);
CREATE INDEX idx_importacao_extrato_iniciada ON importacao_extrato_bancario (iniciada_em);
