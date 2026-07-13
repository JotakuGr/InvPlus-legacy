package br.com.invplus.modules.financeiro.conciliacao.dto;

import br.com.invplus.modules.financeiro.enums.NivelConfiancaConciliacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MelhorSugestaoResponse(
        UUID movimentoFinanceiroId, String descricaoMovimento, LocalDate dataMovimento,
        BigDecimal valorMovimento, Integer pontuacao,
        NivelConfiancaConciliacao nivelConfianca, CriteriosSugestaoResponse criterios
) {}
