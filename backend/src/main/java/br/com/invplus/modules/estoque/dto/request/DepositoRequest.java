package br.com.invplus.modules.estoque.dto.request;

import br.com.invplus.modules.estoque.enums.TipoDeposito;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Dados de entrada para criar ou atualizar Deposito.
 */
public record DepositoRequest(
        @NotNull UUID empresaId,
        UUID filialId,
        @NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 120) String nome,
        String descricao,
        @NotNull TipoDeposito tipoDeposito,
        Boolean principal,
        Boolean permiteVenda,
        Boolean permiteCompra,
        Boolean permiteTransferencia,
        Boolean controlaEstoque,
        Boolean bloqueado
) {
}
