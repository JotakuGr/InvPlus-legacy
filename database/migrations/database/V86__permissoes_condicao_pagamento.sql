INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000860', 'Administrar condicoes de pagamento', 'CONDICAO_PAGAMENTO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000861', 'Criar condicoes de pagamento', 'CONDICAO_PAGAMENTO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000862', 'Visualizar condicoes de pagamento', 'CONDICAO_PAGAMENTO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000863', 'Editar condicoes de pagamento', 'CONDICAO_PAGAMENTO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000864', 'Excluir condicoes de pagamento', 'CONDICAO_PAGAMENTO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CONDICAO_PAGAMENTO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
