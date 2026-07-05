package br.com.invplus.modules.produtos.dto.response;

import br.com.invplus.modules.produtos.enums.TipoCodigoBarras;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados de um codigo de barras vinculado ao Produto.
 */
public record ProdutoCodigoBarrasResponse(
        UUID id,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        TipoCodigoBarras tipoCodigo,
        String codigo,
        Boolean principal,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
