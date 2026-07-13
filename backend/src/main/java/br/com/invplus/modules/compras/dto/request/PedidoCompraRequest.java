package br.com.invplus.modules.compras.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados para criar ou atualizar um pedido de compra.
 */
public record PedidoCompraRequest(
        UUID filialId,
        @NotNull UUID fornecedorId,
        @NotBlank String numero,
        @NotNull LocalDate dataEmissao,
        LocalDate dataPrevistaEntrega,
        String observacao
) {
}
