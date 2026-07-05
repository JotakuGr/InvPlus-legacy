package br.com.invplus.shared.storage;

import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Publica arquivos locais em /uploads/** apenas para desenvolvimento e testes.
 */
@Configuration
public class StorageWebConfig implements WebMvcConfigurer {

    private final StorageProperties properties;

    public StorageWebConfig(StorageProperties properties) {
        this.properties = properties;
    }

    /**
     * Mapeia a pasta local configurada para URLs publicas locais.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String localPath = properties.localPath() == null || properties.localPath().isBlank()
                ? "./uploads"
                : properties.localPath();
        String location = Path.of(localPath).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
