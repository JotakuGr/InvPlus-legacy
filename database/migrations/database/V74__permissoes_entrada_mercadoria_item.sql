INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000730', 'Administrar itens de entrada de mercadoria', 'ENTRADA_MERCADORIA_ITEM', '*', TRUE),
    ('00000000-0000-0000-0000-000000000731', 'Criar itens de entrada de mercadoria', 'ENTRADA_MERCADORIA_ITEM', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000732', 'Visualizar itens de entrada de mercadoria', 'ENTRADA_MERCADORIA_ITEM', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000733', 'Editar itens de entrada de mercadoria', 'ENTRADA_MERCADORIA_ITEM', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000734', 'Excluir itens de entrada de mercadoria', 'ENTRADA_MERCADORIA_ITEM', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'ENTRADA_MERCADORIA_ITEM'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
