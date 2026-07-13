package br.com.invplus.modules.produtos.specification;

import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros de ProdutoPreco.
 */
public final class ProdutoPrecoSpecification {

    private ProdutoPrecoSpecification() {
    }

    public static Specification<ProdutoPreco> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<ProdutoPreco> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<ProdutoPreco> produtoIgual(UUID produtoId) {
        return produtoId == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produto").get("id"), produtoId);
    }

    public static Specification<ProdutoPreco> pertenceEmpresa(UUID empresaId) {
        return (root, query, builder) -> builder.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<ProdutoPreco> tipoIgual(TipoPrecoProduto tipoPreco) {
        return tipoPreco == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("tipoPreco"), tipoPreco);
    }

    public static Specification<ProdutoPreco> principalIgual(Boolean principal) {
        return principal == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("principal"), principal);
    }

    public static Specification<ProdutoPreco> vigenciaInicioMaiorOuIgual(LocalDate data) {
        return data == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("vigenciaInicio"), data);
    }

    public static Specification<ProdutoPreco> vigenciaFimMenorOuIgual(LocalDate data) {
        return data == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.lessThanOrEqualTo(root.get("vigenciaFim"), data);
    }

    private static Specification<ProdutoPreco> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
