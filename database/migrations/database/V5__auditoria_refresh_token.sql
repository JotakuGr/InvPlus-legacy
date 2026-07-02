ALTER TABLE refresh_token
    ADD COLUMN criado_por UUID,
    ADD COLUMN atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN atualizado_por UUID;

ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_criado_por
        FOREIGN KEY (criado_por)
        REFERENCES usuario(id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_refresh_token_atualizado_por
        FOREIGN KEY (atualizado_por)
        REFERENCES usuario(id)
        ON DELETE SET NULL;

CREATE TRIGGER trg_refresh_token_atualizado_em
    BEFORE UPDATE ON refresh_token
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_atualizado_em();
