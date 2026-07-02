package br.com.invplus.modules.cadastros.specification;

import br.com.invplus.modules.cadastros.entity.Cidade;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications reutilizaveis para consultas dinamicas de cidades.
 */
public final class CidadeSpecification {

    private CidadeSpecification() {
    }

    /**
     * Restringe a consulta aos registros ativos e nao excluidos.
     */
    public static Specification<Cidade> ativo() {
        return (root, query, cb) -> cb.and(cb.isTrue(root.get("ativo")), cb.isNull(root.get("deletedAt")));
    }
}
