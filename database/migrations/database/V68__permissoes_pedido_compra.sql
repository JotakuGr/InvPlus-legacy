INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000670', 'Administrar pedidos de compra', 'PEDIDO_COMPRA', '*', TRUE),
    ('00000000-0000-0000-0000-000000000671', 'Criar pedidos de compra', 'PEDIDO_COMPRA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000672', 'Visualizar pedidos de compra', 'PEDIDO_COMPRA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000673', 'Editar pedidos de compra', 'PEDIDO_COMPRA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000674', 'Cancelar pedidos de compra', 'PEDIDO_COMPRA', 'CANCELAR', TRUE),
    ('00000000-0000-0000-0000-000000000675', 'Aprovar pedidos de compra', 'PEDIDO_COMPRA', 'APROVAR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PEDIDO_COMPRA'
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
