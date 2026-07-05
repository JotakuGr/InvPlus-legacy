package br.com.invplus.modules.produtos.dto.response;

import br.com.invplus.modules.produtos.enums.FornecedorProdutoStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Dados retornados do vinculo ProdutoFornecedor com resumo de Produto e Fornecedor.
 */
public record ProdutoFornecedorResponse(
        UUID id,
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        UUID fornecedorId,
        String fornecedorNome,
        String fornecedorCpfCnpj,
        String codigoFornecedor,
        Integer leadTimeDias,
        Boolean principal,
        Integer prioridade,
        FornecedorProdutoStatus status,
        String observacao,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
