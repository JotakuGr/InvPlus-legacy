ALTER TABLE permissao DROP CONSTRAINT IF EXISTS chk_permissao_acao;
ALTER TABLE permissao ADD CONSTRAINT chk_permissao_acao CHECK (acao IN (
    'CRIAR','VISUALIZAR','EDITAR','EXCLUIR','EXPORTAR','APROVAR','CANCELAR','BAIXAR',
    'CONFERIR','EFETIVAR','FATURAR','IMPORTAR','SUGERIR','PAINEL','*'
));

INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES ('00000000-0000-0000-0000-000000001220', 'Acessar painel de conciliacao bancaria',
        'CONCILIACAO_BANCARIA', 'PAINEL', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE SET nome = EXCLUDED.nome, ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM perfil p CROSS JOIN permissao pe
WHERE p.nome = 'ADMIN' AND pe.recurso = 'CONCILIACAO_BANCARIA' AND pe.acao = 'PAINEL'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
