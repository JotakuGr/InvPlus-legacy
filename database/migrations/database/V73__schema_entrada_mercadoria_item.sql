CREATE TABLE entrada_mercadoria_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entrada_mercadoria_id UUID NOT NULL REFERENCES entrada_mercadoria(id),
    pedido_compra_item_id UUID REFERENCES pedido_compra_item(id),
    produto_id UUID NOT NULL REFERENCES produto(id),
    produto_unidade_id UUID REFERENCES produto_unidade(id),
    deposito_id UUID NOT NULL REFERENCES deposito(id),
    localizacao_id UUID NOT NULL REFERENCES localizacao_estoque(id),
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
    CONSTRAINT chk_entrada_mercadoria_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_entrada_mercadoria_item_valor_unitario CHECK (valor_unitario >= 0),
    CONSTRAINT chk_entrada_mercadoria_item_valor_total CHECK (valor_total >= 0)
);

CREATE INDEX idx_entrada_mercadoria_item_entrada_ativo ON entrada_mercadoria_item (entrada_mercadoria_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_entrada_mercadoria_item_pedido_item_ativo ON entrada_mercadoria_item (pedido_compra_item_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_entrada_mercadoria_item_produto_ativo ON entrada_mercadoria_item (produto_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_entrada_mercadoria_item_deposito_ativo ON entrada_mercadoria_item (deposito_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_entrada_mercadoria_item_localizacao_ativo ON entrada_mercadoria_item (localizacao_id) WHERE ativo = TRUE AND deleted_at IS NULL;
