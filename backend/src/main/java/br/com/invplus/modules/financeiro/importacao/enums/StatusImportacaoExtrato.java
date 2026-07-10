package br.com.invplus.modules.financeiro.importacao.enums;

/** Estado auditavel do processamento de um arquivo bancario. */
public enum StatusImportacaoExtrato {
    RECEBIDA,
    PROCESSANDO,
    CONCLUIDA,
    CONCLUIDA_COM_ALERTAS,
    FALHOU
}
