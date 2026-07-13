package br.com.invplus.modules.financeiro.conciliacao.projection;

public interface IndicadoresExtratoProjection {
    long getQuantidadeExtratos();
    long getQuantidadeExtratosImportados();
    long getQuantidadeExtratosProcessando();
    long getQuantidadeExtratosConciliados();
}
