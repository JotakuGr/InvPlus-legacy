package br.com.invplus.modules.vendas.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Item de venda com produto, unidade, preco utilizado e totais calculados.
 */
public record PedidoVendaItemResponse(
        UUID id,
        UUID pedidoVendaId,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        String descricao,
        UUID produtoPrecoId,
        BigDecimal precoUtilizado,
        UUID produtoUnidadeId,
        String unidadeSigla,
        String unidadeDescricao,
        BigDecimal quantidade,
        BigDecimal subtotal,
        BigDecimal desconto,
        BigDecimal acrescimo,
        BigDecimal valorTotal,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
