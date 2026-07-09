ALTER TABLE venda_faturamento
    ADD COLUMN conta_receber_id UUID;

ALTER TABLE venda_faturamento
    ADD CONSTRAINT fk_venda_faturamento_conta_receber
    FOREIGN KEY (conta_receber_id) REFERENCES conta_receber(id);

CREATE INDEX idx_venda_faturamento_conta_receber
    ON venda_faturamento(conta_receber_id);

CREATE UNIQUE INDEX uk_venda_faturamento_conta_receber
    ON venda_faturamento(conta_receber_id)
    WHERE conta_receber_id IS NOT NULL;
