INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000630', 'Administrar movimentacoes de estoque', 'MOVIMENTACAO_ESTOQUE', '*', TRUE),
    ('00000000-0000-0000-0000-000000000631', 'Visualizar movimentacoes de estoque', 'MOVIMENTACAO_ESTOQUE', 'VISUALIZAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'MOVIMENTACAO_ESTOQUE'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
