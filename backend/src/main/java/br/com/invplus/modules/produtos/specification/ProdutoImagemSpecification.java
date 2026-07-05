package br.com.invplus.modules.produtos.specification;

import br.com.invplus.modules.produtos.entity.ProdutoImagem;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros dinamicos de ProdutoImagem.
 */
public final class ProdutoImagemSpecification {

    private ProdutoImagemSpecification() {
    }

    public static Specification<ProdutoImagem> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<ProdutoImagem> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<ProdutoImagem> produtoIgual(UUID produtoId) {
        return produtoId == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produto").get("id"), produtoId);
    }

    public static Specification<ProdutoImagem> arquivoIgual(UUID arquivoId) {
        return arquivoId == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("arquivo").get("id"), arquivoId);
    }

    public static Specification<ProdutoImagem> principalIgual(Boolean principal) {
        return principal == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("principal"), principal);
    }

    private static Specification<ProdutoImagem> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
