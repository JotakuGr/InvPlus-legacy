package br.com.invplus.modules.financeiro.conciliacao.dto;

import br.com.invplus.modules.financeiro.enums.NivelConfiancaConciliacao;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SugestaoConciliacaoResponse(
        UUID movimentoFinanceiroId,
        String movimentoDescricao,
        LocalDate dataMovimento,
        TipoMovimentoFinanceiro tipoMovimento,
        BigDecimal valorMovimento,
        UUID contaBancariaId,
        String contaBancariaDescricao,
        Integer pontuacao,
        NivelConfiancaConciliacao nivelConfianca,
        CriteriosSugestaoResponse criterios
) {
}
