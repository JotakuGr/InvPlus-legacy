CREATE TABLE usuario_empresa (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    empresa_id UUID NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    acesso_todas_filiais BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizado_por UUID,
    CONSTRAINT fk_usuario_empresa_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_usuario_empresa_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT uk_usuario_empresa UNIQUE (usuario_id, empresa_id)
);

CREATE TABLE usuario_empresa_filial (
    id UUID PRIMARY KEY,
    usuario_empresa_id UUID NOT NULL,
    filial_id UUID NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizado_por UUID,
    CONSTRAINT fk_usuario_empresa_filial_vinculo FOREIGN KEY (usuario_empresa_id) REFERENCES usuario_empresa(id),
    CONSTRAINT fk_usuario_empresa_filial_filial FOREIGN KEY (filial_id) REFERENCES filial(id),
    CONSTRAINT uk_usuario_empresa_filial UNIQUE (usuario_empresa_id, filial_id)
);

CREATE INDEX idx_usuario_empresa_usuario ON usuario_empresa(usuario_id);
CREATE INDEX idx_usuario_empresa_empresa ON usuario_empresa(empresa_id);
CREATE INDEX idx_usuario_empresa_ativo ON usuario_empresa(usuario_id, ativo);
CREATE INDEX idx_usuario_empresa_filial_vinculo ON usuario_empresa_filial(usuario_empresa_id);
CREATE INDEX idx_usuario_empresa_filial_filial ON usuario_empresa_filial(filial_id);
CREATE UNIQUE INDEX uk_usuario_empresa_principal ON usuario_empresa(usuario_id)
    WHERE principal = TRUE AND ativo = TRUE;
CREATE UNIQUE INDEX uk_usuario_empresa_filial_principal ON usuario_empresa_filial(usuario_empresa_id)
    WHERE principal = TRUE AND ativo = TRUE;

ALTER TABLE refresh_token ADD COLUMN empresa_id UUID;
ALTER TABLE refresh_token ADD COLUMN filial_id UUID;
ALTER TABLE refresh_token ADD CONSTRAINT fk_refresh_token_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id);
ALTER TABLE refresh_token ADD CONSTRAINT fk_refresh_token_filial FOREIGN KEY (filial_id) REFERENCES filial(id);
