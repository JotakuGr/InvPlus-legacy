package br.com.invplus.modules.financeiro.enums;

/**
 * Origem operacional usada para identificar como o extrato entrou no ERP.
 */
public enum OrigemExtratoBancario {
    MANUAL,
    OFX,
    CSV,
    CNAB,
    API_BANCARIA,
    OPEN_FINANCE
}
