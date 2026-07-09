INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001030', 'Administrar relatorios financeiros', 'RELATORIO_FINANCEIRO', '*', TRUE),
    ('00000000-0000-0000-0000-000000001031', 'Visualizar relatorios financeiros', 'RELATORIO_FINANCEIRO', 'VISUALIZAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'RELATORIO_FINANCEIRO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
