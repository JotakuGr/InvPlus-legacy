package br.com.invplus.modules.compras.specification;

import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.compras.enums.StatusEntradaMercadoria;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para entradas de mercadoria.
 */
public final class EntradaMercadoriaSpecification {

    private EntradaMercadoriaSpecification() {
    }

    public static Specification<EntradaMercadoria> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<EntradaMercadoria> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<EntradaMercadoria> empresaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), id);
    }

    public static Specification<EntradaMercadoria> fornecedorIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("fornecedor").get("id"), id);
    }

    public static Specification<EntradaMercadoria> pedidoIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("pedidoCompra").get("id"), id);
    }

    public static Specification<EntradaMercadoria> statusIgual(StatusEntradaMercadoria status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
