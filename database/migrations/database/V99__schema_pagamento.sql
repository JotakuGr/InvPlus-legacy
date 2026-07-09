CREATE TABLE pagamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parcela_pagar_id UUID NOT NULL REFERENCES parcela_pagar(id),
    conta_pagar_id UUID NOT NULL REFERENCES conta_pagar(id),
    forma_pagamento_id UUID NOT NULL REFERENCES forma_pagamento(id),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    fornecedor_id UUID NOT NULL REFERENCES fornecedor(id),
    valor_pago NUMERIC(15,2) NOT NULL,
    juros NUMERIC(15,2) NOT NULL DEFAULT 0,
    multa NUMERIC(15,2) NOT NULL DEFAULT 0,
    desconto NUMERIC(15,2) NOT NULL DEFAULT 0,
    valor_liquido NUMERIC(15,2) NOT NULL,
    data_pagamento DATE NOT NULL,
    observacao TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_pagamento_status CHECK (status IN ('ATIVO', 'CANCELADO')),
    CONSTRAINT chk_pagamento_valor_pago CHECK (valor_pago > 0),
    CONSTRAINT chk_pagamento_juros CHECK (juros >= 0),
    CONSTRAINT chk_pagamento_multa CHECK (multa >= 0),
    CONSTRAINT chk_pagamento_desconto CHECK (desconto >= 0),
    CONSTRAINT chk_pagamento_valor_liquido CHECK (valor_liquido >= 0)
);

CREATE INDEX idx_pagamento_parcela ON pagamento (parcela_pagar_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagamento_conta ON pagamento (conta_pagar_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagamento_forma ON pagamento (forma_pagamento_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagamento_empresa ON pagamento (empresa_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagamento_filial ON pagamento (filial_id) WHERE filial_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_pagamento_fornecedor ON pagamento (fornecedor_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagamento_data ON pagamento (data_pagamento) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagamento_status ON pagamento (status) WHERE deleted_at IS NULL;
