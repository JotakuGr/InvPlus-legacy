package br.com.invplus.modules.financeiro.conciliacao.dto;

import br.com.invplus.modules.financeiro.enums.StatusResultadoConciliacaoLote;
import java.util.UUID;

public record ConciliacaoBancariaLoteItemResponse(
        UUID lancamentoBancarioId, UUID movimentoFinanceiroId, UUID conciliacaoBancariaId,
        StatusResultadoConciliacaoLote status, String codigoErro, String mensagem
) {}
