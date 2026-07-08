package br.com.invplus.modules.vendas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados de cabecalho para criar ou atualizar um pedido de venda.
 */
public record PedidoVendaRequest(
        @NotNull UUID empresaId,
        UUID filialId,
        @NotNull UUID clienteId,
        UUID vendedorId,
        @NotBlank String numero,
        @NotNull LocalDate dataEmissao,
        LocalDate dataEntregaPrevista,
        @PositiveOrZero BigDecimal desconto,
        @PositiveOrZero BigDecimal acrescimo,
        String observacao
) {
}
