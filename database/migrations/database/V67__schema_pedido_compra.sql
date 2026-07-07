CREATE TABLE pedido_compra (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    fornecedor_id UUID NOT NULL REFERENCES fornecedor(id),
    numero VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    data_emissao DATE NOT NULL,
    data_prevista_entrega DATE,
    observacao TEXT,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ,
    atualizado_por UUID,
    CONSTRAINT chk_pedido_compra_status CHECK (status IN ('ABERTO', 'APROVADO', 'PARCIALMENTE_RECEBIDO', 'RECEBIDO', 'CANCELADO')),
    CONSTRAINT chk_pedido_compra_valor_total CHECK (valor_total >= 0)
);

CREATE UNIQUE INDEX uk_pedido_compra_empresa_numero_ativo
    ON pedido_compra (empresa_id, lower(numero))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_pedido_compra_empresa_ativo ON pedido_compra (empresa_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_compra_filial_ativo ON pedido_compra (filial_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_compra_fornecedor_ativo ON pedido_compra (fornecedor_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_compra_status_ativo ON pedido_compra (status) WHERE ativo = TRUE AND deleted_at IS NULL;
