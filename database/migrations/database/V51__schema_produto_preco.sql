-- Cria historico de precos comerciais do Produto com vigencia e empresa opcional.
CREATE TABLE IF NOT EXISTS produto_preco (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL,
    empresa_id UUID,
    tipo_preco VARCHAR(30) NOT NULL,
    preco NUMERIC(15, 4) NOT NULL,
    vigencia_inicio DATE NOT NULL,
    vigencia_fim DATE,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT fk_produto_preco_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_produto_preco_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT chk_produto_preco_tipo CHECK (
        tipo_preco IN ('CUSTO', 'VENDA', 'ATACADO', 'VAREJO', 'PROMOCIONAL', 'ESPECIAL')
    ),
    CONSTRAINT chk_produto_preco_valor CHECK (preco >= 0),
    CONSTRAINT chk_produto_preco_vigencia CHECK (vigencia_fim IS NULL OR vigencia_fim >= vigencia_inicio),
    CONSTRAINT chk_produto_preco_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_preco_vigencia_ativo
    ON produto_preco (produto_id, tipo_preco, COALESCE(empresa_id, '00000000-0000-0000-0000-000000000000'::uuid), vigencia_inicio)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_preco_principal_ativo
    ON produto_preco (produto_id, tipo_preco, COALESCE(empresa_id, '00000000-0000-0000-0000-000000000000'::uuid))
    WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_preco_produto
    ON produto_preco (produto_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_preco_empresa
    ON produto_preco (empresa_id)
    WHERE empresa_id IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_preco_tipo
    ON produto_preco (tipo_preco)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_preco_vigencia
    ON produto_preco (produto_id, tipo_preco, vigencia_inicio, vigencia_fim)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_preco_ativo
    ON produto_preco (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON TABLE produto_preco IS 'Historico de precos comerciais do Produto por tipo, vigencia e empresa opcional';
COMMENT ON COLUMN produto_preco.empresa_id IS 'Empresa opcional para precificacao multiempresa; nulo representa preco global';
COMMENT ON COLUMN produto_preco.principal IS 'Indica o preco preferencial para produto, tipo e empresa';
