CREATE TABLE conta_bancaria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    banco_codigo VARCHAR(20) NOT NULL,
    banco_nome VARCHAR(120) NOT NULL,
    agencia VARCHAR(20) NOT NULL,
    agencia_digito VARCHAR(5),
    numero_conta VARCHAR(30) NOT NULL,
    conta_digito VARCHAR(5),
    tipo_conta VARCHAR(20) NOT NULL,
    titular VARCHAR(160) NOT NULL,
    documento_titular VARCHAR(14),
    pix_chave VARCHAR(160),
    saldo_inicial NUMERIC(15, 2) NOT NULL DEFAULT 0,
    saldo_atual NUMERIC(15, 2) NOT NULL DEFAULT 0,
    data_saldo_inicial DATE,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_conta_bancaria_tipo CHECK (
        tipo_conta IN ('CORRENTE', 'POUPANCA', 'PAGAMENTO', 'CAIXA_INTERNO')
    ),
    CONSTRAINT chk_conta_bancaria_saldo_inicial CHECK (saldo_inicial >= 0),
    CONSTRAINT chk_conta_bancaria_saldo_atual CHECK (saldo_atual >= 0),
    CONSTRAINT chk_conta_bancaria_documento_titular CHECK (
        documento_titular IS NULL OR length(documento_titular) IN (11, 14)
    )
);

CREATE UNIQUE INDEX uk_conta_bancaria_empresa_banco_agencia_numero_ativo
    ON conta_bancaria (empresa_id, banco_codigo, agencia, numero_conta)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX uk_conta_bancaria_empresa_principal_ativo
    ON conta_bancaria (empresa_id)
    WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_conta_bancaria_empresa
    ON conta_bancaria (empresa_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_conta_bancaria_filial
    ON conta_bancaria (filial_id) WHERE filial_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_conta_bancaria_banco_codigo
    ON conta_bancaria (banco_codigo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_conta_bancaria_tipo
    ON conta_bancaria (tipo_conta) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_conta_bancaria_ativo
    ON conta_bancaria (ativo) WHERE deleted_at IS NULL;
