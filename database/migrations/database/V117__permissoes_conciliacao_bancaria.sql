INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001170', 'Administrar conciliacoes bancarias', 'CONCILIACAO_BANCARIA', '*', TRUE),
    ('00000000-0000-0000-0000-000000001171', 'Criar conciliacoes bancarias', 'CONCILIACAO_BANCARIA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000001172', 'Visualizar conciliacoes bancarias', 'CONCILIACAO_BANCARIA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000001173', 'Cancelar conciliacoes bancarias', 'CONCILIACAO_BANCARIA', 'CANCELAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CONCILIACAO_BANCARIA'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
