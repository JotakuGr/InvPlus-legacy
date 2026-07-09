package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consultas paginadas de contas a pagar.
 */
public final class ContaPagarSpecification {

    private ContaPagarSpecification() {
    }

    public static Specification<ContaPagar> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ContaPagar> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<ContaPagar> empresaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), id);
    }

    public static Specification<ContaPagar> filialIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("filial").get("id"), id);
    }

    public static Specification<ContaPagar> fornecedorIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("fornecedor").get("id"), id);
    }

    public static Specification<ContaPagar> entradaMercadoriaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("entradaMercadoria").get("id"), id);
    }

    public static Specification<ContaPagar> centroCustoIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("centroCusto").get("id"), id);
    }

    public static Specification<ContaPagar> planoContaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("planoConta").get("id"), id);
    }

    public static Specification<ContaPagar> numeroContem(String numero) {
        return numero == null || numero.isBlank() ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("numero")), "%" + numero.toLowerCase() + "%");
    }

    public static Specification<ContaPagar> statusIgual(StatusContaPagar status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ContaPagar> emissaoDe(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataEmissao"), data);
    }

    public static Specification<ContaPagar> emissaoAte(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataEmissao"), data);
    }
}
