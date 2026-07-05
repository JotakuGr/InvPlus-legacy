package br.com.invplus.modules.produtos.specification;

import br.com.invplus.modules.produtos.entity.ProdutoCodigoBarras;
import br.com.invplus.modules.produtos.enums.TipoCodigoBarras;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros dinamicos de ProdutoCodigoBarras.
 */
public final class ProdutoCodigoBarrasSpecification {

    private ProdutoCodigoBarrasSpecification() {
    }

    public static Specification<ProdutoCodigoBarras> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<ProdutoCodigoBarras> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<ProdutoCodigoBarras> produtoIgual(UUID produtoId) {
        return produtoId == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produto").get("id"), produtoId);
    }

    public static Specification<ProdutoCodigoBarras> tipoIgual(TipoCodigoBarras tipoCodigo) {
        return tipoCodigo == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("tipoCodigo"), tipoCodigo);
    }

    public static Specification<ProdutoCodigoBarras> codigoContem(String codigo) {
        return codigo == null || codigo.isBlank()
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(
                        builder.lower(root.get("codigo")),
                        "%" + codigo.trim().toLowerCase() + "%"
                );
    }

    public static Specification<ProdutoCodigoBarras> principalIgual(Boolean principal) {
        return principal == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("principal"), principal);
    }

    private static Specification<ProdutoCodigoBarras> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
