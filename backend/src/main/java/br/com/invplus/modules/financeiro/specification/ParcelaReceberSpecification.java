package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.enums.StatusParcelaReceber;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para listagem paginada de parcelas a receber.
 */
public final class ParcelaReceberSpecification {

    private ParcelaReceberSpecification() {
    }

    public static Specification<ParcelaReceber> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ParcelaReceber> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<ParcelaReceber> contaIgual(UUID contaReceberId) {
        return contaReceberId == null ? null : (root, query, cb) -> cb.equal(root.get("contaReceber").get("id"), contaReceberId);
    }

    public static Specification<ParcelaReceber> statusIgual(StatusParcelaReceber status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ParcelaReceber> vencimentoDe(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataVencimento"), data);
    }

    public static Specification<ParcelaReceber> vencimentoAte(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataVencimento"), data);
    }

    public static Specification<ParcelaReceber> pagamentoDe(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataPagamento"), data);
    }

    public static Specification<ParcelaReceber> pagamentoAte(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataPagamento"), data);
    }
}
