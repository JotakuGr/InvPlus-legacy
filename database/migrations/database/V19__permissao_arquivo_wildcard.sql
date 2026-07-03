-- Permissoes especificas para administracao de Arquivo.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000334', 'Administrar arquivos', 'ARQUIVO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000327', 'Criar arquivos', 'ARQUIVO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000328', 'Visualizar arquivos', 'ARQUIVO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000329', 'Editar arquivos', 'ARQUIVO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000330', 'Excluir arquivos', 'ARQUIVO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'ARQUIVO'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
