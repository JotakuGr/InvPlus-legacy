package br.com.invplus.modules.produtos.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados do vinculo ProdutoImagem com resumo de Produto e Arquivo.
 */
public record ProdutoImagemResponse(
        UUID id,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        UUID arquivoId,
        String arquivoNomeOriginal,
        String arquivoUrl,
        String arquivoTipoMime,
        Boolean principal,
        Integer ordem,
        String descricao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
