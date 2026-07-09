package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.enums.StatusParcelaPagar;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para parcelas a pagar.
 */
public final class ParcelaPagarSpecification {

    private ParcelaPagarSpecification() {
    }

    public static Specification<ParcelaPagar> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ParcelaPagar> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<ParcelaPagar> contaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("contaPagar").get("id"), id);
    }

    public static Specification<ParcelaPagar> statusIgual(StatusParcelaPagar status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ParcelaPagar> vencimentoDe(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataVencimento"), data);
    }

    public static Specification<ParcelaPagar> vencimentoAte(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataVencimento"), data);
    }

    public static Specification<ParcelaPagar> pagamentoDe(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataPagamento"), data);
    }

    public static Specification<ParcelaPagar> pagamentoAte(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataPagamento"), data);
    }
}
