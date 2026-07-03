-- Permissoes da entidade Fornecedor no modulo Pessoas.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000421', 'Administrar fornecedores', 'FORNECEDOR', '*', TRUE),
    ('00000000-0000-0000-0000-000000000422', 'Criar fornecedores', 'FORNECEDOR', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000423', 'Visualizar fornecedores', 'FORNECEDOR', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000424', 'Editar fornecedores', 'FORNECEDOR', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000425', 'Excluir fornecedores', 'FORNECEDOR', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'FORNECEDOR'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
