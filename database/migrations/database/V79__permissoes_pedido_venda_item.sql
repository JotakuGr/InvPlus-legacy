INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000780', 'Administrar itens de pedido de venda', 'PEDIDO_VENDA_ITEM', '*', TRUE),
    ('00000000-0000-0000-0000-000000000781', 'Criar itens de pedido de venda', 'PEDIDO_VENDA_ITEM', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000782', 'Visualizar itens de pedido de venda', 'PEDIDO_VENDA_ITEM', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000783', 'Editar itens de pedido de venda', 'PEDIDO_VENDA_ITEM', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000784', 'Excluir itens de pedido de venda', 'PEDIDO_VENDA_ITEM', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PEDIDO_VENDA_ITEM'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
