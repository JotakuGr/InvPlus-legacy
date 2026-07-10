package br.com.invplus.modules.financeiro.dto.response;

import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Visao auditavel de uma conciliacao bancaria. */
public record ConciliacaoBancariaResponse(
        UUID id,
        UUID extratoBancarioLancamentoId,
        String extratoDescricao,
        UUID movimentoFinanceiroId,
        String movimentoDescricao,
        UUID contaBancariaId,
        String contaBancariaDescricao,
        TipoConciliacaoBancaria tipoConciliacao,
        StatusConciliacaoBancaria status,
        BigDecimal valorExtrato,
        BigDecimal valorMovimento,
        BigDecimal diferenca,
        OffsetDateTime conciliadoEm,
        UUID conciliadoPor,
        OffsetDateTime canceladoEm,
        UUID canceladoPor,
        String motivoCancelamento,
        String observacao,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
