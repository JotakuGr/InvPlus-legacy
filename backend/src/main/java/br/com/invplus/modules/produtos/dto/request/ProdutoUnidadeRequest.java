package br.com.invplus.modules.produtos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dados de entrada para unidade alternativa do produto.
 */
public record ProdutoUnidadeRequest(
        @NotNull UUID produtoId,
        @NotNull UUID unidadeMedidaId,
        @NotNull @DecimalMin(value = "0.000001") BigDecimal fatorConversao,
        Boolean unidadePadrao,
        Boolean permiteCompra,
        Boolean permiteVenda,
        Boolean permiteEstoque,
        Boolean permiteFracionamento,
        String observacao
) {
}
