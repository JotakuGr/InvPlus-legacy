package br.com.invplus.modules.financeiro.conciliacao.projection;

import java.math.BigDecimal;

public interface IndicadoresLancamentoProjection {
    long getQuantidadeLancamentos();
    long getQuantidadeLancamentosPendentes();
    long getQuantidadeLancamentosConciliados();
    BigDecimal getValorTotalCreditosPendentes();
    BigDecimal getValorTotalDebitosPendentes();
}
