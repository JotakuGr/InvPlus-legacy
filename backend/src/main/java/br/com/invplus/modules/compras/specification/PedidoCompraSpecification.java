package br.com.invplus.modules.compras.specification;

import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.modules.compras.enums.StatusPedidoCompra;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para listagem de pedidos de compra.
 */
public final class PedidoCompraSpecification {

    private PedidoCompraSpecification() {
    }

    public static Specification<PedidoCompra> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<PedidoCompra> ativo(Boolean ativo) {
        return ativo == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<PedidoCompra> pertenceEmpresa(UUID id) {
        return (root, query, cb) -> cb.equal(root.get("empresa").get("id"), id);
    }

    public static Specification<PedidoCompra> fornecedorIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("fornecedor").get("id"), id);
    }

    public static Specification<PedidoCompra> statusIgual(StatusPedidoCompra status) {
        return status == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<PedidoCompra> numeroContem(String numero) {
        return numero == null || numero.isBlank() ? sempreVerdadeiro()
                : (root, query, cb) -> cb.like(cb.lower(root.get("numero")), "%" + numero.toLowerCase() + "%");
    }

    private static Specification<PedidoCompra> sempreVerdadeiro() {
        return (root, query, cb) -> cb.conjunction();
    }
}
