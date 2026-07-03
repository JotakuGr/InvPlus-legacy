-- Permissoes da entidade Cliente no modulo Pessoas.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000411', 'Administrar clientes', 'CLIENTE', '*', TRUE),
    ('00000000-0000-0000-0000-000000000412', 'Criar clientes', 'CLIENTE', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000413', 'Visualizar clientes', 'CLIENTE', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000414', 'Editar clientes', 'CLIENTE', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000415', 'Excluir clientes', 'CLIENTE', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CLIENTE'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
