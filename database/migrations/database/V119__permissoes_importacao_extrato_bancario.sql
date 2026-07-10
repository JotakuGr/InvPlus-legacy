INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000001190', 'Administrar importacoes de extrato', 'IMPORTACAO_EXTRATO', '*', TRUE),
    ('00000000-0000-0000-0000-000000001191', 'Importar arquivos de extrato', 'IMPORTACAO_EXTRATO', 'IMPORTAR', TRUE),
    ('00000000-0000-0000-0000-000000001192', 'Visualizar importacoes de extrato', 'IMPORTACAO_EXTRATO', 'VISUALIZAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'IMPORTACAO_EXTRATO'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
