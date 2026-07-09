ALTER TABLE conta_receber
    ADD COLUMN plano_conta_id UUID;

ALTER TABLE conta_receber
    ADD CONSTRAINT fk_conta_receber_plano_conta
    FOREIGN KEY (plano_conta_id) REFERENCES plano_conta(id);

CREATE INDEX idx_conta_receber_plano_conta
    ON conta_receber(plano_conta_id);

ALTER TABLE conta_pagar
    ADD COLUMN plano_conta_id UUID;

ALTER TABLE conta_pagar
    ADD CONSTRAINT fk_conta_pagar_plano_conta
    FOREIGN KEY (plano_conta_id) REFERENCES plano_conta(id);

CREATE INDEX idx_conta_pagar_plano_conta
    ON conta_pagar(plano_conta_id);

ALTER TABLE movimento_financeiro
    ADD COLUMN plano_conta_id UUID;

ALTER TABLE movimento_financeiro
    ADD CONSTRAINT fk_movimento_financeiro_plano_conta
    FOREIGN KEY (plano_conta_id) REFERENCES plano_conta(id);

CREATE INDEX idx_movimento_financeiro_plano_conta
    ON movimento_financeiro(plano_conta_id);
