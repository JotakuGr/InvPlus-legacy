package br.com.invplus.modules.estoque.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resposta enxuta para consultas de saldo disponivel usadas por PDV e Vendas.
 */
public record EstoqueDisponivelResponse(
        UUID estoqueId,
        BigDecimal saldo,
        BigDecimal reservado,
        BigDecimal disponivel
) {
}
