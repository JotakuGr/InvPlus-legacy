package br.com.invplus.modules.financeiro.enums;

/**
 * Define como datas de vencimento devem ser ajustadas quando caem em dias nao uteis.
 */
public enum RegraVencimento {
    MANTER_DATA,
    PROXIMO_DIA_UTIL,
    DIA_UTIL_ANTERIOR
}
