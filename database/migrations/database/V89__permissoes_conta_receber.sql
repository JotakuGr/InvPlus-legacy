INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000890', 'Administrar contas a receber', 'CONTA_RECEBER', '*', TRUE),
    ('00000000-0000-0000-0000-000000000891', 'Criar contas a receber', 'CONTA_RECEBER', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000892', 'Visualizar contas a receber', 'CONTA_RECEBER', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000893', 'Editar contas a receber', 'CONTA_RECEBER', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000894', 'Cancelar contas a receber', 'CONTA_RECEBER', 'CANCELAR', TRUE),
    ('00000000-0000-0000-0000-000000000895', 'Baixar contas a receber', 'CONTA_RECEBER', 'BAIXAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CONTA_RECEBER'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
