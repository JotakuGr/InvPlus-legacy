package br.com.invplus.modules.produtos.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados do historico de custos de ProdutoFornecedor.
 */
public record ProdutoFornecedorPrecoResponse(
        UUID id,
        UUID produtoFornecedorId,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        UUID fornecedorId,
        String fornecedorNome,
        BigDecimal preco,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFim,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
