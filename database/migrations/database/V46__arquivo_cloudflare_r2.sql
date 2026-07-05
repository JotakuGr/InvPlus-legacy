-- Permite preparar o modulo de Arquivo para storage Cloudflare R2.
ALTER TABLE arquivo DROP CONSTRAINT IF EXISTS chk_arquivo_storage;

ALTER TABLE arquivo
    ADD CONSTRAINT chk_arquivo_storage
    CHECK (storage IN ('LOCAL', 'S3', 'MINIO', 'AZURE', 'GOOGLE_CLOUD', 'CLOUDFLARE_R2'));

COMMENT ON COLUMN arquivo.storage IS 'Provedor fisico de armazenamento: LOCAL, S3, MINIO, AZURE, GOOGLE_CLOUD ou CLOUDFLARE_R2';
