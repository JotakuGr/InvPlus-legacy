INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000930', 'Administrar recebimentos', 'RECEBIMENTO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000931', 'Criar recebimentos', 'RECEBIMENTO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000932', 'Visualizar recebimentos', 'RECEBIMENTO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000933', 'Cancelar recebimentos', 'RECEBIMENTO', 'CANCELAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'RECEBIMENTO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
