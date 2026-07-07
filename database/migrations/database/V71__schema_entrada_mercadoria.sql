CREATE TABLE entrada_mercadoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    fornecedor_id UUID NOT NULL REFERENCES fornecedor(id),
    pedido_compra_id UUID REFERENCES pedido_compra(id),
    numero VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTA',
    data_entrada DATE NOT NULL,
    observacao TEXT,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ,
    atualizado_por UUID,
    CONSTRAINT chk_entrada_mercadoria_status CHECK (status IN ('ABERTA', 'CONFERIDA', 'EFETIVADA', 'CANCELADA')),
    CONSTRAINT chk_entrada_mercadoria_valor_total CHECK (valor_total >= 0)
);

CREATE UNIQUE INDEX uk_entrada_mercadoria_empresa_numero_ativo
    ON entrada_mercadoria (empresa_id, lower(numero))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_entrada_mercadoria_empresa_ativo ON entrada_mercadoria (empresa_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_entrada_mercadoria_fornecedor_ativo ON entrada_mercadoria (fornecedor_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_entrada_mercadoria_pedido_ativo ON entrada_mercadoria (pedido_compra_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_entrada_mercadoria_status_ativo ON entrada_mercadoria (status) WHERE ativo = TRUE AND deleted_at IS NULL;
