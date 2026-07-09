package br.com.invplus.modules.pagamentos.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dados de uma parcela configurada para retorno na API.
 */
public record CondicaoPagamentoParcelaResponse(
        UUID id,
        Integer numeroParcela,
        Integer dias,
        BigDecimal percentual,
        Boolean ativo
) {
}
