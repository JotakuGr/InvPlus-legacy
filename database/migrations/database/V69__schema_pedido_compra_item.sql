CREATE TABLE pedido_compra_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_compra_id UUID NOT NULL REFERENCES pedido_compra(id),
    produto_id UUID NOT NULL REFERENCES produto(id),
    produto_unidade_id UUID REFERENCES produto_unidade(id),
    quantidade NUMERIC(15, 3) NOT NULL,
    valor_unitario NUMERIC(15, 2) NOT NULL DEFAULT 0,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ,
    atualizado_por UUID,
    CONSTRAINT chk_pedido_compra_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_pedido_compra_item_valor_unitario CHECK (valor_unitario >= 0),
    CONSTRAINT chk_pedido_compra_item_valor_total CHECK (valor_total >= 0)
);

CREATE INDEX idx_pedido_compra_item_pedido_ativo ON pedido_compra_item (pedido_compra_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_compra_item_produto_ativo ON pedido_compra_item (produto_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_compra_item_unidade_ativo ON pedido_compra_item (produto_unidade_id) WHERE ativo = TRUE AND deleted_at IS NULL;
