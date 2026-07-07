-- Cria saldos fisicos atuais por produto, empresa, deposito e localizacao.
CREATE TABLE IF NOT EXISTS estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL,
    empresa_id UUID NOT NULL,
    filial_id UUID,
    deposito_id UUID NOT NULL,
    localizacao_id UUID NOT NULL,
    quantidade NUMERIC(14,3) NOT NULL DEFAULT 0,
    quantidade_reservada NUMERIC(14,3) NOT NULL DEFAULT 0,
    quantidade_disponivel NUMERIC(14,3) NOT NULL DEFAULT 0,
    quantidade_avariada NUMERIC(14,3) NOT NULL DEFAULT 0,
    quantidade_transito NUMERIC(14,3) NOT NULL DEFAULT 0,
    estoque_minimo NUMERIC(14,3) NOT NULL DEFAULT 0,
    estoque_maximo NUMERIC(14,3),
    ultimo_movimento_em TIMESTAMP WITH TIME ZONE,
    ultimo_movimento_id UUID,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT fk_estoque_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_estoque_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_estoque_filial FOREIGN KEY (filial_id) REFERENCES filial (id),
    CONSTRAINT fk_estoque_deposito FOREIGN KEY (deposito_id) REFERENCES deposito (id),
    CONSTRAINT fk_estoque_localizacao FOREIGN KEY (localizacao_id) REFERENCES localizacao_estoque (id),
    CONSTRAINT chk_estoque_quantidade_nao_negativa CHECK (quantidade >= 0),
    CONSTRAINT chk_estoque_quantidade_reservada_nao_negativa CHECK (quantidade_reservada >= 0),
    CONSTRAINT chk_estoque_quantidade_disponivel_nao_negativa CHECK (quantidade_disponivel >= 0),
    CONSTRAINT chk_estoque_quantidade_avariada_nao_negativa CHECK (quantidade_avariada >= 0),
    CONSTRAINT chk_estoque_quantidade_transito_nao_negativa CHECK (quantidade_transito >= 0),
    CONSTRAINT chk_estoque_minimo_nao_negativo CHECK (estoque_minimo >= 0),
    CONSTRAINT chk_estoque_maximo_maior_minimo CHECK (estoque_maximo IS NULL OR estoque_maximo >= estoque_minimo),
    CONSTRAINT chk_estoque_disponivel_calculado CHECK (quantidade_disponivel = quantidade - quantidade_reservada),
    CONSTRAINT chk_estoque_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_estoque_produto_empresa_deposito_localizacao_ativo
    ON estoque (produto_id, empresa_id, deposito_id, localizacao_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_estoque_produto
    ON estoque (produto_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_estoque_empresa
    ON estoque (empresa_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_estoque_filial
    ON estoque (filial_id)
    WHERE filial_id IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_estoque_deposito
    ON estoque (deposito_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_estoque_localizacao
    ON estoque (localizacao_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_estoque_ativo
    ON estoque (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_estoque_baixo
    ON estoque (produto_id, empresa_id, deposito_id)
    WHERE quantidade_disponivel <= estoque_minimo
      AND ativo = TRUE
      AND deleted_at IS NULL;

COMMENT ON TABLE estoque IS 'Saldo fisico atual por produto, empresa, deposito e localizacao';
COMMENT ON COLUMN estoque.quantidade_disponivel IS 'Quantidade calculada pela aplicacao: quantidade - quantidade_reservada';
COMMENT ON COLUMN estoque.ultimo_movimento_id IS 'Referencia futura para MovimentacaoEstoque';
