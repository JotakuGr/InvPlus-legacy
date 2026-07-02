package br.com.invplus.modules.cadastros.specification;

import br.com.invplus.modules.cadastros.entity.Pais;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications reutilizaveis para consultas dinamicas de paises.
 */
public final class PaisSpecification {

    private PaisSpecification() {
    }

    /**
     * Restringe a consulta aos registros ativos e nao excluidos.
     */
    public static Specification<Pais> ativo() {
        return (root, query, cb) -> cb.and(cb.isTrue(root.get("ativo")), cb.isNull(root.get("deletedAt")));
    }
}
