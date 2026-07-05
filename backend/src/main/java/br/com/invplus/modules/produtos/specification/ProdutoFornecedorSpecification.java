package br.com.invplus.modules.produtos.specification;

import br.com.invplus.modules.produtos.entity.ProdutoFornecedor;
import br.com.invplus.modules.produtos.enums.FornecedorProdutoStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros dinamicos do vinculo ProdutoFornecedor.
 */
public final class ProdutoFornecedorSpecification {

    private ProdutoFornecedorSpecification() {
    }

    public static Specification<ProdutoFornecedor> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<ProdutoFornecedor> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<ProdutoFornecedor> produtoIgual(UUID produtoId) {
        return produtoId == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produto").get("id"), produtoId);
    }

    public static Specification<ProdutoFornecedor> fornecedorIgual(UUID fornecedorId) {
        return fornecedorId == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("fornecedor").get("id"), fornecedorId);
    }

    public static Specification<ProdutoFornecedor> principalIgual(Boolean principal) {
        return principal == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("principal"), principal);
    }

    public static Specification<ProdutoFornecedor> codigoFornecedorContem(String codigoFornecedor) {
        return codigoFornecedor == null || codigoFornecedor.isBlank()
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(
                        builder.lower(root.get("codigoFornecedor")),
                        "%" + codigoFornecedor.trim().toLowerCase() + "%"
                );
    }

    public static Specification<ProdutoFornecedor> statusIgual(FornecedorProdutoStatus status) {
        return status == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    private static Specification<ProdutoFornecedor> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
