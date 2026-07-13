package br.com.invplus.modules.financeiro.dto.request;

import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Dados para criar ou atualizar uma conta do plano.
 */
public record PlanoContaRequest(
        UUID planoContaPaiId,
        @NotBlank @Size(max = 40) String codigo,
        @NotBlank @Size(max = 120) String nome,
        String descricao,
        @NotNull TipoPlanoConta tipo,
        @NotNull NaturezaPlanoConta natureza
) {
}
