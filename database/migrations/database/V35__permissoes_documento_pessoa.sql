-- Permissoes da entidade DocumentoPessoa no modulo Pessoas.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000456', 'Administrar documentos de pessoas', 'DOCUMENTO_PESSOA', '*', TRUE),
    ('00000000-0000-0000-0000-000000000457', 'Criar documentos de pessoas', 'DOCUMENTO_PESSOA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000458', 'Visualizar documentos de pessoas', 'DOCUMENTO_PESSOA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000459', 'Editar documentos de pessoas', 'DOCUMENTO_PESSOA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000460', 'Excluir documentos de pessoas', 'DOCUMENTO_PESSOA', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'DOCUMENTO_PESSOA'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
