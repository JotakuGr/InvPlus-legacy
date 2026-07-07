ALTER TABLE permissao
    DROP CONSTRAINT IF EXISTS chk_permissao_acao;

ALTER TABLE permissao
    ADD CONSTRAINT chk_permissao_acao
        CHECK (acao IN (
            'CRIAR',
            'VISUALIZAR',
            'EDITAR',
            'EXCLUIR',
            'EXPORTAR',
            'APROVAR',
            'CANCELAR',
            'BAIXAR',
            '*'
        ));

INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000650', 'Administrar reservas de estoque', 'RESERVA_ESTOQUE', '*', TRUE),
    ('00000000-0000-0000-0000-000000000651', 'Criar reservas de estoque', 'RESERVA_ESTOQUE', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000652', 'Visualizar reservas de estoque', 'RESERVA_ESTOQUE', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000653', 'Cancelar reservas de estoque', 'RESERVA_ESTOQUE', 'CANCELAR', TRUE),
    ('00000000-0000-0000-0000-000000000654', 'Baixar reservas de estoque', 'RESERVA_ESTOQUE', 'BAIXAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'RESERVA_ESTOQUE'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
