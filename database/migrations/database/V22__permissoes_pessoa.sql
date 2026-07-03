-- Permissoes do modulo Pessoas para a entidade Pessoa.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000401', 'Administrar modulo Pessoas', 'PESSOAS', '*', TRUE),
    ('00000000-0000-0000-0000-000000000402', 'Administrar pessoas', 'PESSOA', '*', TRUE),
    ('00000000-0000-0000-0000-000000000403', 'Criar pessoas', 'PESSOA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000404', 'Visualizar pessoas', 'PESSOA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000405', 'Editar pessoas', 'PESSOA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000406', 'Excluir pessoas', 'PESSOA', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND (
      (permissao.recurso = 'PESSOAS' AND permissao.acao = '*')
      OR (permissao.recurso = 'PESSOA' AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR'))
  )
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
