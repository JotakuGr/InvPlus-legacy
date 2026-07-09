package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.modules.financeiro.enums.StatusPagamentoFinanceiro;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consultas paginadas de pagamentos.
 */
public final class PagamentoSpecification {

    private PagamentoSpecification() {
    }

    public static Specification<Pagamento> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Pagamento> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<Pagamento> parcelaIgual(UUID parcelaPagarId) {
        return parcelaPagarId == null ? null : (root, query, cb) -> cb.equal(root.get("parcelaPagar").get("id"), parcelaPagarId);
    }

    public static Specification<Pagamento> contaIgual(UUID contaPagarId) {
        return contaPagarId == null ? null : (root, query, cb) -> cb.equal(root.get("contaPagar").get("id"), contaPagarId);
    }

    public static Specification<Pagamento> formaPagamentoIgual(UUID formaPagamentoId) {
        return formaPagamentoId == null ? null : (root, query, cb) -> cb.equal(root.get("formaPagamento").get("id"), formaPagamentoId);
    }

    public static Specification<Pagamento> empresaIgual(UUID empresaId) {
        return empresaId == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<Pagamento> filialIgual(UUID filialId) {
        return filialId == null ? null : (root, query, cb) -> cb.equal(root.get("filial").get("id"), filialId);
    }

    public static Specification<Pagamento> fornecedorIgual(UUID fornecedorId) {
        return fornecedorId == null ? null : (root, query, cb) -> cb.equal(root.get("fornecedor").get("id"), fornecedorId);
    }

    public static Specification<Pagamento> statusIgual(StatusPagamentoFinanceiro status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Pagamento> pagamentoDe(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataPagamento"), data);
    }

    public static Specification<Pagamento> pagamentoAte(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataPagamento"), data);
    }
}
