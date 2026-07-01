CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    nome VARCHAR(120) NOT NULL,
    email CITEXT NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,

    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,

    ultimo_login TIMESTAMPTZ,

    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,

    CONSTRAINT uk_usuario_email UNIQUE (email),

    CONSTRAINT chk_usuario_deleted_ativo
        CHECK (deleted_at IS NULL OR ativo = FALSE),

    CONSTRAINT fk_usuario_criado_por
        FOREIGN KEY (criado_por)
        REFERENCES usuario(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_usuario_atualizado_por
        FOREIGN KEY (atualizado_por)
        REFERENCES usuario(id)
        ON DELETE SET NULL
);

CREATE TABLE perfil (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(80) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_perfil_nome UNIQUE (nome)
);

CREATE TABLE permissao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(80) NOT NULL,
    recurso VARCHAR(80) NOT NULL,
    acao VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_permissao_recurso_acao UNIQUE (recurso, acao),

    CONSTRAINT chk_permissao_acao
        CHECK (acao IN (
            'CRIAR',
            'VISUALIZAR',
            'EDITAR',
            'EXCLUIR',
            'EXPORTAR',
            'APROVAR',
            '*'
        ))
);

CREATE TABLE usuario_perfil (
    usuario_id UUID NOT NULL,
    perfil_id UUID NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (usuario_id, perfil_id),

    CONSTRAINT fk_usuario_perfil_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_usuario_perfil_perfil
        FOREIGN KEY (perfil_id)
        REFERENCES perfil(id)
        ON DELETE CASCADE
);

CREATE TABLE perfil_permissao (
    perfil_id UUID NOT NULL,
    permissao_id UUID NOT NULL,

    PRIMARY KEY (perfil_id, permissao_id),

    CONSTRAINT fk_perfil_permissao_perfil
        FOREIGN KEY (perfil_id)
        REFERENCES perfil(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_perfil_permissao_permissao
        FOREIGN KEY (permissao_id)
        REFERENCES permissao(id)
        ON DELETE CASCADE
);

CREATE TABLE auditoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    usuario_id UUID,
    ip INET,
    user_agent VARCHAR(255),

    tabela VARCHAR(80) NOT NULL,
    registro_id UUID NOT NULL,
    acao VARCHAR(20) NOT NULL,

    dados_antes JSONB,
    dados_depois JSONB,

    data TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_auditoria_acao
        CHECK (acao IN (
            'INSERT',
            'UPDATE',
            'DELETE',
            'LOGIN',
            'LOGOUT',
            'ACESSO_NEGADO'
        )),

    CONSTRAINT chk_auditoria_dados
        CHECK (
            NOT (acao = 'INSERT' AND dados_antes IS NOT NULL)
            AND
            NOT (acao = 'DELETE' AND dados_depois IS NOT NULL)
        ),

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_usuario_ativo_deleted
    ON usuario (ativo, deleted_at)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_usuario_deleted_at
    ON usuario (deleted_at)
    WHERE deleted_at IS NOT NULL;

CREATE INDEX idx_usuario_ultimo_login
    ON usuario (ultimo_login DESC NULLS LAST)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_perfil_ativo
    ON perfil (ativo)
    WHERE ativo = TRUE;

CREATE INDEX idx_permissao_ativo
    ON permissao (recurso, acao)
    WHERE ativo = TRUE;

CREATE INDEX idx_usuario_perfil_perfil_id
    ON usuario_perfil (perfil_id);

CREATE INDEX idx_perfil_permissao_permissao_id
    ON perfil_permissao (permissao_id);

CREATE INDEX idx_auditoria_tabela_data
    ON auditoria (tabela, data DESC);

CREATE INDEX idx_auditoria_registro
    ON auditoria (tabela, registro_id, data DESC);

CREATE INDEX idx_auditoria_usuario_data
    ON auditoria (usuario_id, data DESC)
    WHERE usuario_id IS NOT NULL;

CREATE INDEX idx_auditoria_dados_antes_gin
    ON auditoria USING GIN (dados_antes);

CREATE INDEX idx_auditoria_dados_depois_gin
    ON auditoria USING GIN (dados_depois);

CREATE OR REPLACE FUNCTION fn_set_atualizado_em()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_usuario_atualizado_em
    BEFORE UPDATE ON usuario
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_atualizado_em();

COMMENT ON TABLE usuario IS 'Credenciais de acesso ao sistema invPlus';
COMMENT ON COLUMN usuario.email IS 'Email case-insensitive usando CITEXT';
COMMENT ON COLUMN usuario.deleted_at IS 'Soft delete: NULL = ativo, NOT NULL = excluído/anonimizado';
COMMENT ON COLUMN usuario.ativo IS 'Indica se o usuário pode acessar o sistema';

COMMENT ON TABLE perfil IS 'Agrupamento de permissões de acesso';
COMMENT ON TABLE permissao IS 'Permissão granular por recurso e ação';
COMMENT ON TABLE usuario_perfil IS 'Relacionamento N:N entre usuário e perfil';
COMMENT ON TABLE perfil_permissao IS 'Relacionamento N:N entre perfil e permissão';
COMMENT ON TABLE auditoria IS 'Log de auditoria para rastreabilidade de operações críticas';
COMMENT ON FUNCTION fn_set_atualizado_em() IS 'Atualiza automaticamente o campo atualizado_em em operações de UPDATE';