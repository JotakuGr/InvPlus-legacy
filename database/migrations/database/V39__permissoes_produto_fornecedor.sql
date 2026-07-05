-- Permissoes da entidade ProdutoFornecedor.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000506', 'Administrar fornecedores do produto', 'PRODUTO_FORNECEDOR', '*', TRUE),
    ('00000000-0000-0000-0000-000000000507', 'Criar fornecedores do produto', 'PRODUTO_FORNECEDOR', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000508', 'Visualizar fornecedores do produto', 'PRODUTO_FORNECEDOR', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000509', 'Editar fornecedores do produto', 'PRODUTO_FORNECEDOR', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000510', 'Excluir fornecedores do produto', 'PRODUTO_FORNECEDOR', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso = 'PRODUTO_FORNECEDOR'
  AND permissao.acao IN ('*', 'CRIAR', 'VISUALIZAR', 'EDITAR', 'EXCLUIR')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
