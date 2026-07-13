package br.com.invplus.modules.estoque.specification;

import br.com.invplus.modules.estoque.entity.Estoque;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros operacionais de saldo de estoque.
 */
public final class EstoqueSpecification {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private EstoqueSpecification() {
    }

    public static Specification<Estoque> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<Estoque> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<Estoque> produtoIgual(UUID produtoId) {
        return produtoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produto").get("id"), produtoId);
    }

    public static Specification<Estoque> pertenceEmpresa(UUID empresaId) {
        return (root, query, builder) -> builder.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<Estoque> filialIgual(UUID filialId) {
        return filialId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("filial").get("id"), filialId);
    }

    public static Specification<Estoque> depositoIgual(UUID depositoId) {
        return depositoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("deposito").get("id"), depositoId);
    }

    public static Specification<Estoque> localizacaoIgual(UUID localizacaoId) {
        return localizacaoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("localizacao").get("id"), localizacaoId);
    }

    public static Specification<Estoque> estoqueBaixo(Boolean estoqueBaixo) {
        return !Boolean.TRUE.equals(estoqueBaixo) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.lessThanOrEqualTo(root.get("quantidadeDisponivel"), root.get("estoqueMinimo"));
    }

    public static Specification<Estoque> estoqueZerado(Boolean estoqueZerado) {
        return !Boolean.TRUE.equals(estoqueZerado) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("quantidadeDisponivel"), ZERO);
    }

    public static Specification<Estoque> comSaldo(Boolean comSaldo) {
        return !Boolean.TRUE.equals(comSaldo) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.greaterThan(root.get("quantidadeDisponivel"), ZERO);
    }

    public static Specification<Estoque> semSaldo(Boolean semSaldo) {
        return !Boolean.TRUE.equals(semSaldo) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.lessThanOrEqualTo(root.get("quantidadeDisponivel"), ZERO);
    }

    private static Specification<Estoque> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
