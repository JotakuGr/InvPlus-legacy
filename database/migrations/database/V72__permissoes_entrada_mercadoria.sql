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
            '*'
        ));

INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000710', 'Administrar entradas de mercadoria', 'ENTRADA_MERCADORIA', '*', TRUE),
    ('00000000-0000-0000-0000-000000000711', 'Criar entradas de mercadoria', 'ENTRADA_MERCADORIA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000712', 'Visualizar entradas de mercadoria', 'ENTRADA_MERCADORIA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000713', 'Editar entradas de mercadoria', 'ENTRADA_MERCADORIA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000714', 'Cancelar entradas de mercadoria', 'ENTRADA_MERCADORIA', 'CANCELAR', TRUE),
    ('00000000-0000-0000-0000-000000000715', 'Conferir entradas de mercadoria', 'ENTRADA_MERCADORIA', 'CONFERIR', TRUE),
    ('00000000-0000-0000-0000-000000000716', 'Efetivar entradas de mercadoria', 'ENTRADA_MERCADORIA', 'EFETIVAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'ENTRADA_MERCADORIA'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
