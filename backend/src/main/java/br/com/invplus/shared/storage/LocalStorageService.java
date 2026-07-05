package br.com.invplus.shared.storage;

import br.com.invplus.shared.domain.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Implementacao local para desenvolvimento e testes, gravando arquivos em disco.
 */
@Service
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;

    public LocalStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    /**
     * Salva o arquivo em subpastas por data, calcula SHA-256 e devolve a URL publica.
     */
    @Override
    public StoredFile upload(byte[] content, String originalName, String contentType) {
        String extension = extensionFrom(originalName);
        String storedName = storedName(extension);
        Path target = rootPath().resolve(storedName).normalize();

        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException ex) {
            throw new BusinessException("Nao foi possivel armazenar o arquivo localmente", ex);
        }

        return new StoredFile(
                originalName,
                storedName.replace("\\", "/"),
                contentType,
                extension,
                content.length,
                sha256(content),
                publicUrl(storedName),
                provider()
        );
    }

    /**
     * Remove arquivo local, ignorando ausencia para manter operacao idempotente.
     */
    @Override
    public void delete(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(rootPath().resolve(storedName).normalize());
        } catch (IOException ex) {
            throw new BusinessException("Nao foi possivel remover o arquivo local", ex);
        }
    }

    /**
     * Usa a base publica configurada para montar a URL do arquivo.
     */
    @Override
    public String publicUrl(String storedName) {
        String baseUrl = properties.publicBaseUrl() == null
                ? "http://localhost:8080/uploads"
                : properties.publicBaseUrl().replaceAll("/+$", "");
        return baseUrl + "/" + storedName.replace("\\", "/");
    }

    @Override
    public StorageProvider provider() {
        return StorageProvider.LOCAL;
    }

    private Path rootPath() {
        String localPath = properties.localPath() == null || properties.localPath().isBlank()
                ? "./uploads"
                : properties.localPath();
        return Path.of(localPath).toAbsolutePath().normalize();
    }

    private String storedName(String extension) {
        LocalDate now = LocalDate.now();
        return "%04d/%02d/%02d/%s.%s".formatted(
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                UUID.randomUUID(),
                extension
        );
    }

    private String extensionFrom(String originalName) {
        String fileName = originalName == null ? "" : Path.of(originalName).getFileName().toString();
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "bin";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException ex) {
            throw new BusinessException("Algoritmo SHA-256 indisponivel", ex);
        }
    }
}
