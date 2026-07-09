package br.com.invplus.modules.vendas.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando o pedido passa para FATURADO.
 */
public record PedidoVendaFaturadoEvent(
        UUID pedidoVendaId,
        UUID faturamentoId,
        UUID empresaId,
        UUID clienteId,
        BigDecimal valorTotal
) {
}
