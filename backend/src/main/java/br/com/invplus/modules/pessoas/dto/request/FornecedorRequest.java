package br.com.invplus.modules.pessoas.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

/**
 * Dados de entrada para criar ou atualizar o papel Fornecedor de uma Pessoa.
 */
public record FornecedorRequest(
        @NotNull UUID pessoaId,
        UUID condicaoPagamentoId,
        @PositiveOrZero Integer prazoMedioEntrega,
        String observacao
) {
}
