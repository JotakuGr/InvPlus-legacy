package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Payload para registrar metadados de arquivos.
 */
public record ArquivoRequest(
        @NotBlank @Size(max = 255) String nomeOriginal,
        @NotBlank @Size(max = 255) String nomeArmazenado,
        @NotBlank @Size(max = 120) String contentType,
        @NotNull @Min(0) Long tamanhoBytes,
        @NotBlank @Size(max = 500) String caminho,
        @Size(max = 64) String checksumSha256,
        @Size(max = 80) String entidade,
        UUID entidadeId
) {
}
