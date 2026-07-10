package br.com.invplus.modules.financeiro.dto.request;

import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Dados informados para vincular um lancamento bancario a um movimento. */
public record ConciliacaoBancariaRequest(
        @NotNull UUID extratoBancarioLancamentoId,
        @NotNull UUID movimentoFinanceiroId,
        @NotNull TipoConciliacaoBancaria tipoConciliacao,
        String observacao
) {
}
