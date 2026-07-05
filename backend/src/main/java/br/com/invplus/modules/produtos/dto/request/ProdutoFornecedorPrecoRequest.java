package br.com.invplus.modules.produtos.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados de entrada para registrar preco historico de ProdutoFornecedor.
 */
public record ProdutoFornecedorPrecoRequest(
        @NotNull UUID produtoFornecedorId,
        @NotNull @PositiveOrZero BigDecimal preco,
        @NotNull LocalDate vigenciaInicio,
        LocalDate vigenciaFim
) {
}
