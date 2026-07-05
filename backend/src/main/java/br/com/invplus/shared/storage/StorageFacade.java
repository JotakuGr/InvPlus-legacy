package br.com.invplus.shared.storage;

import br.com.invplus.shared.domain.exception.BusinessException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Resolve o provider configurado e centraliza o acesso ao storage fisico.
 */
@Component
public class StorageFacade {

    private final StorageProperties properties;
    private final Map<StorageProvider, StorageService> services;

    public StorageFacade(StorageProperties properties, List<StorageService> services) {
        this.properties = properties;
        this.services = new EnumMap<>(StorageProvider.class);
        services.forEach(service -> this.services.put(service.provider(), service));
    }

    /**
     * Envia o upload para o provider ativo.
     */
    public StoredFile upload(byte[] content, String originalName, String contentType) {
        return currentService().upload(content, originalName, contentType);
    }

    /**
     * Remove arquivo no provider ativo.
     */
    public void delete(String storedName) {
        currentService().delete(storedName);
    }

    /**
     * Monta URL publica usando o provider ativo.
     */
    public String publicUrl(String storedName) {
        return currentService().publicUrl(storedName);
    }

    /**
     * Retorna o provider configurado, usando LOCAL como padrao conservador.
     */
    public StorageProvider provider() {
        return properties.provider() == null ? StorageProvider.LOCAL : properties.provider();
    }

    private StorageService currentService() {
        StorageService service = services.get(provider());
        if (service == null) {
            throw new BusinessException("Provider de storage ainda nao implementado: " + provider());
        }
        return service;
    }
}
