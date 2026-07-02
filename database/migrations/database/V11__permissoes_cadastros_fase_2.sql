-- Permissoes especificas por entidade da fase 2 de Cadastros.
INSERT INTO permissao (id, nome, recurso, acao, ativo)
VALUES
    ('00000000-0000-0000-0000-000000000311', 'Criar unidades de medida', 'UNIDADE_MEDIDA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000312', 'Visualizar unidades de medida', 'UNIDADE_MEDIDA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000313', 'Editar unidades de medida', 'UNIDADE_MEDIDA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000314', 'Excluir unidades de medida', 'UNIDADE_MEDIDA', 'EXCLUIR', TRUE),
    ('00000000-0000-0000-0000-000000000315', 'Criar categorias', 'CATEGORIA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000316', 'Visualizar categorias', 'CATEGORIA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000317', 'Editar categorias', 'CATEGORIA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000318', 'Excluir categorias', 'CATEGORIA', 'EXCLUIR', TRUE),
    ('00000000-0000-0000-0000-000000000319', 'Criar marcas', 'MARCA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000320', 'Visualizar marcas', 'MARCA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000321', 'Editar marcas', 'MARCA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000322', 'Excluir marcas', 'MARCA', 'EXCLUIR', TRUE),
    ('00000000-0000-0000-0000-000000000323', 'Criar empresas', 'EMPRESA', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000324', 'Visualizar empresas', 'EMPRESA', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000325', 'Editar empresas', 'EMPRESA', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000326', 'Excluir empresas', 'EMPRESA', 'EXCLUIR', TRUE),
    ('00000000-0000-0000-0000-000000000327', 'Criar arquivos', 'ARQUIVO', 'CRIAR', TRUE),
    ('00000000-0000-0000-0000-000000000328', 'Visualizar arquivos', 'ARQUIVO', 'VISUALIZAR', TRUE),
    ('00000000-0000-0000-0000-000000000329', 'Editar arquivos', 'ARQUIVO', 'EDITAR', TRUE),
    ('00000000-0000-0000-0000-000000000330', 'Excluir arquivos', 'ARQUIVO', 'EXCLUIR', TRUE)
ON CONFLICT (recurso, acao) DO UPDATE
SET nome = EXCLUDED.nome,
    ativo = TRUE;

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT perfil.id, permissao.id
FROM perfil
CROSS JOIN permissao
WHERE perfil.nome = 'ADMIN'
  AND permissao.recurso IN ('UNIDADE_MEDIDA', 'CATEGORIA', 'MARCA', 'EMPRESA', 'ARQUIVO')
ON CONFLICT (perfil_id, permissao_id) DO NOTHING;
