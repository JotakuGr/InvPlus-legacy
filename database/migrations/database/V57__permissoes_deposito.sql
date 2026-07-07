INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000560', 'Administrar modulo Estoque', 'ESTOQUE', '*', TRUE),
    ('00000000-0000-0000-0000-000000000561', 'Administrar depositos', 'DEPOSITO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000562', 'Criar depositos', 'DEPOSITO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000563', 'Visualizar depositos', 'DEPOSITO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000564', 'Editar depositos', 'DEPOSITO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000565', 'Excluir depositos', 'DEPOSITO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso IN ('ESTOQUE', 'DEPOSITO')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
