CREATE TABLE recebimento (
    id UUID PRIMARY KEY,
    parcela_receber_id UUID NOT NULL,
    conta_receber_id UUID NOT NULL,
    forma_pagamento_id UUID NOT NULL,
    empresa_id UUID NOT NULL,
    filial_id UUID,
    cliente_id UUID NOT NULL,
    valor_recebido NUMERIC(15, 2) NOT NULL,
    juros NUMERIC(15, 2) NOT NULL DEFAULT 0,
    multa NUMERIC(15, 2) NOT NULL DEFAULT 0,
    desconto NUMERIC(15, 2) NOT NULL DEFAULT 0,
    valor_liquido NUMERIC(15, 2) NOT NULL,
    data_recebimento DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ATIVO',
    observacao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT fk_recebimento_parcela FOREIGN KEY (parcela_receber_id) REFERENCES parcela_receber(id),
    CONSTRAINT fk_recebimento_conta FOREIGN KEY (conta_receber_id) REFERENCES conta_receber(id),
    CONSTRAINT fk_recebimento_forma_pagamento FOREIGN KEY (forma_pagamento_id) REFERENCES forma_pagamento(id),
    CONSTRAINT fk_recebimento_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_recebimento_filial FOREIGN KEY (filial_id) REFERENCES filial(id),
    CONSTRAINT fk_recebimento_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT ck_recebimento_valor_recebido CHECK (valor_recebido > 0),
    CONSTRAINT ck_recebimento_juros CHECK (juros >= 0),
    CONSTRAINT ck_recebimento_multa CHECK (multa >= 0),
    CONSTRAINT ck_recebimento_desconto CHECK (desconto >= 0),
    CONSTRAINT ck_recebimento_valor_liquido CHECK (valor_liquido >= 0),
    CONSTRAINT ck_recebimento_status CHECK (status IN ('ATIVO', 'CANCELADO'))
);

CREATE INDEX idx_recebimento_parcela ON recebimento (parcela_receber_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_recebimento_conta ON recebimento (conta_receber_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_recebimento_forma_pagamento ON recebimento (forma_pagamento_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_recebimento_empresa ON recebimento (empresa_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_recebimento_filial ON recebimento (filial_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_recebimento_cliente ON recebimento (cliente_id) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_recebimento_data ON recebimento (data_recebimento) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_recebimento_status ON recebimento (status) WHERE ativo = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_recebimento_ativo ON recebimento (ativo, deleted_at);
