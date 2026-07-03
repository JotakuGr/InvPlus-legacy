-- Permissoes especificas para administracao de Empresa.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000333', 'Administrar empresas', 'EMPRESA', '*', TRUE),
    ('00000000-0000-0000-0000-000000000323', 'Criar empresas', 'EMPRESA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000324', 'Visualizar empresas', 'EMPRESA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000325', 'Editar empresas', 'EMPRESA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000326', 'Excluir empresas', 'EMPRESA', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'EMPRESA'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
