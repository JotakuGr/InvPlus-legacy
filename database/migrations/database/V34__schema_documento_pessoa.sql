-- Registra documentos de uma Pessoa, com anexo digital opcional via Arquivo.
CREATE TABLE IF NOT EXISTS documento_pessoa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL,
    arquivo_id UUID,
    tipo_documento VARCHAR(40) NOT NULL,
    numero VARCHAR(120) NOT NULL,
    orgao_emissor VARCHAR(120),
    data_emissao DATE,
    data_validade DATE,
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_por UUID,
    CONSTRAINT fk_documento_pessoa_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT fk_documento_pessoa_arquivo FOREIGN KEY (arquivo_id) REFERENCES arquivo (id),
    CONSTRAINT chk_documento_pessoa_tipo CHECK (tipo_documento IN (
        'RG',
        'CPF',
        'CNH',
        'CONTRATO_SOCIAL',
        'ALVARA',
        'LICENCA_SANITARIA',
        'CERTIFICADO_DIGITAL',
        'COMPROVANTE_ENDERECO',
        'OUTRO'
    )),
    CONSTRAINT chk_documento_pessoa_validade CHECK (
        data_emissao IS NULL OR data_validade IS NULL OR data_validade >= data_emissao
    ),
    CONSTRAINT chk_documento_pessoa_deleted_ativo CHECK (deleted_at IS NULL OR ativo = FALSE)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_documento_pessoa_ativo
    ON documento_pessoa (pessoa_id, tipo_documento, LOWER(numero))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_documento_pessoa_pessoa
    ON documento_pessoa (pessoa_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_documento_pessoa_arquivo
    ON documento_pessoa (arquivo_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_documento_pessoa_ativos
    ON documento_pessoa (ativo, deleted_at);
