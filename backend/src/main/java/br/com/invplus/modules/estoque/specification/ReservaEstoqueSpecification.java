package br.com.invplus.modules.estoque.specification;

import br.com.invplus.modules.estoque.entity.ReservaEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para consultas paginadas de reservas de estoque.
 */
public final class ReservaEstoqueSpecification {

    private ReservaEstoqueSpecification() {
    }

    public static Specification<ReservaEstoque> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<ReservaEstoque> ativo(Boolean ativo) {
        return ativo == null ? sempreVerdadeiro() : (root, query, builder) -> builder.equal(root.get("ativo"), ativo);
    }

    public static Specification<ReservaEstoque> estoqueIgual(UUID estoqueId) {
        return estoqueId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("estoque").get("id"), estoqueId);
    }

    public static Specification<ReservaEstoque> produtoIgual(UUID produtoId) {
        return produtoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("produto").get("id"), produtoId);
    }

    public static Specification<ReservaEstoque> pertenceEmpresa(UUID empresaId) {
        return (root, query, builder) -> builder.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<ReservaEstoque> filialIgual(UUID filialId) {
        return filialId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("filial").get("id"), filialId);
    }

    public static Specification<ReservaEstoque> depositoIgual(UUID depositoId) {
        return depositoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("deposito").get("id"), depositoId);
    }

    public static Specification<ReservaEstoque> localizacaoIgual(UUID localizacaoId) {
        return localizacaoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("localizacao").get("id"), localizacaoId);
    }

    public static Specification<ReservaEstoque> statusIgual(StatusReservaEstoque status) {
        return status == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<ReservaEstoque> origemIgual(MovimentacaoOrigem origem) {
        return origem == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("origem"), origem);
    }

    public static Specification<ReservaEstoque> origemIdIgual(UUID origemId) {
        return origemId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("origemId"), origemId);
    }

    private static Specification<ReservaEstoque> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
