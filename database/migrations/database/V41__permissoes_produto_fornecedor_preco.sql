-- Permissoes do historico de precos de ProdutoFornecedor.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000511', 'Administrar precos de fornecedores do produto', 'PRODUTO_FORNECEDOR_PRECO', '*', TRUE),
    ('00000000-0000-0000-0000-000000000512', 'Criar precos de fornecedores do produto', 'PRODUTO_FORNECEDOR_PRECO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000513', 'Visualizar precos de fornecedores do produto', 'PRODUTO_FORNECEDOR_PRECO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000514', 'Editar precos de fornecedores do produto', 'PRODUTO_FORNECEDOR_PRECO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000515', 'Excluir precos de fornecedores do produto', 'PRODUTO_FORNECEDOR_PRECO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PRODUTO_FORNECEDOR_PRECO'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
