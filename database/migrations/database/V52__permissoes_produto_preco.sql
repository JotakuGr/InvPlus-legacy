INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000530', 'Administrar precos de produto', 'PRODUTO_PRECO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000531', 'Criar precos de produto', 'PRODUTO_PRECO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000532', 'Visualizar precos de produto', 'PRODUTO_PRECO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000533', 'Editar precos de produto', 'PRODUTO_PRECO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000534', 'Excluir precos de produto', 'PRODUTO_PRECO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PRODUTO_PRECO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
