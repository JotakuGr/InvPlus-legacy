package br.com.invplus.modules.produtos.dto.response;

import br.com.invplus.modules.produtos.enums.OrigemMercadoria;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados pela API de ProdutoFiscal.
 */
public record ProdutoFiscalResponse(
        UUID id,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        String ncm,
        String cest,
        OrigemMercadoria origemMercadoria,
        String cfopPadrao,
        String cstIcms,
        String csosn,
        String cstPis,
        String cstCofins,
        BigDecimal aliquotaIcms,
        BigDecimal aliquotaIpi,
        BigDecimal aliquotaPis,
        BigDecimal aliquotaCofins,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
