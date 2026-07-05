package br.com.invplus.shared.storage;

/**
 * Contrato comum para provedores de armazenamento de arquivos.
 */
public interface StorageService {

    /**
     * Armazena o conteudo recebido e retorna os metadados calculados.
     */
    StoredFile upload(byte[] content, String originalName, String contentType);

    /**
     * Remove fisicamente um arquivo pelo nome armazenado.
     */
    void delete(String storedName);

    /**
     * Monta a URL publica para o arquivo armazenado.
     */
    String publicUrl(String storedName);

    /**
     * Identifica o provider implementado por este service.
     */
    StorageProvider provider();
}
