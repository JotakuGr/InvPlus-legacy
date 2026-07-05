-- Cria multiplos codigos de barras ou codigos alternativos por Produto.
CREATE TABLE IF NOT EXISTS produto_codigo_barras (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL,
    tipo_codigo VARCHAR(40) NOT NULL,
    codigo VARCHAR(120) NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT fk_produto_codigo_barras_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT chk_produto_codigo_barras_tipo CHECK (
        tipo_codigo IN (
            'EAN_UNIDADE',
            'EAN_CAIXA',
            'EAN_PACOTE',
            'GTIN_LOGISTICO',
            'CODIGO_INTERNO_ALTERNATIVO'
        )
    ),
    CONSTRAINT chk_produto_codigo_barras_codigo CHECK (length(trim(codigo)) > 0),
    CONSTRAINT chk_produto_codigo_barras_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_codigo_barras_codigo_ativo
    ON produto_codigo_barras (LOWER(codigo))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_codigo_barras_produto_codigo_ativo
    ON produto_codigo_barras (produto_id, LOWER(codigo))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_codigo_barras_principal_ativo
    ON produto_codigo_barras (produto_id)
    WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_codigo_barras_produto
    ON produto_codigo_barras (produto_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_codigo_barras_tipo
    ON produto_codigo_barras (tipo_codigo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_codigo_barras_ativo
    ON produto_codigo_barras (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON TABLE produto_codigo_barras IS 'Codigos de barras e codigos alternativos vinculados ao Produto';
COMMENT ON COLUMN produto_codigo_barras.tipo_codigo IS 'Tipo do codigo: EAN unidade, EAN caixa, EAN pacote, GTIN logistico ou codigo interno alternativo';
COMMENT ON COLUMN produto_codigo_barras.principal IS 'Indica o codigo principal usado em buscas rapidas e operacoes padrao';
