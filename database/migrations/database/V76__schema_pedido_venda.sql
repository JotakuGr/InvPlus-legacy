CREATE TABLE pedido_venda (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    cliente_id UUID NOT NULL REFERENCES cliente(id),
    vendedor_id UUID REFERENCES funcionario(id),
    numero VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    data_emissao DATE NOT NULL,
    data_entrega_prevista DATE,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    desconto NUMERIC(15, 2) NOT NULL DEFAULT 0,
    acrescimo NUMERIC(15, 2) NOT NULL DEFAULT 0,
    observacao TEXT,
    nivel_aprovacao INTEGER NOT NULL DEFAULT 0,
    aprovado_por UUID,
    aprovado_em TIMESTAMPTZ,
    cancelado_em TIMESTAMPTZ,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ,
    atualizado_por UUID,
    CONSTRAINT chk_pedido_venda_status
        CHECK (status IN ('ABERTO', 'APROVADO', 'SEPARANDO', 'FATURADO', 'CANCELADO')),
    CONSTRAINT chk_pedido_venda_valor_total CHECK (valor_total >= 0),
    CONSTRAINT chk_pedido_venda_desconto CHECK (desconto >= 0),
    CONSTRAINT chk_pedido_venda_acrescimo CHECK (acrescimo >= 0)
);

CREATE UNIQUE INDEX uk_pedido_venda_empresa_numero_ativo
    ON pedido_venda (empresa_id, lower(numero))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_pedido_venda_empresa_ativo ON pedido_venda (empresa_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_venda_filial_ativo ON pedido_venda (filial_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_venda_cliente_ativo ON pedido_venda (cliente_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_venda_vendedor_ativo ON pedido_venda (vendedor_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_venda_status_ativo ON pedido_venda (status) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_pedido_venda_data_emissao ON pedido_venda (data_emissao);

CREATE TABLE pedido_venda_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_venda_id UUID NOT NULL REFERENCES pedido_venda(id),
    status_anterior VARCHAR(30),
    status_novo VARCHAR(30) NOT NULL,
    observacao TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ,
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_pedido_venda_hist_status_anterior
        CHECK (status_anterior IS NULL OR status_anterior IN ('ABERTO', 'APROVADO', 'SEPARANDO', 'FATURADO', 'CANCELADO')),
    CONSTRAINT chk_pedido_venda_hist_status_novo
        CHECK (status_novo IN ('ABERTO', 'APROVADO', 'SEPARANDO', 'FATURADO', 'CANCELADO'))
);

CREATE INDEX idx_pedido_venda_historico_pedido
    ON pedido_venda_historico (pedido_venda_id, criado_em DESC);
