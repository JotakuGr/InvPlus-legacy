package br.com.invplus.modules.compras.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados para criar ou atualizar uma entrada de mercadoria.
 */
public record EntradaMercadoriaRequest(
        @NotNull UUID empresaId,
        UUID filialId,
        @NotNull UUID fornecedorId,
        UUID pedidoCompraId,
        @NotBlank String numero,
        @NotNull LocalDate dataEntrada,
        String observacao
) {
}
