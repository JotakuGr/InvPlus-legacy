package br.com.invplus.modules.financeiro.dto.request;

import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Dados para criar ou atualizar um centro de custo.
 */
public record CentroCustoRequest(
        UUID centroCustoPaiId,
        @NotBlank @Size(max = 40) String codigo,
        @NotBlank @Size(max = 120) String nome,
        String descricao,
        @NotNull TipoCentroCusto tipo
) {
}
