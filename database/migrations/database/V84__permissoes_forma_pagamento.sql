INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000840', 'Administrar formas de pagamento', 'FORMA_PAGAMENTO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000841', 'Criar formas de pagamento', 'FORMA_PAGAMENTO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000842', 'Visualizar formas de pagamento', 'FORMA_PAGAMENTO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000843', 'Editar formas de pagamento', 'FORMA_PAGAMENTO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000844', 'Excluir formas de pagamento', 'FORMA_PAGAMENTO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'FORMA_PAGAMENTO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
