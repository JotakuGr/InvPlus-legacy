package br.com.invplus.modules.produtos.dto.response;

import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados do preco comercial do produto.
 */
public record ProdutoPrecoResponse(
        UUID id,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        UUID empresaId,
        String empresaRazaoSocial,
        TipoPrecoProduto tipoPreco,
        BigDecimal preco,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFim,
        Boolean principal,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
