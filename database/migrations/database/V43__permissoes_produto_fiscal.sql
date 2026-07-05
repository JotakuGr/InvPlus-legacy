-- Permissoes dos dados fiscais de produto.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000516', 'Administrar dados fiscais de produto', 'PRODUTO_FISCAL', '*', TRUE),
    ('00000000-0000-0000-0000-000000000517', 'Criar dados fiscais de produto', 'PRODUTO_FISCAL', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000518', 'Visualizar dados fiscais de produto', 'PRODUTO_FISCAL', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000519', 'Editar dados fiscais de produto', 'PRODUTO_FISCAL', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000520', 'Excluir dados fiscais de produto', 'PRODUTO_FISCAL', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PRODUTO_FISCAL'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
