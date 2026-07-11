ALTER TABLE permissao DROP CONSTRAINT IF EXISTS chk_permissao_acao;
ALTER TABLE permissao ADD CONSTRAINT chk_permissao_acao CHECK (acao IN (
    'CRIAR','VISUALIZAR','EDITAR','EXCLUIR','EXPORTAR','APROVAR','CANCELAR','BAIXAR',
    'CONFERIR','EFETIVAR','FATURAR','IMPORTAR','SUGERIR','PAINEL','CONCILIAR_LOTE',
    'SELECIONAR','TROCAR','*'
));

INSERT INTO permissao (id, nome, recurso, acao, ativo) VALUES
('00000000-0000-0000-0000-000000001250','Administrar contexto empresarial','CONTEXTO_EMPRESARIAL','*',TRUE),
('00000000-0000-0000-0000-000000001251','Visualizar contexto empresarial','CONTEXTO_EMPRESARIAL','VISUALIZAR',TRUE),
('00000000-0000-0000-0000-000000001252','Selecionar contexto empresarial','CONTEXTO_EMPRESARIAL','SELECIONAR',TRUE),
('00000000-0000-0000-0000-000000001253','Trocar contexto empresarial','CONTEXTO_EMPRESARIAL','TROCAR',TRUE)
ON CONFLICT (recurso, acao) DO UPDATE SET nome=EXCLUDED.nome, ativo=TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM perfil p CROSS JOIN permissao pe
WHERE p.nome='ADMIN' AND pe.recurso='CONTEXTO_EMPRESARIAL'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
