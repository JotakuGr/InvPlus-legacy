package br.com.invplus.modules.produtos.dto.response;

import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados pela API para Produto.
 */
public record ProdutoResponse(
        UUID id,
        String codigo,
        String sku,
        String codigoInterno,
        String codigoBarras,
        String nome,
        String descricao,
        TipoProduto tipoProduto,
        NaturezaProduto naturezaProduto,
        StatusProduto statusProduto,
        UUID categoriaId,
        String categoriaNome,
        UUID marcaId,
        String marcaNome,
        UUID unidadeMedidaId,
        String unidadeMedidaSigla,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        BigDecimal pesoLiquido,
        BigDecimal pesoBruto,
        BigDecimal alturaCm,
        BigDecimal larguraCm,
        BigDecimal comprimentoCm,
        BigDecimal estoqueMinimo,
        Boolean controlaLote,
        Boolean controlaValidade,
        Boolean permiteFracionamento,
        Boolean controlaEstoque,
        Boolean permiteCompra,
        Boolean permiteVenda,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
