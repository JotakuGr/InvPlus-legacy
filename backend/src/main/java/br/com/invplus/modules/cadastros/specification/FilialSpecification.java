package br.com.invplus.modules.cadastros.specification;

import br.com.invplus.modules.cadastros.entity.Filial;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications reutilizaveis para consultas dinamicas de filiais.
 */
public final class FilialSpecification {

    private FilialSpecification() {
    }

    /**
     * Restringe a consulta aos registros ativos e nao excluidos.
     */
    public static Specification<Filial> ativo() {
        return (root, query, cb) -> cb.and(cb.isTrue(root.get("ativo")), cb.isNull(root.get("deletedAt")));
    }
}
