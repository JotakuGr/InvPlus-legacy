ALTER TABLE permissao
    DROP CONSTRAINT IF EXISTS chk_permissao_acao;

ALTER TABLE permissao
    ADD CONSTRAINT chk_permissao_acao
        CHECK (acao IN (
            'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR', 'EXPORTAR', 'APROVAR',
            'CANCELAR', 'BAIXAR', 'CONFERIR', 'EFETIVAR', 'FATURAR', 'IMPORTAR',
            'SUGERIR', '*'
        ));

INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES ('00000000-0000-0000-0000-000000001210',
        'Consultar sugestoes de conciliacao bancaria',
        'CONCILIACAO_BANCARIA', 'SUGERIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'CONCILIACAO_BANCARIA'
  AND permissao.acao = 'SUGERIR'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
