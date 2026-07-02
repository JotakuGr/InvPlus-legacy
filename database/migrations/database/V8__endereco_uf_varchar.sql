ALTER TABLE endereco
    ALTER COLUMN uf TYPE VARCHAR(2);

COMMENT ON COLUMN endereco.uf IS 'Sigla da unidade federativa em dois caracteres';
