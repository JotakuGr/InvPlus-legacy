package br.com.invplus.modules.financeiro.importacao.parser.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Modelo intermediario independente de OFX, CSV ou API bancaria. */
public record ExtratoImportado(
        String bancoCodigo,
        String agencia,
        String numeroConta,
        String moeda,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal saldoInicial,
        BigDecimal saldoFinal,
        List<LancamentoImportado> lancamentos
) {
}
