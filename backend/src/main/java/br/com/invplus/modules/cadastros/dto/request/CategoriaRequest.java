package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Payload para criar ou atualizar categorias com hierarquia opcional.
 */
public record CategoriaRequest(
        @NotBlank @Size(max = 100) String nome,
        String descricao,
        UUID categoriaPaiId,
        Integer ordem
) {
}
