INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000385', 'Administrar imagens de produto', 'PRODUTO_IMAGEM', '*', TRUE),
    ('00000000-0000-0000-0000-000000000386', 'Criar imagens de produto', 'PRODUTO_IMAGEM', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000387', 'Visualizar imagens de produto', 'PRODUTO_IMAGEM', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000388', 'Editar imagens de produto', 'PRODUTO_IMAGEM', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000389', 'Excluir imagens de produto', 'PRODUTO_IMAGEM', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PRODUTO_IMAGEM'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
