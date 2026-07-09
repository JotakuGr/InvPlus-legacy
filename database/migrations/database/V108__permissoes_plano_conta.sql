INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001070', 'Administrar planos de contas', 'PLANO_CONTA', '*', TRUE),
    ('00000000-0000-0000-0000-000000001071', 'Criar planos de contas', 'PLANO_CONTA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000001072', 'Visualizar planos de contas', 'PLANO_CONTA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000001073', 'Editar planos de contas', 'PLANO_CONTA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000001074', 'Excluir planos de contas', 'PLANO_CONTA', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PLANO_CONTA'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
