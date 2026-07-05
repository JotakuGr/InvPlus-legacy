INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000390', 'Administrar codigos de barras de produto', 'PRODUTO_CODIGO_BARRAS', '*', TRUE),
    ('00000000-0000-0000-0000-000000000391', 'Criar codigos de barras de produto', 'PRODUTO_CODIGO_BARRAS', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000392', 'Visualizar codigos de barras de produto', 'PRODUTO_CODIGO_BARRAS', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000393', 'Editar codigos de barras de produto', 'PRODUTO_CODIGO_BARRAS', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000394', 'Excluir codigos de barras de produto', 'PRODUTO_CODIGO_BARRAS', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PRODUTO_CODIGO_BARRAS'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
