INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000760', 'Administrar pedidos de venda', 'PEDIDO_VENDA', '*', TRUE),
    ('00000000-0000-0000-0000-000000000761', 'Criar pedidos de venda', 'PEDIDO_VENDA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000762', 'Visualizar pedidos de venda', 'PEDIDO_VENDA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000763', 'Editar pedidos de venda', 'PEDIDO_VENDA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000764', 'Aprovar pedidos de venda', 'PEDIDO_VENDA', 'APROVAR', TRUE),
    ('00000000-0000-0000-0000-000000000765', 'Cancelar pedidos de venda', 'PEDIDO_VENDA', 'CANCELAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PEDIDO_VENDA'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
