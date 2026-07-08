CREATE TABLE pedido_venda_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_venda_id UUID NOT NULL REFERENCES pedido_venda(id),
    produto_id UUID NOT NULL REFERENCES produto(id),
    produto_preco_id UUID NOT NULL REFERENCES produto_preco(id),
    produto_unidade_id UUID NOT NULL REFERENCES produto_unidade(id),
    quantidade NUMERIC(15, 3) NOT NULL,
    valor_unitario NUMERIC(15, 4) NOT NULL DEFAULT 0,
    desconto NUMERIC(15, 2) NOT NULL DEFAULT 0,
    acrescimo NUMERIC(15, 2) NOT NULL DEFAULT 0,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ,
    atualizado_por UUID,
    CONSTRAINT chk_pedido_venda_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_pedido_venda_item_valor_unitario CHECK (valor_unitario >= 0),
    CONSTRAINT chk_pedido_venda_item_desconto CHECK (desconto >= 0),
    CONSTRAINT chk_pedido_venda_item_acrescimo CHECK (acrescimo >= 0),
    CONSTRAINT chk_pedido_venda_item_valor_total CHECK (valor_total >= 0)
);

CREATE INDEX idx_pedido_venda_item_pedido ON pedido_venda_item (pedido_venda_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_venda_item_produto ON pedido_venda_item (produto_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_venda_item_preco ON pedido_venda_item (produto_preco_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_venda_item_unidade ON pedido_venda_item (produto_unidade_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_venda_item_ativo ON pedido_venda_item (ativo) WHERE deleted_at IS NULL;
