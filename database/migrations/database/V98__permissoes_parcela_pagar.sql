INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000980', 'Administrar parcelas a pagar', 'PARCELA_PAGAR', '*', TRUE),
    ('00000000-0000-0000-0000-000000000981', 'Criar parcelas a pagar', 'PARCELA_PAGAR', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000982', 'Visualizar parcelas a pagar', 'PARCELA_PAGAR', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000983', 'Editar parcelas a pagar', 'PARCELA_PAGAR', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000984', 'Cancelar parcelas a pagar', 'PARCELA_PAGAR', 'CANCELAR', TRUE),
    ('00000000-0000-0000-0000-000000000985', 'Baixar parcelas a pagar', 'PARCELA_PAGAR', 'BAIXAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PARCELA_PAGAR'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
