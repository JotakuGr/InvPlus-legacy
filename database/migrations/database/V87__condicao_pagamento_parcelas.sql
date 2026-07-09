ALTER TABLE condicao_pagamento
    ADD COLUMN IF NOT EXISTS regra_vencimento VARCHAR(30) NOT NULL DEFAULT 'PROXIMO_DIA_UTIL',
    ADD COLUMN IF NOT EXISTS considera_feriados BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE condicao_pagamento
    ALTER COLUMN quantidade_parcelas SET DEFAULT 1,
    ALTER COLUMN intervalo_dias SET DEFAULT 0,
    ALTER COLUMN entrada_percentual SET DEFAULT 0;

ALTER TABLE condicao_pagamento
    DROP CONSTRAINT IF EXISTS chk_condicao_pagamento_regra_vencimento;

ALTER TABLE condicao_pagamento
    ADD CONSTRAINT chk_condicao_pagamento_regra_vencimento
        CHECK (regra_vencimento IN ('MANTER_DATA', 'PROXIMO_DIA_UTIL', 'DIA_UTIL_ANTERIOR'));

CREATE TABLE IF NOT EXISTS condicao_pagamento_parcela (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    condicao_pagamento_id UUID NOT NULL REFERENCES condicao_pagamento(id),
    numero_parcela INTEGER NOT NULL,
    dias INTEGER NOT NULL,
    percentual NUMERIC(7,4) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    criado_por UUID,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por UUID,
    CONSTRAINT chk_condicao_pagamento_parcela_numero CHECK (numero_parcela >= 1),
    CONSTRAINT chk_condicao_pagamento_parcela_dias CHECK (dias >= 0),
    CONSTRAINT chk_condicao_pagamento_parcela_percentual CHECK (percentual > 0 AND percentual <= 100)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_condicao_pagamento_parcela_numero_ativo
    ON condicao_pagamento_parcela (condicao_pagamento_id, numero_parcela)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_condicao_pagamento_parcela_condicao
    ON condicao_pagamento_parcela (condicao_pagamento_id);

CREATE INDEX IF NOT EXISTS idx_condicao_pagamento_parcela_ativo
    ON condicao_pagamento_parcela (ativo)
    WHERE deleted_at IS NULL;
