-- Registra reservas de saldo sem baixar fisicamente o estoque.
CREATE TABLE IF NOT EXISTS reserva_estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estoque_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    empresa_id UUID NOT NULL,
    filial_id UUID,
    deposito_id UUID NOT NULL,
    localizacao_id UUID NOT NULL,
    quantidade NUMERIC(14,3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    origem VARCHAR(60) NOT NULL,
    origem_id UUID,
    expira_em TIMESTAMP WITH TIME ZONE,
    baixada_em TIMESTAMP WITH TIME ZONE,
    cancelada_em TIMESTAMP WITH TIME ZONE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_por UUID,
    CONSTRAINT fk_reserva_estoque_estoque FOREIGN KEY (estoque_id) REFERENCES estoque (id),
    CONSTRAINT fk_reserva_estoque_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_reserva_estoque_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_reserva_estoque_filial FOREIGN KEY (filial_id) REFERENCES filial (id),
    CONSTRAINT fk_reserva_estoque_deposito FOREIGN KEY (deposito_id) REFERENCES deposito (id),
    CONSTRAINT fk_reserva_estoque_localizacao FOREIGN KEY (localizacao_id) REFERENCES localizacao_estoque (id),
    CONSTRAINT chk_reserva_estoque_quantidade_positiva CHECK (quantidade > 0),
    CONSTRAINT chk_reserva_estoque_status CHECK (status IN ('ATIVA', 'CANCELADA', 'BAIXADA', 'EXPIRADA')),
    CONSTRAINT chk_reserva_estoque_origem CHECK (origem IN (
        'COMPRA',
        'VENDA',
        'AJUSTE',
        'TRANSFERENCIA',
        'INVENTARIO',
        'RESERVA',
        'DEVOLUCAO',
        'PRODUCAO',
        'IMPORTACAO',
        'API'
    ))
);

CREATE INDEX IF NOT EXISTS idx_reserva_estoque_estoque
    ON reserva_estoque (estoque_id, status)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reserva_estoque_produto
    ON reserva_estoque (produto_id, status)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reserva_estoque_empresa
    ON reserva_estoque (empresa_id, status)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reserva_estoque_filial
    ON reserva_estoque (filial_id, status)
    WHERE filial_id IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reserva_estoque_deposito
    ON reserva_estoque (deposito_id, status)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reserva_estoque_localizacao
    ON reserva_estoque (localizacao_id, status)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reserva_estoque_origem
    ON reserva_estoque (origem, origem_id)
    WHERE origem_id IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_reserva_estoque_origem_ativa
    ON reserva_estoque (origem, origem_id)
    WHERE origem_id IS NOT NULL
      AND status = 'ATIVA'
      AND ativo = TRUE
      AND deleted_at IS NULL;

COMMENT ON TABLE reserva_estoque IS 'Reserva quantidade de estoque para processos de venda, separacao ou expedicao';
COMMENT ON COLUMN reserva_estoque.quantidade IS 'Quantidade reservada sem baixa fisica imediata';
COMMENT ON COLUMN reserva_estoque.origem IS 'Origem forte do processo que criou a reserva';
