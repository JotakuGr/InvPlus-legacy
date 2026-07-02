ALTER TABLE usuario ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE refresh_token ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE endereco ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE categoria ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE marca ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE unidade_medida ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE arquivo ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE pais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    codigo_iso2 VARCHAR(2) NOT NULL,
    codigo_iso3 VARCHAR(3),
    codigo_bacen VARCHAR(10),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_pais_codigo_iso2 UNIQUE (codigo_iso2),
    CONSTRAINT uk_pais_nome UNIQUE (nome),
    CONSTRAINT chk_pais_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE estado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pais_id UUID NOT NULL,
    nome VARCHAR(100) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    codigo_ibge VARCHAR(10),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_estado_pais FOREIGN KEY (pais_id) REFERENCES pais(id) ON DELETE RESTRICT,
    CONSTRAINT uk_estado_pais_uf UNIQUE (pais_id, uf),
    CONSTRAINT chk_estado_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE cidade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estado_id UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    codigo_ibge VARCHAR(10),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_cidade_estado FOREIGN KEY (estado_id) REFERENCES estado(id) ON DELETE RESTRICT,
    CONSTRAINT uk_cidade_estado_nome UNIQUE (estado_id, nome),
    CONSTRAINT chk_cidade_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE filial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL,
    endereco_id UUID,
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    cnpj CITEXT,
    inscricao_estadual VARCHAR(30),
    telefone VARCHAR(30),
    email CITEXT,
    matriz BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_filial_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id) ON DELETE RESTRICT,
    CONSTRAINT fk_filial_endereco FOREIGN KEY (endereco_id) REFERENCES endereco(id) ON DELETE SET NULL,
    CONSTRAINT uk_filial_empresa_codigo UNIQUE (empresa_id, codigo),
    CONSTRAINT uk_filial_cnpj UNIQUE (cnpj),
    CONSTRAINT chk_filial_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

ALTER TABLE endereco ADD COLUMN IF NOT EXISTS cidade_id UUID;
ALTER TABLE endereco
    ADD CONSTRAINT fk_endereco_cidade
    FOREIGN KEY (cidade_id)
    REFERENCES cidade(id)
    ON DELETE SET NULL;

CREATE INDEX idx_pais_ativo ON pais (ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_estado_pais_ativo ON estado (pais_id, ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_cidade_estado_ativo ON cidade (estado_id, ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_endereco_cidade ON endereco (cidade_id) WHERE cidade_id IS NOT NULL;
CREATE INDEX idx_filial_empresa_ativo ON filial (empresa_id, ativo) WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE TRIGGER trg_pais_atualizado_em BEFORE UPDATE ON pais FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();
CREATE TRIGGER trg_estado_atualizado_em BEFORE UPDATE ON estado FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();
CREATE TRIGGER trg_cidade_atualizado_em BEFORE UPDATE ON cidade FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();
CREATE TRIGGER trg_filial_atualizado_em BEFORE UPDATE ON filial FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();

COMMENT ON COLUMN usuario.version IS 'Controle de concorrencia otimista usado pelo Hibernate @Version';
COMMENT ON COLUMN empresa.version IS 'Controle de concorrencia otimista usado pelo Hibernate @Version';
COMMENT ON TABLE pais IS 'Cadastro de paises usados por enderecos e documentos fiscais';
COMMENT ON TABLE estado IS 'Cadastro de estados ou unidades federativas';
COMMENT ON TABLE cidade IS 'Cadastro de cidades vinculadas a estados';
COMMENT ON TABLE filial IS 'Unidades operacionais vinculadas a uma empresa';
