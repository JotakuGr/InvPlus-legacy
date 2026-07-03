-- Permissoes da entidade ContatoPessoa no modulo Pessoas.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000451', 'Administrar contatos de pessoas', 'CONTATO_PESSOA', '*', TRUE),
    ('00000000-0000-0000-0000-000000000452', 'Criar contatos de pessoas', 'CONTATO_PESSOA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000453', 'Visualizar contatos de pessoas', 'CONTATO_PESSOA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000454', 'Editar contatos de pessoas', 'CONTATO_PESSOA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000455', 'Excluir contatos de pessoas', 'CONTATO_PESSOA', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CONTATO_PESSOA'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
