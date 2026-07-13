package br.com.invplus.modules.financeiro.dto.request;

import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados para registrar um movimento financeiro manual.
 */
public record MovimentoFinanceiroRequest(
        UUID filialId,
        @NotNull UUID formaPagamentoId,
        UUID clienteId,
        UUID fornecedorId,
        UUID centroCustoId,
        UUID planoContaId,
        UUID contaBancariaId,
        @NotNull TipoMovimentoFinanceiro tipoMovimento,
        @NotNull OrigemMovimentoFinanceiro origem,
        UUID origemId,
        @NotNull @Positive BigDecimal valor,
        @NotNull LocalDate dataMovimento,
        @NotBlank String descricao,
        String observacao
) {
}
