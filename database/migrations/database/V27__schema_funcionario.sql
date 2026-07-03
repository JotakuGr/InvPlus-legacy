-- Cria o papel Funcionario vinculado a uma Pessoa e, opcionalmente, a um Usuario.
CREATE TABLE IF NOT EXISTS funcionario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL,
    usuario_id UUID,
    cargo VARCHAR(120) NOT NULL,
    salario NUMERIC(15,2) NOT NULL DEFAULT 0,
    data_admissao DATE NOT NULL,
    data_demissao DATE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_funcionario_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT fk_funcionario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT chk_funcionario_salario CHECK (salario >= 0),
    CONSTRAINT chk_funcionario_datas CHECK (data_demissao IS NULL OR data_demissao >= data_admissao),
    CONSTRAINT chk_funcionario_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_funcionario_pessoa_ativo
    ON funcionario (pessoa_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_funcionario_usuario_ativo
    ON funcionario (usuario_id)
    WHERE usuario_id IS NOT NULL AND ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_funcionario_pessoa
    ON funcionario (pessoa_id);

CREATE INDEX IF NOT EXISTS idx_funcionario_usuario
    ON funcionario (usuario_id);

CREATE INDEX IF NOT EXISTS idx_funcionario_ativo
    ON funcionario (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;
