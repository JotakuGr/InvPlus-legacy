-- Cria a entidade principal do modulo Pessoas.
CREATE TABLE IF NOT EXISTS pessoa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_pessoa VARCHAR(20) NOT NULL,
    nome VARCHAR(160) NOT NULL,
    nome_fantasia VARCHAR(160),
    cpf_cnpj VARCHAR(14) NOT NULL,
    rg_ie VARCHAR(30),
    telefone VARCHAR(30),
    celular VARCHAR(30),
    email CITEXT,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT chk_pessoa_tipo_pessoa CHECK (tipo_pessoa IN ('FISICA', 'JURIDICA')),
    CONSTRAINT chk_pessoa_cpf_cnpj_formato CHECK (cpf_cnpj ~ '^[0-9]{11}$|^[0-9]{14}$'),
    CONSTRAINT chk_pessoa_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_pessoa_cpf_cnpj_ativo
    ON pessoa (cpf_cnpj)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_pessoa_ativo
    ON pessoa (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_pessoa_cpf_cnpj
    ON pessoa (cpf_cnpj);

COMMENT ON TABLE pessoa IS 'Cadastro base de pessoas fisicas e juridicas usadas pelos modulos comerciais e fiscais';
COMMENT ON COLUMN pessoa.tipo_pessoa IS 'Tipo da pessoa: FISICA ou JURIDICA';
COMMENT ON COLUMN pessoa.cpf_cnpj IS 'Documento normalizado com apenas numeros, CPF com 11 digitos ou CNPJ com 14 digitos';
