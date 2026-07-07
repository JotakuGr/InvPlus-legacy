INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000610', 'Administrar estoques', 'ESTOQUE_SALDO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000611', 'Criar estoques', 'ESTOQUE_SALDO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000612', 'Visualizar estoques', 'ESTOQUE_SALDO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000613', 'Editar estoques', 'ESTOQUE_SALDO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000614', 'Excluir estoques', 'ESTOQUE_SALDO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'ESTOQUE_SALDO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
