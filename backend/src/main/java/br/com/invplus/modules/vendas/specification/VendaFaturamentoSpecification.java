package br.com.invplus.modules.vendas.specification;

import br.com.invplus.modules.vendas.entity.VendaFaturamento;
import br.com.invplus.modules.vendas.enums.StatusVendaFaturamento;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consultas paginadas de faturamentos de venda.
 */
public final class VendaFaturamentoSpecification {

    private VendaFaturamentoSpecification() {
    }

    public static Specification<VendaFaturamento> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<VendaFaturamento> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<VendaFaturamento> pedidoIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("pedidoVenda").get("id"), id);
    }

    public static Specification<VendaFaturamento> empresaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), id);
    }

    public static Specification<VendaFaturamento> filialIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("filial").get("id"), id);
    }

    public static Specification<VendaFaturamento> clienteIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("cliente").get("id"), id);
    }

    public static Specification<VendaFaturamento> numeroContem(String numero) {
        return numero == null || numero.isBlank() ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("numero")), "%" + numero.toLowerCase() + "%");
    }

    public static Specification<VendaFaturamento> statusIgual(StatusVendaFaturamento status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
