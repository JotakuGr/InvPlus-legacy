package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.Recebimento;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consultas paginadas de recebimentos.
 */
public final class RecebimentoSpecification {

    private RecebimentoSpecification() {
    }

    public static Specification<Recebimento> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Recebimento> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<Recebimento> parcelaIgual(UUID parcelaReceberId) {
        return parcelaReceberId == null ? null : (root, query, cb) -> cb.equal(root.get("parcelaReceber").get("id"), parcelaReceberId);
    }

    public static Specification<Recebimento> contaIgual(UUID contaReceberId) {
        return contaReceberId == null ? null : (root, query, cb) -> cb.equal(root.get("contaReceber").get("id"), contaReceberId);
    }

    public static Specification<Recebimento> formaPagamentoIgual(UUID formaPagamentoId) {
        return formaPagamentoId == null ? null : (root, query, cb) -> cb.equal(root.get("formaPagamento").get("id"), formaPagamentoId);
    }

    public static Specification<Recebimento> empresaIgual(UUID empresaId) {
        return empresaId == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<Recebimento> filialIgual(UUID filialId) {
        return filialId == null ? null : (root, query, cb) -> cb.equal(root.get("filial").get("id"), filialId);
    }

    public static Specification<Recebimento> clienteIgual(UUID clienteId) {
        return clienteId == null ? null : (root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId);
    }

    public static Specification<Recebimento> recebimentoDe(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataRecebimento"), data);
    }

    public static Specification<Recebimento> recebimentoAte(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataRecebimento"), data);
    }
}
