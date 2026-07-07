-- Cria depositos onde o estoque fisico ou em transito fica armazenado.
CREATE TABLE IF NOT EXISTS deposito (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL,
    filial_id UUID,
    codigo CITEXT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    descricao TEXT,
    tipo_deposito VARCHAR(30) NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    permite_venda BOOLEAN NOT NULL DEFAULT TRUE,
    permite_compra BOOLEAN NOT NULL DEFAULT TRUE,
    permite_transferencia BOOLEAN NOT NULL DEFAULT TRUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT fk_deposito_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_deposito_filial FOREIGN KEY (filial_id) REFERENCES filial (id),
    CONSTRAINT chk_deposito_tipo CHECK (tipo_deposito IN ('LOJA', 'CENTRAL', 'ALMOXARIFADO', 'PRODUCAO', 'CAMARA_FRIA', 'TERCEIROS', 'TRANSITO')),
    CONSTRAINT chk_deposito_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_deposito_empresa_codigo_ativo
    ON deposito (empresa_id, LOWER(codigo))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_deposito_empresa_principal_ativo
    ON deposito (empresa_id)
    WHERE principal = TRUE AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_deposito_empresa
    ON deposito (empresa_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_deposito_filial
    ON deposito (filial_id)
    WHERE filial_id IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_deposito_tipo
    ON deposito (tipo_deposito)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_deposito_ativo
    ON deposito (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON TABLE deposito IS 'Locais fisicos, logicos ou em transito usados para armazenar estoque';
COMMENT ON COLUMN deposito.principal IS 'Indica o deposito preferencial da empresa';
COMMENT ON COLUMN deposito.tipo_deposito IS 'Classificacao operacional do deposito';
