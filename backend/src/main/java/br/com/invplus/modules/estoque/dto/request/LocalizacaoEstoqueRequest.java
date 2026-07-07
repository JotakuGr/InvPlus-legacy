package br.com.invplus.modules.estoque.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Dados de entrada para criar ou atualizar LocalizacaoEstoque.
 */
public record LocalizacaoEstoqueRequest(
        @NotNull UUID depositoId,
        @NotBlank @Size(max = 30) String codigo,
        @Size(max = 160) String descricao,
        @Size(max = 60) String rua,
        @Size(max = 60) String corredor,
        @Size(max = 60) String prateleira,
        @Size(max = 60) String nivel,
        @Size(max = 60) String posicao,
        Boolean principal,
        Boolean bloqueada,
        String observacao
) {
}
