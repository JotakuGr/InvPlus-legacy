package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consultas paginadas de contas a receber.
 */
public final class ContaReceberSpecification {

    private ContaReceberSpecification() {
    }

    public static Specification<ContaReceber> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ContaReceber> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<ContaReceber> empresaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), id);
    }

    public static Specification<ContaReceber> filialIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("filial").get("id"), id);
    }

    public static Specification<ContaReceber> clienteIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("cliente").get("id"), id);
    }

    public static Specification<ContaReceber> vendaFaturamentoIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("vendaFaturamento").get("id"), id);
    }

    public static Specification<ContaReceber> centroCustoIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("centroCusto").get("id"), id);
    }

    public static Specification<ContaReceber> planoContaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("planoConta").get("id"), id);
    }

    public static Specification<ContaReceber> numeroContem(String numero) {
        return numero == null || numero.isBlank() ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("numero")), "%" + numero.toLowerCase() + "%");
    }

    public static Specification<ContaReceber> statusIgual(StatusContaReceber status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ContaReceber> emissaoDe(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataEmissao"), data);
    }

    public static Specification<ContaReceber> emissaoAte(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataEmissao"), data);
    }
}
