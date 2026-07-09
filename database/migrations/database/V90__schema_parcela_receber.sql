CREATE TABLE parcela_receber (
    id UUID PRIMARY KEY,
    conta_receber_id UUID NOT NULL,
    numero_parcela INTEGER NOT NULL,
    data_vencimento DATE NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    valor_pago NUMERIC(15, 2) NOT NULL DEFAULT 0,
    valor_aberto NUMERIC(15, 2) NOT NULL,
    juros NUMERIC(15, 2) NOT NULL DEFAULT 0,
    multa NUMERIC(15, 2) NOT NULL DEFAULT 0,
    desconto NUMERIC(15, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTA',
    data_pagamento DATE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT fk_parcela_receber_conta FOREIGN KEY (conta_receber_id) REFERENCES conta_receber(id),
    CONSTRAINT ck_parcela_receber_numero CHECK (numero_parcela > 0),
    CONSTRAINT ck_parcela_receber_valor CHECK (valor > 0),
    CONSTRAINT ck_parcela_receber_valor_pago CHECK (valor_pago >= 0),
    CONSTRAINT ck_parcela_receber_valor_aberto CHECK (valor_aberto >= 0),
    CONSTRAINT ck_parcela_receber_juros CHECK (juros >= 0),
    CONSTRAINT ck_parcela_receber_multa CHECK (multa >= 0),
    CONSTRAINT ck_parcela_receber_desconto CHECK (desconto >= 0),
    CONSTRAINT ck_parcela_receber_status CHECK (status IN ('ABERTA', 'PARCIALMENTE_PAGA', 'PAGA', 'CANCELADA', 'VENCIDA'))
);

CREATE UNIQUE INDEX uk_parcela_receber_conta_numero_ativo
    ON parcela_receber (conta_receber_id, numero_parcela)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_parcela_receber_conta ON parcela_receber (conta_receber_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_parcela_receber_status ON parcela_receber (status) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_parcela_receber_vencimento ON parcela_receber (data_vencimento) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_parcela_receber_pagamento ON parcela_receber (data_pagamento) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_parcela_receber_ativo ON parcela_receber (ativo, deleted_at);
