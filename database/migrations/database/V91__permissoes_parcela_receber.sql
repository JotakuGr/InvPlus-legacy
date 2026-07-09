INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000910', 'Administrar parcelas a receber', 'PARCELA_RECEBER', '*', TRUE),
    ('00000000-0000-0000-0000-000000000911', 'Criar parcelas a receber', 'PARCELA_RECEBER', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000912', 'Visualizar parcelas a receber', 'PARCELA_RECEBER', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000913', 'Editar parcelas a receber', 'PARCELA_RECEBER', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000914', 'Cancelar parcelas a receber', 'PARCELA_RECEBER', 'CANCELAR', TRUE),
    ('00000000-0000-0000-0000-000000000915', 'Baixar parcelas a receber', 'PARCELA_RECEBER', 'BAIXAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PARCELA_RECEBER'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
