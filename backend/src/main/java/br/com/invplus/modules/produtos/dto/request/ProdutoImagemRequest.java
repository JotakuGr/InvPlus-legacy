package br.com.invplus.modules.produtos.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

/**
 * Dados de entrada para vincular Produto e Arquivo como imagem.
 */
public record ProdutoImagemRequest(
        @NotNull UUID produtoId,
        @NotNull UUID arquivoId,
        Boolean principal,
        @PositiveOrZero Integer ordem,
        String descricao
) {
}
