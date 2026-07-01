INSERT INTO perfil (id, nome, descricao, ativo, criado_em)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'ADMIN',
    'Administrador geral do sistema',
    TRUE,
    NOW()
)
ON CONFLICT (nome) DO UPDATE
SET descricao = EXCLUDED.descricao,
    ativo = TRUE;

INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'Acesso total de seguranca', 'SEGURANCA', '*', TRUE),
    ('00000000-0000-0000-0000-000000000102', 'Criar usuarios', 'USUARIO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000103', 'Visualizar usuarios', 'USUARIO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000104', 'Editar usuarios', 'USUARIO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000105', 'Excluir usuarios', 'USUARIO', 'EXCLUIR', TRUE),
    ('00000000-0000-0000-0000-000000000106', 'Criar perfis', 'PERFIL', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000107', 'Visualizar perfis', 'PERFIL', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000108', 'Editar perfis', 'PERFIL', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000109', 'Excluir perfis', 'PERFIL', 'EXCLUIR', TRUE),
    ('00000000-0000-0000-0000-000000000110', 'Criar permissoes', 'PERMISSAO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000111', 'Visualizar permissoes', 'PERMISSAO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000112', 'Editar permissoes', 'PERMISSAO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000113', 'Excluir permissoes', 'PERMISSAO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO usuario (id, nome, email, senha_hash, ativo, criado_em, atualizado_em)
VALUES (
    '00000000-0000-0000-0000-000000000201',
    'Administrador',
    'admin@invplus.com',
    crypt('admin123', gen_salt('bf', 10)),
    TRUE,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE,
    deleted_at = NULL,
    atualizado_em = NOW();

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso IN ('SEGURANCA', 'USUARIO', 'PERFIL', 'PERMISSAO')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;

INSERT INTO usuario_perfil (usuario_id, perfil_id, criado_em)
SELECT usuario.id, perfil.id, NOW()
FROM usuario
CROSS JOIN perfil
WHERE usuario.email = 'admin@invplus.com'
  AND perfil.nome = 'ADMIN'
ON CONFLICT (usuario_id, perfil_id) DO NOTHING;
