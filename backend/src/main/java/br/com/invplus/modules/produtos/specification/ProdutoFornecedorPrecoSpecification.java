package br.com.invplus.modules.produtos.specification;

import br.com.invplus.modules.produtos.entity.ProdutoFornecedorPreco;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros do historico de custos de ProdutoFornecedor.
 */
public final class ProdutoFornecedorPrecoSpecification {

    private ProdutoFornecedorPrecoSpecification() {
    }

    public static Specification<ProdutoFornecedorPreco> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<ProdutoFornecedorPreco> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<ProdutoFornecedorPreco> produtoFornecedorIgual(UUID produtoFornecedorId) {
        return produtoFornecedorId == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produtoFornecedor").get("id"), produtoFornecedorId);
    }

    public static Specification<ProdutoFornecedorPreco> vigenciaInicioMaiorOuIgual(LocalDate data) {
        return data == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("vigenciaInicio"), data);
    }

    public static Specification<ProdutoFornecedorPreco> vigenciaFimMenorOuIgual(LocalDate data) {
        return data == null
                ? sempreVerdadeiro()
                : (root, query, builder) -> builder.lessThanOrEqualTo(root.get("vigenciaFim"), data);
    }

    private static Specification<ProdutoFornecedorPreco> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
