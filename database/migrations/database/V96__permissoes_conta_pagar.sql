INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000960', 'Administrar contas a pagar', 'CONTA_PAGAR', '*', TRUE),
    ('00000000-0000-0000-0000-000000000961', 'Criar contas a pagar', 'CONTA_PAGAR', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000962', 'Visualizar contas a pagar', 'CONTA_PAGAR', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000963', 'Editar contas a pagar', 'CONTA_PAGAR', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000964', 'Cancelar contas a pagar', 'CONTA_PAGAR', 'CANCELAR', TRUE),
    ('00000000-0000-0000-0000-000000000965', 'Baixar contas a pagar', 'CONTA_PAGAR', 'BAIXAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CONTA_PAGAR'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
