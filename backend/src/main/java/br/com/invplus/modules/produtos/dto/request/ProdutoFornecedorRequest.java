package br.com.invplus.modules.produtos.dto.request;

import br.com.invplus.modules.produtos.enums.FornecedorProdutoStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

/**
 * Dados de entrada para vincular Produto e Fornecedor.
 */
public record ProdutoFornecedorRequest(
        @NotNull UUID produtoId,
        @NotNull UUID fornecedorId,
        String codigoFornecedor,
        @PositiveOrZero Integer leadTimeDias,
        Boolean principal,
        @PositiveOrZero Integer prioridade,
        FornecedorProdutoStatus status,
        String observacao
) {
}
