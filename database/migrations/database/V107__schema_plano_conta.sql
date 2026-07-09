CREATE TABLE plano_conta (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    plano_conta_pai_id UUID REFERENCES plano_conta(id),
    codigo VARCHAR(40) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    descricao TEXT,
    tipo VARCHAR(20) NOT NULL,
    natureza VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_plano_conta_tipo CHECK (tipo IN ('SINTETICA', 'ANALITICA')),
    CONSTRAINT chk_plano_conta_natureza CHECK (
        natureza IN ('RECEITA', 'DESPESA', 'ATIVO', 'PASSIVO', 'PATRIMONIO_LIQUIDO')
    ),
    CONSTRAINT chk_plano_conta_auto_pai CHECK (plano_conta_pai_id IS NULL OR plano_conta_pai_id <> id)
);

CREATE UNIQUE INDEX uk_plano_conta_empresa_codigo_ativo
    ON plano_conta (empresa_id, UPPER(codigo))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_plano_conta_empresa
    ON plano_conta (empresa_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_plano_conta_pai
    ON plano_conta (plano_conta_pai_id)
    WHERE plano_conta_pai_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_plano_conta_tipo
    ON plano_conta (tipo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_plano_conta_natureza
    ON plano_conta (natureza) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_plano_conta_ativo
    ON plano_conta (ativo) WHERE deleted_at IS NULL;
