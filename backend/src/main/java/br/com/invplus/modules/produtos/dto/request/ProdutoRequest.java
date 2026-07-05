package br.com.invplus.modules.produtos.dto.request;

import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dados de entrada para criar ou atualizar Produto.
 */
public record ProdutoRequest(
        @NotNull UUID categoriaId,
        UUID marcaId,
        @NotNull UUID unidadeMedidaId,
        @NotBlank String codigo,
        String sku,
        String codigoInterno,
        String codigoBarras,
        @NotBlank String nome,
        String descricao,
        TipoProduto tipoProduto,
        NaturezaProduto naturezaProduto,
        StatusProduto statusProduto,
        @PositiveOrZero BigDecimal precoCusto,
        @PositiveOrZero BigDecimal precoVenda,
        @PositiveOrZero BigDecimal pesoLiquido,
        @PositiveOrZero BigDecimal pesoBruto,
        @PositiveOrZero BigDecimal alturaCm,
        @PositiveOrZero BigDecimal larguraCm,
        @PositiveOrZero BigDecimal comprimentoCm,
        @PositiveOrZero BigDecimal estoqueMinimo,
        Boolean controlaLote,
        Boolean controlaValidade,
        Boolean permiteFracionamento,
        Boolean controlaEstoque,
        Boolean permiteCompra,
        Boolean permiteVenda
) {
}
