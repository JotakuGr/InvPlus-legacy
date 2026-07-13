package br.com.invplus.modules.compras.specification;

import br.com.invplus.modules.compras.entity.PedidoCompraItem;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para itens de pedido de compra.
 */
public final class PedidoCompraItemSpecification {

    private PedidoCompraItemSpecification() {
    }

    public static Specification<PedidoCompraItem> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<PedidoCompraItem> pertenceEmpresa(UUID empresaId) {
        return (root, query, cb) -> cb.equal(
                root.get("pedidoCompra").get("empresa").get("id"), empresaId);
    }

    public static Specification<PedidoCompraItem> ativo(Boolean ativo) {
        return ativo == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<PedidoCompraItem> pedidoIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("pedidoCompra").get("id"), id);
    }

    public static Specification<PedidoCompraItem> produtoIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("produto").get("id"), id);
    }

    private static Specification<PedidoCompraItem> sempreVerdadeiro() {
        return (root, query, cb) -> cb.conjunction();
    }
}
