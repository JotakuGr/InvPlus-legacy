package br.com.invplus.modules.cadastros.dto.request;

import br.com.invplus.modules.cadastros.enums.StorageArquivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Payload para registrar metadados de arquivos.
 */
public record ArquivoRequest(
        @NotBlank @Size(max = 255) String nomeOriginal,
        @NotBlank @Size(max = 255) String nomeArmazenado,
        @NotBlank @Size(max = 120) String tipoMime,
        @NotBlank @Size(max = 20) String extensao,
        @NotNull @Positive Long tamanhoBytes,
        @NotBlank @Pattern(regexp = "^[A-Fa-f0-9]{64}$") String hashSha256,
        @NotBlank @Size(max = 500) String url,
        @NotNull StorageArquivo storage,
        @NotBlank @Size(max = 80) String entidade,
        @NotNull UUID entidadeId
) {
}
