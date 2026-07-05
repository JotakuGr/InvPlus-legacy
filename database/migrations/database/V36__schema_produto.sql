-- Produto e a entidade central do modulo Produtos e referencia cadastros base.
CREATE TABLE IF NOT EXISTS produto (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(60) NOT NULL,
    sku VARCHAR(80),
    codigo_interno VARCHAR(80),
    codigo_barras VARCHAR(80),
    nome VARCHAR(180) NOT NULL,
    descricao TEXT,
    categoria_id UUID NOT NULL,
    marca_id UUID,
    unidade_medida_id UUID NOT NULL,
    preco_custo NUMERIC(15,4) NOT NULL DEFAULT 0,
    preco_venda NUMERIC(15,4) NOT NULL DEFAULT 0,
    peso_liquido NUMERIC(14,3) NOT NULL DEFAULT 0,
    peso_bruto NUMERIC(14,3) NOT NULL DEFAULT 0,
    altura_cm NUMERIC(14,3) NOT NULL DEFAULT 0,
    largura_cm NUMERIC(14,3) NOT NULL DEFAULT 0,
    comprimento_cm NUMERIC(14,3) NOT NULL DEFAULT 0,
    estoque_minimo NUMERIC(14,3) NOT NULL DEFAULT 0,
    controla_lote BOOLEAN NOT NULL DEFAULT FALSE,
    controla_validade BOOLEAN NOT NULL DEFAULT FALSE,
    permite_fracionamento BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_produto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT fk_produto_marca FOREIGN KEY (marca_id) REFERENCES marca (id),
    CONSTRAINT fk_produto_unidade_medida FOREIGN KEY (unidade_medida_id) REFERENCES unidade_medida (id),
    CONSTRAINT chk_produto_preco_custo CHECK (preco_custo >= 0),
    CONSTRAINT chk_produto_preco_venda CHECK (preco_venda >= 0),
    CONSTRAINT chk_produto_peso_liquido CHECK (peso_liquido >= 0),
    CONSTRAINT chk_produto_peso_bruto CHECK (peso_bruto >= 0),
    CONSTRAINT chk_produto_altura CHECK (altura_cm >= 0),
    CONSTRAINT chk_produto_largura CHECK (largura_cm >= 0),
    CONSTRAINT chk_produto_comprimento CHECK (comprimento_cm >= 0),
    CONSTRAINT chk_produto_estoque_minimo CHECK (estoque_minimo >= 0),
    CONSTRAINT chk_produto_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_codigo_ativo
    ON produto (LOWER(codigo))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_sku_ativo
    ON produto (LOWER(sku))
    WHERE sku IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_codigo_barras_ativo
    ON produto (LOWER(codigo_barras))
    WHERE codigo_barras IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_categoria
    ON produto (categoria_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_marca
    ON produto (marca_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_unidade
    ON produto (unidade_medida_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_ativos
    ON produto (ativo, deleted_at);
