CREATE TABLE empresa (
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
    CONSTRAINT uk_empresa_cnpj UNIQUE (cnpj),
    CONSTRAINT chk_empresa_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE endereco (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID,
    tipo VARCHAR(40) NOT NULL DEFAULT 'PRINCIPAL',
    logradouro VARCHAR(160) NOT NULL,
    numero VARCHAR(20),
    complemento VARCHAR(120),
    bairro VARCHAR(100),
    cidade VARCHAR(100) NOT NULL,
    uf CHAR(2) NOT NULL,
    cep VARCHAR(12),
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_endereco_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id) ON DELETE CASCADE,
    CONSTRAINT chk_endereco_uf CHECK (uf = UPPER(uf)),
    CONSTRAINT chk_endereco_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE categoria (
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
    CONSTRAINT uk_categoria_nome_tipo UNIQUE (nome, tipo),
    CONSTRAINT chk_categoria_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE marca (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT uk_marca_nome UNIQUE (nome),
    CONSTRAINT chk_marca_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE unidade_medida (
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
    CONSTRAINT uk_unidade_medida_sigla UNIQUE (sigla),
    CONSTRAINT chk_unidade_medida_casas_decimais CHECK (casas_decimais BETWEEN 0 AND 6),
    CONSTRAINT chk_unidade_medida_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE TABLE arquivo (
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
    CONSTRAINT chk_arquivo_tamanho CHECK (tamanho_bytes >= 0),
    CONSTRAINT chk_arquivo_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE INDEX idx_empresa_ativo ON empresa (ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_endereco_empresa ON endereco (empresa_id) WHERE empresa_id IS NOT NULL;
CREATE INDEX idx_categoria_ativo_tipo ON categoria (tipo, ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_marca_ativo ON marca (ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_unidade_medida_ativo ON unidade_medida (ativo) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_arquivo_entidade ON arquivo (entidade, entidade_id) WHERE entidade IS NOT NULL AND entidade_id IS NOT NULL;

CREATE TRIGGER trg_empresa_atualizado_em BEFORE UPDATE ON empresa FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();
CREATE TRIGGER trg_endereco_atualizado_em BEFORE UPDATE ON endereco FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();
CREATE TRIGGER trg_categoria_atualizado_em BEFORE UPDATE ON categoria FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();
CREATE TRIGGER trg_marca_atualizado_em BEFORE UPDATE ON marca FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();
CREATE TRIGGER trg_unidade_medida_atualizado_em BEFORE UPDATE ON unidade_medida FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();
CREATE TRIGGER trg_arquivo_atualizado_em BEFORE UPDATE ON arquivo FOR EACH ROW EXECUTE FUNCTION fn_set_atualizado_em();

INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000301', 'Acesso total de cadastros', 'CADASTROS', '*', TRUE),
    ('00000000-0000-0000-0000-000000000302', 'Criar cadastros', 'CADASTROS', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000303', 'Visualizar cadastros', 'CADASTROS', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000304', 'Editar cadastros', 'CADASTROS', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000305', 'Excluir cadastros', 'CADASTROS', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CADASTROS'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;

COMMENT ON TABLE empresa IS 'Empresas ou filiais que operam dentro do invPlus';
COMMENT ON TABLE endereco IS 'Enderecos reutilizaveis vinculados a empresas e futuras entidades';
COMMENT ON TABLE categoria IS 'Categorias base para classificacao de cadastros e produtos';
COMMENT ON TABLE marca IS 'Marcas comerciais usadas no catalogo de produtos';
COMMENT ON TABLE unidade_medida IS 'Unidades de medida usadas em estoque, compras e vendas';
COMMENT ON TABLE arquivo IS 'Metadados de arquivos anexados a entidades do sistema';
