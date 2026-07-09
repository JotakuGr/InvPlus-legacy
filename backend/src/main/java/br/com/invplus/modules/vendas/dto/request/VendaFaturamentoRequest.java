package br.com.invplus.modules.vendas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Dados minimos para faturar um pedido de venda aprovado.
 */
public record VendaFaturamentoRequest(
        @NotNull(message = "Pedido de venda e obrigatorio")
        UUID pedidoVendaId,

        @NotNull(message = "Condicao de pagamento e obrigatoria")
        UUID condicaoPagamentoId,

        @NotBlank(message = "Numero e obrigatorio")
        String numero,

        String descricaoFinanceira,

        String observacao
) {
}
