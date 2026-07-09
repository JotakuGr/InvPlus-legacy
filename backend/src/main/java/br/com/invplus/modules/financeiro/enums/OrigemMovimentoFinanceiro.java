package br.com.invplus.modules.financeiro.enums;

/**
 * Identifica o processo de negocio que originou o movimento financeiro.
 */
public enum OrigemMovimentoFinanceiro {
    RECEBIMENTO,
    PAGAMENTO,
    AJUSTE_MANUAL,
    TRANSFERENCIA,
    ESTORNO
}
