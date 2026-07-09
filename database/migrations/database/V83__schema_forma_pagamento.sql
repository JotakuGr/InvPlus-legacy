CREATE TABLE forma_pagamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    permite_troco BOOLEAN NOT NULL DEFAULT FALSE,
    permite_parcelamento BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_forma_pagamento_tipo CHECK (tipo IN (
        'DINHEIRO',
        'PIX',
        'CARTAO_CREDITO',
        'CARTAO_DEBITO',
        'BOLETO',
        'TRANSFERENCIA',
        'CHEQUE',
        'CREDIARIO',
        'OUTRO'
    )),
    CONSTRAINT chk_forma_pagamento_troco CHECK (permite_troco = FALSE OR tipo = 'DINHEIRO'),
    CONSTRAINT chk_forma_pagamento_parcelamento CHECK (
        permite_parcelamento = FALSE
        OR tipo IN ('CARTAO_CREDITO', 'BOLETO', 'CREDIARIO')
    )
);

CREATE UNIQUE INDEX uk_forma_pagamento_codigo_ativo
    ON forma_pagamento (LOWER(codigo))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_forma_pagamento_tipo_ativo
    ON forma_pagamento (tipo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_forma_pagamento_ativo
    ON forma_pagamento (ativo)
    WHERE deleted_at IS NULL;
