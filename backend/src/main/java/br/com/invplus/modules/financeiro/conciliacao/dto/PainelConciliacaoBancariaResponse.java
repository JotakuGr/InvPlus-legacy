package br.com.invplus.modules.financeiro.conciliacao.dto;

import java.util.List;

public record PainelConciliacaoBancariaResponse(
        IndicadoresConciliacaoResponse indicadores,
        List<ExtratoPendenteResponse> extratosRecentes
) {}
