package br.com.invplus.modules.produtos.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados da unidade alternativa do produto.
 */
public record ProdutoUnidadeResponse(
        UUID id,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        UUID unidadeMedidaId,
        String unidadeMedidaSigla,
        String unidadeMedidaDescricao,
        BigDecimal fatorConversao,
        Boolean unidadePadrao,
        Boolean permiteCompra,
        Boolean permiteVenda,
        Boolean permiteEstoque,
        Boolean permiteFracionamento,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
