package br.com.invplus.modules.financeiro.conciliacao.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConciliacaoBancariaLoteItemRequest(
        @NotNull UUID lancamentoBancarioId,
        @NotNull UUID movimentoFinanceiroId,
        String observacao
) {}
