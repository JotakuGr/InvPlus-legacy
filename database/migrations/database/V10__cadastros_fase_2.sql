-- Consolida a segunda fase de Cadastros sem recriar tabelas ja entregues pela V7.
-- As tabelas abaixo usam IF NOT EXISTS para permitir ambientes novos e bancos ja migrados.
CREATE TABLE IF NOT EXISTS unidade_medida (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sigla VARCHAR(10) NOT NULL,
    nome VARCHAR(80) NOT NULL,
    casas_decimais SMALLINT NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_unidade_medida_sigla UNIQUE (sigla),
    CONSTRAINT chk_unidade_medida_casas_decimais CHECK (casas_decimais BETWEEN 0 AND 6),
    CONSTRAINT chk_unidade_medida_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE IF NOT EXISTS categoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    tipo VARCHAR(40) NOT NULL DEFAULT 'GERAL',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_categoria_nome_tipo UNIQUE (nome, tipo),
    CONSTRAINT chk_categoria_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE IF NOT EXISTS marca (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_marca_nome UNIQUE (nome),
    CONSTRAINT chk_marca_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE IF NOT EXISTS empresa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    razao_social VARCHAR(160) NOT NULL,
    nome_fantasia VARCHAR(160),
    cnpj CITEXT NOT NULL,
    inscricao_estadual VARCHAR(30),
    telefone VARCHAR(30),
    email CITEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_empresa_cnpj UNIQUE (cnpj),
    CONSTRAINT chk_empresa_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE IF NOT EXISTS arquivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_original VARCHAR(255) NOT NULL,
    nome_armazenado VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    caminho VARCHAR(500) NOT NULL,
    checksum_sha256 VARCHAR(64),
    entidade VARCHAR(80),
    entidade_id UUID,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_arquivo_tamanho CHECK (tamanho_bytes >= 0),
    CONSTRAINT chk_arquivo_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE INDEX IF NOT EXISTS idx_unidade_medida_ativo ON unidade_medida (ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_categoria_ativo_tipo ON categoria (tipo, ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_marca_ativo ON marca (ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_empresa_ativo ON empresa (ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_arquivo_entidade ON arquivo (entidade, entidade_id) WHERE entidade IS NOT NULL AND entidade_id IS NOT NULL;

COMMENT ON TABLE unidade_medida IS 'Unidades de medida usadas em estoque, compras e vendas';
COMMENT ON COLUMN unidade_medida.sigla IS 'Sigla unica da unidade, como UN, KG, CX, LT ou M';
COMMENT ON COLUMN unidade_medida.nome IS 'Descricao obrigatoria da unidade de medida';
