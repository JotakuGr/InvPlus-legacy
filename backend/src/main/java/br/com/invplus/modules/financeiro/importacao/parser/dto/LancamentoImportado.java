package br.com.invplus.modules.financeiro.importacao.parser.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Linha temporaria lida pelo parser antes de entrar no dominio bancario. */
public record LancamentoImportado(
        LocalDate dataLancamento,
        String identificadorBancario,
        String tipoOriginal,
        String descricao,
        String documento,
        BigDecimal valor,
        BigDecimal saldoAposLancamento
) {
}
