package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para listagens paginadas de centros de custo.
 */
public final class CentroCustoSpecification {

    private CentroCustoSpecification() {
    }

    public static Specification<CentroCusto> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<CentroCusto> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<CentroCusto> empresaIgual(UUID empresaId) {
        return empresaId == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<CentroCusto> paiIgual(UUID paiId) {
        return paiId == null ? null : (root, query, cb) -> cb.equal(root.get("centroCustoPai").get("id"), paiId);
    }

    public static Specification<CentroCusto> codigoContem(String codigo) {
        return textoContem("codigo", codigo);
    }

    public static Specification<CentroCusto> nomeContem(String nome) {
        return textoContem("nome", nome);
    }

    public static Specification<CentroCusto> tipoIgual(TipoCentroCusto tipo) {
        return tipo == null ? null : (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    private static Specification<CentroCusto> textoContem(String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String termo = "%" + valor.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(campo)), termo);
    }
}
