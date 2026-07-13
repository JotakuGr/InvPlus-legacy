package br.com.invplus.modules.compras.specification;

import br.com.invplus.modules.compras.entity.EntradaMercadoriaItem;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para itens de entrada de mercadoria.
 */
public final class EntradaMercadoriaItemSpecification {

    private EntradaMercadoriaItemSpecification() {
    }

    public static Specification<EntradaMercadoriaItem> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<EntradaMercadoriaItem> pertenceEmpresa(UUID empresaId) {
        return (root, query, cb) -> cb.equal(
                root.get("entradaMercadoria").get("empresa").get("id"), empresaId);
    }

    public static Specification<EntradaMercadoriaItem> ativo(Boolean ativo) {
        return ativo == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<EntradaMercadoriaItem> entradaIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("entradaMercadoria").get("id"), id);
    }

    public static Specification<EntradaMercadoriaItem> produtoIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("produto").get("id"), id);
    }

    private static Specification<EntradaMercadoriaItem> sempreVerdadeiro() {
        return (root, query, cb) -> cb.conjunction();
    }
}
