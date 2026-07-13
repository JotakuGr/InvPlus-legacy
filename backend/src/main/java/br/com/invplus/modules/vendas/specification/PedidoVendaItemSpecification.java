package br.com.invplus.modules.vendas.specification;

import br.com.invplus.modules.vendas.entity.PedidoVendaItem;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para itens de pedido de venda.
 */
public final class PedidoVendaItemSpecification {

    private PedidoVendaItemSpecification() {
    }

    public static Specification<PedidoVendaItem> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<PedidoVendaItem> ativo(Boolean ativo) {
        return ativo == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<PedidoVendaItem> pertenceEmpresa(UUID empresaId) {
        return (root, query, cb) -> cb.equal(root.get("pedidoVenda").get("empresa").get("id"), empresaId);
    }

    public static Specification<PedidoVendaItem> pedidoIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("pedidoVenda").get("id"), id);
    }

    public static Specification<PedidoVendaItem> produtoIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("produto").get("id"), id);
    }

    private static Specification<PedidoVendaItem> sempreVerdadeiro() {
        return (root, query, cb) -> cb.conjunction();
    }
}
