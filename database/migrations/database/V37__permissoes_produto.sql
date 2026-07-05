-- Permissoes da entidade Produto.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000501', 'Administrar produtos', 'PRODUTO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000502', 'Criar produtos', 'PRODUTO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000503', 'Visualizar produtos', 'PRODUTO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000504', 'Editar produtos', 'PRODUTO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000505', 'Excluir produtos', 'PRODUTO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PRODUTO'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
