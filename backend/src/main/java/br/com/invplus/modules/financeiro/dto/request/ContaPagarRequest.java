package br.com.invplus.modules.financeiro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload para criar ou atualizar uma conta a pagar.
 */
public record ContaPagarRequest(
        @NotNull(message = "Empresa e obrigatoria")
        UUID empresaId,
        UUID filialId,
        @NotNull(message = "Fornecedor e obrigatorio")
        UUID fornecedorId,
        UUID entradaMercadoriaId,
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
    public ContaPagarRequest(UUID empresaId, UUID filialId, UUID fornecedorId, UUID entradaMercadoriaId,
            String numero, String descricao, LocalDate dataEmissao, BigDecimal valorTotal, String observacao) {
        this(empresaId, filialId, fornecedorId, entradaMercadoriaId, null, null, numero, descricao,
                dataEmissao, valorTotal, observacao);
    }

    /**
     * Mantem compatibilidade com chamadas internas anteriores ao PlanoConta.
     */
    public ContaPagarRequest(UUID empresaId, UUID filialId, UUID fornecedorId, UUID entradaMercadoriaId,
            UUID centroCustoId, String numero, String descricao, LocalDate dataEmissao,
            BigDecimal valorTotal, String observacao) {
        this(empresaId, filialId, fornecedorId, entradaMercadoriaId, centroCustoId, null, numero, descricao,
                dataEmissao, valorTotal, observacao);
    }
}
