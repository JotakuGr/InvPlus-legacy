package br.com.invplus.modules.financeiro.enums;

/**
 * Estado de processamento e conciliacao de um extrato bancario importado.
 */
public enum StatusExtratoBancario {
    IMPORTADO,
    PROCESSANDO,
    CONCILIADO,
    CANCELADO
}
