CREATE TABLE conta_receber (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    cliente_id UUID NOT NULL REFERENCES cliente(id),
    venda_faturamento_id UUID REFERENCES venda_faturamento(id),
    numero VARCHAR(50) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    data_emissao DATE NOT NULL,
    valor_total NUMERIC(15,2) NOT NULL,
    valor_pago NUMERIC(15,2) NOT NULL DEFAULT 0,
    valor_aberto NUMERIC(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTA',
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_conta_receber_status CHECK (status IN ('ABERTA', 'PARCIALMENTE_PAGA', 'PAGA', 'CANCELADA', 'VENCIDA')),
    CONSTRAINT chk_conta_receber_valor_total CHECK (valor_total > 0),
    CONSTRAINT chk_conta_receber_valor_pago CHECK (valor_pago >= 0),
    CONSTRAINT chk_conta_receber_valor_aberto CHECK (valor_aberto >= 0)
);

CREATE UNIQUE INDEX uk_conta_receber_empresa_numero_ativo
    ON conta_receber (empresa_id, LOWER(numero))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_conta_receber_empresa
    ON conta_receber (empresa_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_conta_receber_cliente
    ON conta_receber (cliente_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_conta_receber_venda_faturamento
    ON conta_receber (venda_faturamento_id)
    WHERE venda_faturamento_id IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX idx_conta_receber_status
    ON conta_receber (status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_conta_receber_ativo
    ON conta_receber (ativo)
    WHERE deleted_at IS NULL;
