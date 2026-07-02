package br.com.invplus.modules.cadastros.specification;

import br.com.invplus.modules.cadastros.entity.Estado;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications reutilizaveis para consultas dinamicas de estados.
 */
public final class EstadoSpecification {

    private EstadoSpecification() {
    }

    /**
     * Restringe a consulta aos registros ativos e nao excluidos.
     */
    public static Specification<Estado> ativo() {
        return (root, query, cb) -> cb.and(cb.isTrue(root.get("ativo")), cb.isNull(root.get("deletedAt")));
    }
}
