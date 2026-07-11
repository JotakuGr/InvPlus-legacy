package br.com.invplus.modules.financeiro.conciliacao.dto;

import java.util.List;

public record ConciliacaoBancariaLoteResponse(
        int quantidadeRecebida, int quantidadeConciliada, int quantidadeRejeitada,
        List<ConciliacaoBancariaLoteItemResponse> resultados
) {}
