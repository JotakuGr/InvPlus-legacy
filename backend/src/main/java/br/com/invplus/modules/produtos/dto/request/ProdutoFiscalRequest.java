package br.com.invplus.modules.produtos.dto.request;

import br.com.invplus.modules.produtos.enums.OrigemMercadoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dados de entrada para criar ou atualizar informacoes fiscais de Produto.
 */
public record ProdutoFiscalRequest(
        @NotNull UUID produtoId,
        @NotBlank String ncm,
        String cest,
        OrigemMercadoria origemMercadoria,
        String cfopPadrao,
        String cstIcms,
        String csosn,
        String cstPis,
        String cstCofins,
        @PositiveOrZero BigDecimal aliquotaIcms,
        @PositiveOrZero BigDecimal aliquotaIpi,
        @PositiveOrZero BigDecimal aliquotaPis,
        @PositiveOrZero BigDecimal aliquotaCofins,
        String observacao
) {
}
