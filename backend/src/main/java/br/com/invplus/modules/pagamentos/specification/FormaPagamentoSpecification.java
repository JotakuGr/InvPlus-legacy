package br.com.invplus.modules.pagamentos.specification;

import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consultas paginadas de formas de pagamento.
 */
public final class FormaPagamentoSpecification {

    private FormaPagamentoSpecification() {
    }

    public static Specification<FormaPagamento> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<FormaPagamento> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<FormaPagamento> codigoContem(String codigo) {
        return codigo == null || codigo.isBlank()
                ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("codigo")), "%" + codigo.trim().toLowerCase() + "%");
    }

    public static Specification<FormaPagamento> nomeContem(String nome) {
        return nome == null || nome.isBlank()
                ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%");
    }

    public static Specification<FormaPagamento> tipoIgual(TipoFormaPagamento tipo) {
        return tipo == null ? null : (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }
}
