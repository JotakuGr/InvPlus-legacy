package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para criar ou atualizar marcas.
 */
public record MarcaRequest(
        @NotBlank @Size(max = 100) String nome,
        String descricao
) {
}
