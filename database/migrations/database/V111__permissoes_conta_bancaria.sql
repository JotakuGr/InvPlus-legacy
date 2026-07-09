INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001100', 'Administrar contas bancarias', 'CONTA_BANCARIA', '*', TRUE),
    ('00000000-0000-0000-0000-000000001101', 'Criar contas bancarias', 'CONTA_BANCARIA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000001102', 'Visualizar contas bancarias', 'CONTA_BANCARIA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000001103', 'Editar contas bancarias', 'CONTA_BANCARIA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000001104', 'Excluir contas bancarias', 'CONTA_BANCARIA', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CONTA_BANCARIA'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
