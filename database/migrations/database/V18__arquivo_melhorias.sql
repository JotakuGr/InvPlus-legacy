-- Refina Arquivo para suportar provedores de storage, hash de integridade e vinculo generico.
ALTER TABLE arquivo ADD COLUMN IF NOT EXISTS tipo_mime VARCHAR(120);
ALTER TABLE arquivo ADD COLUMN IF NOT EXISTS extensao VARCHAR(20);
ALTER TABLE arquivo ADD COLUMN IF NOT EXISTS hash_sha256 VARCHAR(64);
ALTER TABLE arquivo ADD COLUMN IF NOT EXISTS url VARCHAR(500);
ALTER TABLE arquivo ADD COLUMN IF NOT EXISTS storage VARCHAR(30) NOT NULL DEFAULT 'LOCAL';
ALTER TABLE arquivo ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

UPDATE arquivo
SET tipo_mime = COALESCE(tipo_mime, content_type)
WHERE tipo_mime IS NULL
  AND content_type IS NOT NULL;

UPDATE arquivo
SET hash_sha256 = COALESCE(hash_sha256, checksum_sha256)
WHERE hash_sha256 IS NULL
  AND checksum_sha256 IS NOT NULL;

UPDATE arquivo
SET url = COALESCE(url, caminho)
WHERE url IS NULL
  AND caminho IS NOT NULL;

UPDATE arquivo
SET extensao = LOWER(NULLIF(REGEXP_REPLACE(nome_original, '^.*\.', ''), nome_original))
WHERE extensao IS NULL
  AND nome_original LIKE '%.%';

UPDATE arquivo
SET extensao = 'bin'
WHERE extensao IS NULL;

ALTER TABLE arquivo ALTER COLUMN tipo_mime SET NOT NULL;
ALTER TABLE arquivo ALTER COLUMN extensao SET NOT NULL;
ALTER TABLE arquivo ALTER COLUMN url SET NOT NULL;

ALTER TABLE arquivo DROP CONSTRAINT IF EXISTS chk_arquivo_tamanho;
ALTER TABLE arquivo
    ADD CONSTRAINT chk_arquivo_tamanho
    CHECK (tamanho_bytes > 0);

ALTER TABLE arquivo DROP CONSTRAINT IF EXISTS chk_arquivo_storage;
ALTER TABLE arquivo
    ADD CONSTRAINT chk_arquivo_storage
    CHECK (storage IN ('LOCAL', 'S3', 'MINIO', 'AZURE', 'GOOGLE_CLOUD'));

ALTER TABLE arquivo DROP CONSTRAINT IF EXISTS chk_arquivo_hash_sha256;
ALTER TABLE arquivo
    ADD CONSTRAINT chk_arquivo_hash_sha256
    CHECK (hash_sha256 IS NULL OR hash_sha256 ~ '^[A-Fa-f0-9]{64}$');

CREATE INDEX IF NOT EXISTS idx_arquivo_ativo
    ON arquivo (ativo)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_arquivo_hash_sha256
    ON arquivo (hash_sha256)
    WHERE ativo = TRUE AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_arquivo_entidade_ativo
    ON arquivo (entidade, entidade_id)
    WHERE ativo = TRUE AND deleted_at IS NULL;

COMMENT ON COLUMN arquivo.tipo_mime IS 'Tipo MIME informado para download e validacao do anexo';
COMMENT ON COLUMN arquivo.extensao IS 'Extensao normalizada do arquivo, sem ponto inicial';
COMMENT ON COLUMN arquivo.hash_sha256 IS 'Hash SHA-256 usado para integridade e deteccao de duplicidade';
COMMENT ON COLUMN arquivo.url IS 'URL ou caminho logico onde o arquivo pode ser recuperado';
COMMENT ON COLUMN arquivo.storage IS 'Provedor fisico de armazenamento: LOCAL, S3, MINIO, AZURE ou GOOGLE_CLOUD';
