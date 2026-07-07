package br.com.invplus.modules.estoque.enums;

/**
 * Tipos de alteracao historica do saldo de estoque.
 */
public enum TipoMovimentacaoEstoque {
    ENTRADA,
    SAIDA,
    AJUSTE_ENTRADA,
    AJUSTE_SAIDA,
    TRANSFERENCIA_ENTRADA,
    TRANSFERENCIA_SAIDA,
    RESERVA,
    CANCELAMENTO_RESERVA,
    BAIXA_RESERVA,
    DEVOLUCAO,
    AVARIA
}
