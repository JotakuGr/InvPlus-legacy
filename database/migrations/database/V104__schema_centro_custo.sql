CREATE TABLE centro_custo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    codigo VARCHAR(40) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    descricao TEXT,
    centro_custo_pai_id UUID REFERENCES centro_custo(id),
    tipo VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_centro_custo_tipo CHECK (tipo IN ('RECEITA', 'DESPESA', 'AMBOS')),
    CONSTRAINT chk_centro_custo_auto_pai CHECK (centro_custo_pai_id IS NULL OR centro_custo_pai_id <> id)
);

CREATE UNIQUE INDEX uk_centro_custo_empresa_codigo_ativo
    ON centro_custo (empresa_id, UPPER(codigo))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_centro_custo_empresa
    ON centro_custo (empresa_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_centro_custo_pai
    ON centro_custo (centro_custo_pai_id) WHERE centro_custo_pai_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_centro_custo_tipo
    ON centro_custo (tipo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_centro_custo_ativo
    ON centro_custo (ativo) WHERE deleted_at IS NULL;
