INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001040', 'Administrar centros de custo', 'CENTRO_CUSTO', '*', TRUE),
    ('00000000-0000-0000-0000-000000001041', 'Criar centros de custo', 'CENTRO_CUSTO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000001042', 'Visualizar centros de custo', 'CENTRO_CUSTO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000001043', 'Editar centros de custo', 'CENTRO_CUSTO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000001044', 'Excluir centros de custo', 'CENTRO_CUSTO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CENTRO_CUSTO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
