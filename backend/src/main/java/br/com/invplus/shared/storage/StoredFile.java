package br.com.invplus.shared.storage;

/**
 * Resultado normalizado de um upload realizado por qualquer provider.
 */
public record StoredFile(
        String originalName,
        String storedName,
        String contentType,
        String extension,
        long sizeBytes,
        String sha256,
        String url,
        StorageProvider provider
) {
}
