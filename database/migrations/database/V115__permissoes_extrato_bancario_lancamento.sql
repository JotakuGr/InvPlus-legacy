INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001150', 'Administrar lancamentos de extrato bancario', 'EXTRATO_BANCARIO_LANCAMENTO', '*', TRUE),
    ('00000000-0000-0000-0000-000000001151', 'Criar lancamentos de extrato bancario', 'EXTRATO_BANCARIO_LANCAMENTO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000001152', 'Visualizar lancamentos de extrato bancario', 'EXTRATO_BANCARIO_LANCAMENTO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000001153', 'Editar lancamentos de extrato bancario', 'EXTRATO_BANCARIO_LANCAMENTO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000001154', 'Excluir lancamentos de extrato bancario', 'EXTRATO_BANCARIO_LANCAMENTO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'EXTRATO_BANCARIO_LANCAMENTO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
