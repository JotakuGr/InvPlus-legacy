ALTER TABLE conta_receber
    ADD COLUMN centro_custo_id UUID,
    ADD CONSTRAINT fk_conta_receber_centro_custo
        FOREIGN KEY (centro_custo_id) REFERENCES centro_custo(id);

ALTER TABLE conta_pagar
    ADD COLUMN centro_custo_id UUID,
    ADD CONSTRAINT fk_conta_pagar_centro_custo
        FOREIGN KEY (centro_custo_id) REFERENCES centro_custo(id);

ALTER TABLE movimento_financeiro
    ADD COLUMN centro_custo_id UUID,
    ADD CONSTRAINT fk_movimento_financeiro_centro_custo
        FOREIGN KEY (centro_custo_id) REFERENCES centro_custo(id);

CREATE INDEX idx_conta_receber_centro_custo ON conta_receber (centro_custo_id);
CREATE INDEX idx_conta_pagar_centro_custo ON conta_pagar (centro_custo_id);
CREATE INDEX idx_movimento_financeiro_centro_custo ON movimento_financeiro (centro_custo_id);
