-- Migra automaticamente apenas ambientes com uma unica empresa ativa, evitando inferencias ambiguas.
INSERT INTO usuario_empresa (id, usuario_id, empresa_id, principal, acesso_todas_filiais, ativo,
                             version, criado_em, atualizado_em)
SELECT gen_random_uuid(), u.id, e.id, TRUE, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM usuario u
JOIN usuario_perfil up ON up.usuario_id = u.id
JOIN perfil p ON p.id = up.perfil_id AND p.nome = 'ADMIN'
CROSS JOIN LATERAL (
    SELECT id FROM empresa WHERE ativo = TRUE AND deleted_at IS NULL
    AND (SELECT count(*) FROM empresa WHERE ativo = TRUE AND deleted_at IS NULL) = 1
) e
WHERE u.ativo = TRUE AND u.deleted_at IS NULL
ON CONFLICT (usuario_id, empresa_id) DO NOTHING;
