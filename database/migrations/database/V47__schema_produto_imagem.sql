-- Cria o vinculo entre Produto e Arquivo para imagens e anexos visuais do catalogo.
CREATE TABLE IF NOT EXISTS produto_imagem (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL,
    arquivo_id UUID NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    ordem INTEGER NOT NULL DEFAULT 0,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT fk_produto_imagem_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_produto_imagem_arquivo FOREIGN KEY (arquivo_id) REFERENCES arquivo (id),
    CONSTRAINT chk_produto_imagem_ordem CHECK (ordem >= 0),
    CONSTRAINT chk_produto_imagem_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_imagem_produto_arquivo_ativo
    ON produto_imagem (produto_id, arquivo_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_imagem_principal_ativo
    ON produto_imagem (produto_id)
    WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_imagem_produto
    ON produto_imagem (produto_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_imagem_arquivo
    ON produto_imagem (arquivo_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_imagem_ativo
    ON produto_imagem (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_produto_imagem_ordem
    ON produto_imagem (produto_id, ordem)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON TABLE produto_imagem IS 'Vincula imagens e arquivos visuais ao cadastro de Produto';
COMMENT ON COLUMN produto_imagem.principal IS 'Indica a imagem principal exibida para o produto';
COMMENT ON COLUMN produto_imagem.ordem IS 'Ordenacao visual das imagens do produto';
