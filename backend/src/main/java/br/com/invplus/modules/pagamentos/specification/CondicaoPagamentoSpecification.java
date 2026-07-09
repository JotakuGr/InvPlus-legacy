package br.com.invplus.modules.pagamentos.specification;

import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consultas paginadas de condicoes de pagamento.
 */
public final class CondicaoPagamentoSpecification {

    private CondicaoPagamentoSpecification() {
    }

    public static Specification<CondicaoPagamento> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<CondicaoPagamento> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<CondicaoPagamento> codigoContem(String codigo) {
        return codigo == null || codigo.isBlank()
                ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("codigo")), "%" + codigo.trim().toLowerCase() + "%");
    }

    public static Specification<CondicaoPagamento> nomeContem(String nome) {
        return nome == null || nome.isBlank()
                ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%");
    }
}
