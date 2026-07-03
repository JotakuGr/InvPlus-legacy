-- Libera colunas legadas apos a migracao para tipo_mime/url/hash_sha256.
ALTER TABLE arquivo ALTER COLUMN content_type DROP NOT NULL;
ALTER TABLE arquivo ALTER COLUMN caminho DROP NOT NULL;
