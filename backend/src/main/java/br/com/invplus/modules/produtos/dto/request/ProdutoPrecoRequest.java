package br.com.invplus.modules.produtos.dto.request;

import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados de entrada para registrar preco comercial do produto.
 */
public record ProdutoPrecoRequest(
        @NotNull UUID produtoId,
        UUID empresaId,
        @NotNull TipoPrecoProduto tipoPreco,
        @NotNull @PositiveOrZero BigDecimal preco,
        @NotNull LocalDate vigenciaInicio,
        LocalDate vigenciaFim,
        Boolean principal,
        String observacao
) {
}
