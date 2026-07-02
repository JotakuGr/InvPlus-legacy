package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Payload para criar ou atualizar estados.
 */
public record EstadoRequest(
        @NotNull UUID paisId,
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(min = 2, max = 2) String uf,
        @Size(max = 10) String codigoIbge
) {
}
