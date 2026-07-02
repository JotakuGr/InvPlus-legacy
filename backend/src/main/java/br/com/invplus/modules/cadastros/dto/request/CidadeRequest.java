package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Payload para criar ou atualizar cidades.
 */
public record CidadeRequest(
        @NotNull UUID estadoId,
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 10) String codigoIbge
) {
}
