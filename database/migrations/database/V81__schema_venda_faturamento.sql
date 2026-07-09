CREATE TABLE venda_faturamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_venda_id UUID NOT NULL REFERENCES pedido_venda(id),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    cliente_id UUID NOT NULL REFERENCES cliente(id),
    numero VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    data_faturamento TIMESTAMPTZ,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ,
    atualizado_por UUID,
    CONSTRAINT chk_venda_faturamento_status
        CHECK (status IN ('ABERTO', 'FATURADO', 'CANCELADO')),
    CONSTRAINT chk_venda_faturamento_valor_total CHECK (valor_total >= 0)
);

CREATE UNIQUE INDEX uk_venda_faturamento_pedido_ativo
    ON venda_faturamento (pedido_venda_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX uk_venda_faturamento_empresa_numero_ativo
    ON venda_faturamento (empresa_id, lower(numero))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_venda_faturamento_empresa_ativo ON venda_faturamento (empresa_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_venda_faturamento_filial_ativo ON venda_faturamento (filial_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_venda_faturamento_cliente_ativo ON venda_faturamento (cliente_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_venda_faturamento_pedido_ativo ON venda_faturamento (pedido_venda_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_venda_faturamento_status_ativo ON venda_faturamento (status) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_venda_faturamento_data ON venda_faturamento (data_faturamento);
