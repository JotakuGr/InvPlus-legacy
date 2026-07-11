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
        @NotNull UUID empresaId,
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
    /**
     * Mantem compatibilidade com chamadas internas anteriores ao CentroCusto.
     */
    public MovimentoFinanceiroRequest(UUID empresaId, UUID filialId, UUID formaPagamentoId,
            UUID clienteId, UUID fornecedorId, TipoMovimentoFinanceiro tipoMovimento,
            OrigemMovimentoFinanceiro origem, UUID origemId, BigDecimal valor,
            LocalDate dataMovimento, String descricao, String observacao) {
        this(empresaId, filialId, formaPagamentoId, clienteId, fornecedorId, null, null, null, tipoMovimento,
                origem, origemId, valor, dataMovimento, descricao, observacao);
    }

    /**
     * Mantem compatibilidade com chamadas internas anteriores ao PlanoConta.
     */
    public MovimentoFinanceiroRequest(UUID empresaId, UUID filialId, UUID formaPagamentoId,
            UUID clienteId, UUID fornecedorId, UUID centroCustoId, TipoMovimentoFinanceiro tipoMovimento,
            OrigemMovimentoFinanceiro origem, UUID origemId, BigDecimal valor,
            LocalDate dataMovimento, String descricao, String observacao) {
        this(empresaId, filialId, formaPagamentoId, clienteId, fornecedorId, centroCustoId, null, null, tipoMovimento,
                origem, origemId, valor, dataMovimento, descricao, observacao);
    }

    public MovimentoFinanceiroRequest(UUID empresaId, UUID filialId, UUID formaPagamentoId,
            UUID clienteId, UUID fornecedorId, UUID centroCustoId, UUID planoContaId,
            TipoMovimentoFinanceiro tipoMovimento, OrigemMovimentoFinanceiro origem, UUID origemId,
            BigDecimal valor, LocalDate dataMovimento, String descricao, String observacao) {
        this(empresaId, filialId, formaPagamentoId, clienteId, fornecedorId, centroCustoId,
                planoContaId, null, tipoMovimento, origem, origemId, valor, dataMovimento,
                descricao, observacao);
    }
}
