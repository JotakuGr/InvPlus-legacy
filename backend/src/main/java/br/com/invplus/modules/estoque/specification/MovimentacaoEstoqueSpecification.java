package br.com.invplus.modules.estoque.specification;

import br.com.invplus.modules.estoque.entity.MovimentacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para consultas do historico de movimentacoes.
 */
public final class MovimentacaoEstoqueSpecification {

    private MovimentacaoEstoqueSpecification() {
    }

    public static Specification<MovimentacaoEstoque> estoqueIgual(UUID estoqueId) {
        return estoqueId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("estoque").get("id"), estoqueId);
    }

    public static Specification<MovimentacaoEstoque> produtoIgual(UUID produtoId) {
        return produtoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produto").get("id"), produtoId);
    }

    public static Specification<MovimentacaoEstoque> empresaIgual(UUID empresaId) {
        return empresaId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<MovimentacaoEstoque> depositoIgual(UUID depositoId) {
        return depositoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("deposito").get("id"), depositoId);
    }

    public static Specification<MovimentacaoEstoque> localizacaoIgual(UUID localizacaoId) {
        return localizacaoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("localizacao").get("id"), localizacaoId);
    }

    public static Specification<MovimentacaoEstoque> tipoIgual(TipoMovimentacaoEstoque tipo) {
        return tipo == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("tipoMovimentacao"), tipo);
    }

    public static Specification<MovimentacaoEstoque> origemIgual(MovimentacaoOrigem origem) {
        return origem == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("origem"), origem);
    }

    public static Specification<MovimentacaoEstoque> periodoDe(OffsetDateTime dataInicial) {
        return dataInicial == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("dataMovimentacao"), dataInicial);
    }

    public static Specification<MovimentacaoEstoque> periodoAte(OffsetDateTime dataFinal) {
        return dataFinal == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.lessThanOrEqualTo(root.get("dataMovimentacao"), dataFinal);
    }

    private static Specification<MovimentacaoEstoque> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
