ALTER TABLE pedido_compra
    ADD COLUMN IF NOT EXISTS nivel_aprovacao INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS aprovado_por UUID,
    ADD COLUMN IF NOT EXISTS aprovado_em TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_pedido_compra_aprovado_por
    ON pedido_compra (aprovado_por)
    WHERE aprovado_por IS NOT NULL;

CREATE TABLE IF NOT EXISTS pedido_compra_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_compra_id UUID NOT NULL REFERENCES pedido_compra(id),
    status_anterior VARCHAR(30),
    novo_status VARCHAR(30) NOT NULL,
    usuario_id UUID,
    data_alteracao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_pedido_compra_hist_status_anterior
        CHECK (status_anterior IS NULL OR status_anterior IN ('ABERTO', 'APROVADO', 'PARCIALMENTE_RECEBIDO', 'RECEBIDO', 'CANCELADO')),
    CONSTRAINT chk_pedido_compra_hist_novo_status
        CHECK (novo_status IN ('ABERTO', 'APROVADO', 'PARCIALMENTE_RECEBIDO', 'RECEBIDO', 'CANCELADO'))
);

CREATE INDEX IF NOT EXISTS idx_pedido_compra_historico_pedido
    ON pedido_compra_historico (pedido_compra_id, data_alteracao DESC);

CREATE TABLE IF NOT EXISTS entrada_mercadoria_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entrada_mercadoria_id UUID NOT NULL REFERENCES entrada_mercadoria(id),
    status_anterior VARCHAR(30),
    novo_status VARCHAR(30) NOT NULL,
    usuario_id UUID,
    data_alteracao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_entrada_mercadoria_hist_status_anterior
        CHECK (status_anterior IS NULL OR status_anterior IN ('ABERTA', 'CONFERIDA', 'EFETIVADA', 'CANCELADA')),
    CONSTRAINT chk_entrada_mercadoria_hist_novo_status
        CHECK (novo_status IN ('ABERTA', 'CONFERIDA', 'EFETIVADA', 'CANCELADA'))
);

CREATE INDEX IF NOT EXISTS idx_entrada_mercadoria_historico_entrada
    ON entrada_mercadoria_historico (entrada_mercadoria_id, data_alteracao DESC);
