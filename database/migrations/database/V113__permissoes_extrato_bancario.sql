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
            'IMPORTAR',
            '*'
        ));

INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001130', 'Administrar extratos bancarios', 'EXTRATO_BANCARIO', '*', TRUE),
    ('00000000-0000-0000-0000-000000001131', 'Importar extratos bancarios', 'EXTRATO_BANCARIO', 'IMPORTAR', TRUE),
    ('00000000-0000-0000-0000-000000001132', 'Visualizar extratos bancarios', 'EXTRATO_BANCARIO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000001133', 'Cancelar extratos bancarios', 'EXTRATO_BANCARIO', 'CANCELAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'EXTRATO_BANCARIO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
