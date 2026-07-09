INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001000', 'Administrar pagamentos financeiros', 'PAGAMENTO', '*', TRUE),
    ('00000000-0000-0000-0000-000000001001', 'Criar pagamentos financeiros', 'PAGAMENTO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000001002', 'Visualizar pagamentos financeiros', 'PAGAMENTO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000001003', 'Cancelar pagamentos financeiros', 'PAGAMENTO', 'CANCELAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PAGAMENTO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
