package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para criar ou atualizar paises.
 */
public record PaisRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(min = 2, max = 2) String codigoIso2,
        @Size(min = 3, max = 3) String codigoIso3,
        @Size(max = 10) String codigoBacen
) {
}
