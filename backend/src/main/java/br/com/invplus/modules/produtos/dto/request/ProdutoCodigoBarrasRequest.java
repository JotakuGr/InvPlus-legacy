package br.com.invplus.modules.produtos.dto.request;

import br.com.invplus.modules.produtos.enums.TipoCodigoBarras;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Dados de entrada para cadastrar codigo de barras ou codigo alternativo.
 */
public record ProdutoCodigoBarrasRequest(
        @NotNull UUID produtoId,
        @NotNull TipoCodigoBarras tipoCodigo,
        @NotBlank @Size(max = 120) String codigo,
        Boolean principal,
        String observacao
) {
}
