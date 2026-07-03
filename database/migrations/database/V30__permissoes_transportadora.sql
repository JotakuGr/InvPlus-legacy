-- Permissoes da entidade Transportadora no modulo Pessoas.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000441', 'Administrar transportadoras', 'TRANSPORTADORA', '*', TRUE),
    ('00000000-0000-0000-0000-000000000442', 'Criar transportadoras', 'TRANSPORTADORA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000443', 'Visualizar transportadoras', 'TRANSPORTADORA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000444', 'Editar transportadoras', 'TRANSPORTADORA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000445', 'Excluir transportadoras', 'TRANSPORTADORA', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'TRANSPORTADORA'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
