CREATE TABLE condicao_pagamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    quantidade_parcelas INTEGER NOT NULL,
    intervalo_dias INTEGER NOT NULL DEFAULT 0,
    entrada_percentual NUMERIC(7,4) NOT NULL DEFAULT 0,
    juros_percentual NUMERIC(7,4) NOT NULL DEFAULT 0,
    multa_percentual NUMERIC(7,4) NOT NULL DEFAULT 0,
    desconto_percentual NUMERIC(7,4) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_condicao_pagamento_quantidade CHECK (quantidade_parcelas >= 1),
    CONSTRAINT chk_condicao_pagamento_intervalo CHECK (intervalo_dias >= 0),
    CONSTRAINT chk_condicao_pagamento_entrada CHECK (entrada_percentual >= 0),
    CONSTRAINT chk_condicao_pagamento_juros CHECK (juros_percentual >= 0),
    CONSTRAINT chk_condicao_pagamento_multa CHECK (multa_percentual >= 0),
    CONSTRAINT chk_condicao_pagamento_desconto CHECK (desconto_percentual >= 0)
);

CREATE UNIQUE INDEX uk_condicao_pagamento_codigo_ativo
    ON condicao_pagamento (LOWER(codigo))
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_condicao_pagamento_ativo
    ON condicao_pagamento (ativo)
    WHERE deleted_at IS NULL;
