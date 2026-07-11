ALTER TABLE movimento_financeiro
    ADD COLUMN conta_bancaria_id UUID;

ALTER TABLE movimento_financeiro
    ADD CONSTRAINT fk_movimento_financeiro_conta_bancaria
    FOREIGN KEY (conta_bancaria_id) REFERENCES conta_bancaria(id);

CREATE INDEX idx_movimento_financeiro_conta_bancaria
    ON movimento_financeiro(conta_bancaria_id);

CREATE INDEX idx_movimento_conciliacao_busca
    ON movimento_financeiro(conta_bancaria_id, tipo_movimento, data_movimento, valor);
