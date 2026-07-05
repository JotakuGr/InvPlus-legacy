INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000540', 'Administrar unidades do produto', 'PRODUTO_UNIDADE', '*', TRUE),
    ('00000000-0000-0000-0000-000000000541', 'Criar unidades do produto', 'PRODUTO_UNIDADE', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000542', 'Visualizar unidades do produto', 'PRODUTO_UNIDADE', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000543', 'Editar unidades do produto', 'PRODUTO_UNIDADE', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000544', 'Excluir unidades do produto', 'PRODUTO_UNIDADE', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PRODUTO_UNIDADE'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
