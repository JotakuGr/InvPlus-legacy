CREATE TABLE extrato_bancario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_bancaria_id UUID NOT NULL REFERENCES conta_bancaria(id),
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    saldo_inicial NUMERIC(15, 2) NOT NULL,
    saldo_final NUMERIC(15, 2) NOT NULL,
    origem VARCHAR(30) NOT NULL,
    arquivo_nome VARCHAR(255),
    arquivo_hash VARCHAR(64),
    importado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'IMPORTADO',
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_extrato_bancario_periodo CHECK (data_inicio <= data_fim),
    CONSTRAINT chk_extrato_bancario_origem CHECK (
        origem IN ('MANUAL', 'OFX', 'CSV', 'CNAB', 'API_BANCARIA', 'OPEN_FINANCE')
    ),
    CONSTRAINT chk_extrato_bancario_status CHECK (
        status IN ('IMPORTADO', 'PROCESSANDO', 'CONCILIADO', 'CANCELADO')
    ),
    CONSTRAINT chk_extrato_bancario_arquivo_hash CHECK (
        arquivo_hash IS NULL OR arquivo_hash ~ '^[A-Fa-f0-9]{64}$'
    )
);

CREATE UNIQUE INDEX uk_extrato_bancario_arquivo_hash_ativo
    ON extrato_bancario (arquivo_hash)
    WHERE arquivo_hash IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_extrato_bancario_conta
    ON extrato_bancario (conta_bancaria_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_extrato_bancario_origem
    ON extrato_bancario (origem) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_extrato_bancario_status
    ON extrato_bancario (status) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_extrato_bancario_data_inicio
    ON extrato_bancario (data_inicio) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_extrato_bancario_data_fim
    ON extrato_bancario (data_fim) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_extrato_bancario_ativo
    ON extrato_bancario (ativo) WHERE deleted_at IS NULL;
