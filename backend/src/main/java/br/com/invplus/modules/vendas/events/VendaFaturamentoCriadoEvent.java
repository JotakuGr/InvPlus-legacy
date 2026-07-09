package br.com.invplus.modules.vendas.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando o documento de faturamento e gerado.
 */
public record VendaFaturamentoCriadoEvent(
        UUID faturamentoId,
        UUID pedidoVendaId,
        UUID empresaId,
        UUID clienteId,
        BigDecimal valorTotal
) {
}
