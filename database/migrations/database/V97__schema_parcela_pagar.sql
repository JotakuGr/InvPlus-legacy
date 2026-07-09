CREATE TABLE parcela_pagar (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_pagar_id UUID NOT NULL REFERENCES conta_pagar(id),
    numero_parcela INTEGER NOT NULL,
    data_vencimento DATE NOT NULL,
    valor NUMERIC(15,2) NOT NULL,
    valor_pago NUMERIC(15,2) NOT NULL DEFAULT 0,
    valor_aberto NUMERIC(15,2) NOT NULL,
    juros NUMERIC(15,2) NOT NULL DEFAULT 0,
    multa NUMERIC(15,2) NOT NULL DEFAULT 0,
    desconto NUMERIC(15,2) NOT NULL DEFAULT 0,
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
    CONSTRAINT chk_parcela_pagar_status CHECK (
        status IN ('ABERTA', 'PARCIALMENTE_PAGA', 'PAGA', 'CANCELADA', 'VENCIDA')
    ),
    CONSTRAINT chk_parcela_pagar_numero CHECK (numero_parcela > 0),
    CONSTRAINT chk_parcela_pagar_valor CHECK (valor > 0),
    CONSTRAINT chk_parcela_pagar_valor_pago CHECK (valor_pago >= 0),
    CONSTRAINT chk_parcela_pagar_valor_aberto CHECK (valor_aberto >= 0),
    CONSTRAINT chk_parcela_pagar_juros CHECK (juros >= 0),
    CONSTRAINT chk_parcela_pagar_multa CHECK (multa >= 0),
    CONSTRAINT chk_parcela_pagar_desconto CHECK (desconto >= 0)
);

CREATE UNIQUE INDEX uk_parcela_pagar_conta_numero_ativo
    ON parcela_pagar (conta_pagar_id, numero_parcela)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_parcela_pagar_conta
    ON parcela_pagar (conta_pagar_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_parcela_pagar_status
    ON parcela_pagar (status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_parcela_pagar_vencimento
    ON parcela_pagar (data_vencimento)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_parcela_pagar_ativo
    ON parcela_pagar (ativo)
    WHERE deleted_at IS NULL;
