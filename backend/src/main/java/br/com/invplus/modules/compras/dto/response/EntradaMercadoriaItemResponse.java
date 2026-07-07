package br.com.invplus.modules.compras.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Item recebido com produto e localizacao de estoque.
 */
public record EntradaMercadoriaItemResponse(
        UUID id,
        UUID entradaMercadoriaId,
        UUID pedidoCompraItemId,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        UUID produtoUnidadeId,
        String unidadeSigla,
        UUID depositoId,
        String depositoNome,
        UUID localizacaoId,
        String localizacaoCodigo,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
