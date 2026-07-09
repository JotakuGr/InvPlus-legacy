ALTER TABLE permissao
    DROP CONSTRAINT IF EXISTS chk_permissao_acao;

ALTER TABLE permissao
    ADD CONSTRAINT chk_permissao_acao
        CHECK (acao IN (
            'CRIAR',
            'VISUALIZAR',
            'EDITAR',
            'EXCLUIR',
            'EXPORTAR',
            'APROVAR',
            'CANCELAR',
            'BAIXAR',
            'CONFERIR',
            'EFETIVAR',
            'FATURAR',
            '*'
        ));

INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000820', 'Administrar faturamentos de venda', 'VENDA_FATURAMENTO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000821', 'Criar faturamentos de venda', 'VENDA_FATURAMENTO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000822', 'Visualizar faturamentos de venda', 'VENDA_FATURAMENTO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000823', 'Faturar vendas', 'VENDA_FATURAMENTO', 'FATURAR', TRUE),
    ('00000000-0000-0000-0000-000000000824', 'Cancelar faturamentos de venda', 'VENDA_FATURAMENTO', 'CANCELAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'VENDA_FATURAMENTO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
