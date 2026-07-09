package br.com.invplus.modules.financeiro.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando uma venda faturada gera automaticamente seu titulo financeiro.
 */
public record ContaReceberGeradaPorVendaEvent(
        UUID vendaFaturamentoId,
        UUID contaReceberId,
        UUID pedidoVendaId,
        UUID clienteId,
        BigDecimal valorTotal
) {
}
