CREATE TABLE extrato_bancario_lancamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    extrato_bancario_id UUID NOT NULL REFERENCES extrato_bancario(id),
    data_lancamento DATE NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    documento VARCHAR(80),
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    saldo_apos_lancamento NUMERIC(15, 2),
    identificador_bancario VARCHAR(120),
    categoria_bancaria VARCHAR(30) NOT NULL DEFAULT 'OUTRO',
    conciliado BOOLEAN NOT NULL DEFAULT FALSE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_extrato_lancamento_tipo CHECK (tipo IN ('CREDITO', 'DEBITO')),
    CONSTRAINT chk_extrato_lancamento_categoria CHECK (
        categoria_bancaria IN ('PIX', 'BOLETO', 'TED', 'DOC', 'TRANSFERENCIA', 'CARTAO',
                               'TARIFA', 'JUROS', 'IMPOSTO', 'OUTRO')
    ),
    CONSTRAINT chk_extrato_lancamento_valor CHECK (valor > 0)
);

CREATE UNIQUE INDEX uk_extrato_lancamento_identificador_ativo
    ON extrato_bancario_lancamento (extrato_bancario_id, identificador_bancario)
    WHERE identificador_bancario IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_extrato_lancamento_extrato
    ON extrato_bancario_lancamento (extrato_bancario_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_extrato_lancamento_data
    ON extrato_bancario_lancamento (data_lancamento) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_extrato_lancamento_tipo
    ON extrato_bancario_lancamento (tipo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_extrato_lancamento_categoria
    ON extrato_bancario_lancamento (categoria_bancaria) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_extrato_lancamento_conciliado
    ON extrato_bancario_lancamento (conciliado) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_extrato_lancamento_ativo
    ON extrato_bancario_lancamento (ativo) WHERE deleted_at IS NULL;
