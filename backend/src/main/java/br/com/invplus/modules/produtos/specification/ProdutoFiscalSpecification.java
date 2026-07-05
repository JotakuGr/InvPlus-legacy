package br.com.invplus.modules.produtos.specification;

import br.com.invplus.modules.produtos.entity.ProdutoFiscal;
import br.com.invplus.modules.produtos.enums.OrigemMercadoria;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para consulta paginada dos dados fiscais de Produto.
 */
public final class ProdutoFiscalSpecification {

    private ProdutoFiscalSpecification() {
    }

    public static Specification<ProdutoFiscal> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<ProdutoFiscal> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<ProdutoFiscal> produtoIgual(UUID produtoId) {
        return produtoId == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produto").get("id"), produtoId);
    }

    public static Specification<ProdutoFiscal> ncmContem(String ncm) {
        String filtro = normalizar(ncm);
        return filtro == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(root.get("ncm"), "%" + filtro + "%");
    }

    public static Specification<ProdutoFiscal> cestContem(String cest) {
        String filtro = normalizar(cest);
        return filtro == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(root.get("cest"), "%" + filtro + "%");
    }

    public static Specification<ProdutoFiscal> origemIgual(OrigemMercadoria origemMercadoria) {
        return origemMercadoria == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("origemMercadoria"), origemMercadoria);
    }

    private static Specification<ProdutoFiscal> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.replaceAll("\\D", "");
    }
}
