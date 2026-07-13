package br.com.invplus.modules.financeiro.conciliacao.dto;

import java.math.BigDecimal;

public record IndicadoresConciliacaoResponse(
        long quantidadeExtratos,
        long quantidadeExtratosImportados,
        long quantidadeExtratosProcessando,
        long quantidadeExtratosConciliados,
        long quantidadeLancamentos,
        long quantidadeLancamentosPendentes,
        long quantidadeLancamentosConciliados,
        BigDecimal valorTotalCreditosPendentes,
        BigDecimal valorTotalDebitosPendentes,
        BigDecimal percentualConciliado
) {}
