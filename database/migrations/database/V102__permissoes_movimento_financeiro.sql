INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001020', 'Administrar movimentos financeiros', 'MOVIMENTO_FINANCEIRO', '*', TRUE),
    ('00000000-0000-0000-0000-000000001021', 'Criar movimentos financeiros', 'MOVIMENTO_FINANCEIRO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000001022', 'Visualizar movimentos financeiros', 'MOVIMENTO_FINANCEIRO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000001023', 'Cancelar movimentos financeiros', 'MOVIMENTO_FINANCEIRO', 'CANCELAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'MOVIMENTO_FINANCEIRO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
