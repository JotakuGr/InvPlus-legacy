package br.com.invplus.modules.produtos.dto.response;

import java.util.List;

/**
 * Visao agregada do produto para telas que precisam do cadastro completo em uma chamada.
 */
public record ProdutoCompletoResponse(
        ProdutoResponse produto,
        ProdutoFornecedorResponse fornecedorPrincipal,
        List<ProdutoFornecedorResponse> fornecedores,
        ProdutoImagemResponse imagemPrincipal,
        List<ProdutoImagemResponse> imagens,
        ProdutoCodigoBarrasResponse codigoPrincipal,
        List<ProdutoCodigoBarrasResponse> codigosBarras,
        ProdutoFiscalResponse fiscal,
        ProdutoPrecoResponse precoVenda,
        ProdutoPrecoResponse precoAtacado,
        ProdutoPrecoResponse precoPromocional,
        List<ProdutoPrecoResponse> precos,
        ProdutoUnidadeResponse unidadePadrao,
        List<ProdutoUnidadeResponse> outrasUnidades
) {
}
