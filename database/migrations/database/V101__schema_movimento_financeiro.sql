CREATE TABLE movimento_financeiro (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    tipo_movimento VARCHAR(20) NOT NULL,
    origem VARCHAR(30) NOT NULL,
    origem_id UUID,
    forma_pagamento_id UUID NOT NULL REFERENCES forma_pagamento(id),
    cliente_id UUID REFERENCES cliente(id),
    fornecedor_id UUID REFERENCES fornecedor(id),
    valor NUMERIC(15,2) NOT NULL,
    data_movimento DATE NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    observacao TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_movimento_financeiro_tipo
        CHECK (tipo_movimento IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT chk_movimento_financeiro_origem
        CHECK (origem IN ('RECEBIMENTO', 'PAGAMENTO', 'AJUSTE_MANUAL', 'TRANSFERENCIA', 'ESTORNO')),
    CONSTRAINT chk_movimento_financeiro_status
        CHECK (status IN ('ATIVO', 'CANCELADO')),
    CONSTRAINT chk_movimento_financeiro_valor CHECK (valor > 0),
    CONSTRAINT chk_movimento_financeiro_contraparte CHECK (
        (tipo_movimento = 'ENTRADA' AND fornecedor_id IS NULL)
        OR (tipo_movimento = 'SAIDA' AND cliente_id IS NULL)
    )
);

CREATE INDEX idx_movimento_financeiro_empresa
    ON movimento_financeiro (empresa_id, data_movimento) WHERE deleted_at IS NULL;
CREATE INDEX idx_movimento_financeiro_filial
    ON movimento_financeiro (filial_id) WHERE filial_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_movimento_financeiro_origem
    ON movimento_financeiro (origem, origem_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_movimento_financeiro_forma
    ON movimento_financeiro (forma_pagamento_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_movimento_financeiro_cliente
    ON movimento_financeiro (cliente_id) WHERE cliente_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_movimento_financeiro_fornecedor
    ON movimento_financeiro (fornecedor_id) WHERE fornecedor_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_movimento_financeiro_status
    ON movimento_financeiro (status) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_movimento_financeiro_origem_ativa
    ON movimento_financeiro (origem, origem_id)
    WHERE origem_id IS NOT NULL
      AND origem IN ('RECEBIMENTO', 'PAGAMENTO')
      AND status = 'ATIVO'
      AND ativo = TRUE
      AND deleted_at IS NULL;
