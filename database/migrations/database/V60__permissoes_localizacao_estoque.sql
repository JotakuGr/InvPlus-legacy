INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000590', 'Administrar localizacoes de estoque', 'LOCALIZACAO_ESTOQUE', '*', TRUE),
    ('00000000-0000-0000-0000-000000000591', 'Criar localizacoes de estoque', 'LOCALIZACAO_ESTOQUE', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000592', 'Visualizar localizacoes de estoque', 'LOCALIZACAO_ESTOQUE', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000593', 'Editar localizacoes de estoque', 'LOCALIZACAO_ESTOQUE', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000594', 'Excluir localizacoes de estoque', 'LOCALIZACAO_ESTOQUE', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'LOCALIZACAO_ESTOQUE'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
