package br.com.invplus.modules.cadastros.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para criar ou atualizar unidades de medida.
 */
public record UnidadeMedidaRequest(
        @NotBlank @Size(max = 10) String sigla,
        @NotBlank @Size(max = 80) String descricao,
        @Min(0) @Max(6) Short casasDecimais
) {
}
