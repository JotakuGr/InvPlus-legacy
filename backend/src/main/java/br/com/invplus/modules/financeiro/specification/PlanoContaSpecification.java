package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para a listagem paginada do plano de contas.
 */
public final class PlanoContaSpecification {

    private PlanoContaSpecification() {
    }

    public static Specification<PlanoConta> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<PlanoConta> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<PlanoConta> empresaIgual(UUID empresaId) {
        return empresaId == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<PlanoConta> paiIgual(UUID paiId) {
        return paiId == null ? null : (root, query, cb) -> cb.equal(root.get("planoContaPai").get("id"), paiId);
    }

    public static Specification<PlanoConta> codigoContem(String codigo) {
        return textoContem("codigo", codigo);
    }

    public static Specification<PlanoConta> nomeContem(String nome) {
        return textoContem("nome", nome);
    }

    public static Specification<PlanoConta> tipoIgual(TipoPlanoConta tipo) {
        return tipo == null ? null : (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<PlanoConta> naturezaIgual(NaturezaPlanoConta natureza) {
        return natureza == null ? null : (root, query, cb) -> cb.equal(root.get("natureza"), natureza);
    }

    private static Specification<PlanoConta> textoContem(String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String termo = "%" + valor.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(campo)), termo);
    }
}
