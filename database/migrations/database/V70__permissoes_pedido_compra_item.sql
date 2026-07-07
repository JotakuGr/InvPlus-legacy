INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000690', 'Administrar itens de pedido de compra', 'PEDIDO_COMPRA_ITEM', '*', TRUE),
    ('00000000-0000-0000-0000-000000000691', 'Criar itens de pedido de compra', 'PEDIDO_COMPRA_ITEM', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000692', 'Visualizar itens de pedido de compra', 'PEDIDO_COMPRA_ITEM', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000693', 'Editar itens de pedido de compra', 'PEDIDO_COMPRA_ITEM', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000694', 'Excluir itens de pedido de compra', 'PEDIDO_COMPRA_ITEM', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PEDIDO_COMPRA_ITEM'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
