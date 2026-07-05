package br.com.invplus.shared.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracoes externas para escolha e publicacao do storage.
 */
@ConfigurationProperties(prefix = "invplus.storage")
public record StorageProperties(
        StorageProvider provider,
        String localPath,
        String publicBaseUrl
) {
}
