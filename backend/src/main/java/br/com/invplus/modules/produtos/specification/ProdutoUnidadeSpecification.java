package br.com.invplus.modules.produtos.specification;

import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros de ProdutoUnidade.
 */
public final class ProdutoUnidadeSpecification {

    private ProdutoUnidadeSpecification() {
    }

    public static Specification<ProdutoUnidade> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<ProdutoUnidade> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<ProdutoUnidade> produtoIgual(UUID produtoId) {
        return produtoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produto").get("id"), produtoId);
    }

    public static Specification<ProdutoUnidade> unidadeIgual(UUID unidadeMedidaId) {
        return unidadeMedidaId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("unidadeMedida").get("id"), unidadeMedidaId);
    }

    public static Specification<ProdutoUnidade> unidadePadraoIgual(Boolean unidadePadrao) {
        return unidadePadrao == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("unidadePadrao"), unidadePadrao);
    }

    public static Specification<ProdutoUnidade> permiteCompraIgual(Boolean permiteCompra) {
        return permiteCompra == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("permiteCompra"), permiteCompra);
    }

    public static Specification<ProdutoUnidade> permiteVendaIgual(Boolean permiteVenda) {
        return permiteVenda == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("permiteVenda"), permiteVenda);
    }

    public static Specification<ProdutoUnidade> permiteEstoqueIgual(Boolean permiteEstoque) {
        return permiteEstoque == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("permiteEstoque"), permiteEstoque);
    }

    public static Specification<ProdutoUnidade> permiteFracionamentoIgual(Boolean permiteFracionamento) {
        return permiteFracionamento == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("permiteFracionamento"), permiteFracionamento);
    }

    private static Specification<ProdutoUnidade> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
