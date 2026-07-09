package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload para criar ou atualizar uma conta a receber.
 */
public record ContaReceberRequest(
        @NotNull(message = "Empresa e obrigatoria")
        UUID empresaId,
        UUID filialId,
        @NotNull(message = "Cliente e obrigatorio")
        UUID clienteId,
        UUID vendaFaturamentoId,
        UUID centroCustoId,
        UUID planoContaId,
        @NotBlank(message = "Numero e obrigatorio")
        String numero,
        @NotBlank(message = "Descricao e obrigatoria")
        String descricao,
        @NotNull(message = "Data de emissao e obrigatoria")
        LocalDate dataEmissao,
        @NotNull(message = "Valor total e obrigatorio")
        @Positive(message = "Valor total deve ser maior que zero")
        BigDecimal valorTotal,
        String observacao
) {
    /**
     * Mantem compatibilidade com chamadas internas anteriores ao CentroCusto.
     */
    public ContaReceberRequest(UUID empresaId, UUID filialId, UUID clienteId, UUID vendaFaturamentoId,
            String numero, String descricao, LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        this(empresaId, filialId, clienteId, vendaFaturamentoId, null, null, numero, descricao,
                dataEmissao, valorTotal, observacao);
    }

    /**
     * Mantem compatibilidade com chamadas internas anteriores ao PlanoConta.
     */
    public ContaReceberRequest(UUID empresaId, UUID filialId, UUID clienteId, UUID vendaFaturamentoId,
            UUID centroCustoId, String numero, String descricao, LocalDate dataEmissao,
            BigDecimal valorTotal, String observacao) {
        this(empresaId, filialId, clienteId, vendaFaturamentoId, centroCustoId, null, numero, descricao,
                dataEmissao, valorTotal, observacao);
    }
}
