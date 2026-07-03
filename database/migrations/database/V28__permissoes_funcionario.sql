-- Permissoes da entidade Funcionario no modulo Pessoas.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000431', 'Administrar funcionarios', 'FUNCIONARIO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000432', 'Criar funcionarios', 'FUNCIONARIO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000433', 'Visualizar funcionarios', 'FUNCIONARIO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000434', 'Editar funcionarios', 'FUNCIONARIO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000435', 'Excluir funcionarios', 'FUNCIONARIO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'FUNCIONARIO'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
