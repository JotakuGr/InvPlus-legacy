package br.com.invplus.modules.estoque.specification;

import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros de LocalizacaoEstoque.
 */
public final class LocalizacaoEstoqueSpecification {

    private LocalizacaoEstoqueSpecification() {
    }

    public static Specification<LocalizacaoEstoque> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<LocalizacaoEstoque> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<LocalizacaoEstoque> depositoIgual(UUID depositoId) {
        return depositoId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("deposito").get("id"), depositoId);
    }

    public static Specification<LocalizacaoEstoque> codigoContem(String codigo) {
        return textoVazio(codigo) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(builder.lower(root.get("codigo")), "%" + codigo.trim().toLowerCase() + "%");
    }

    public static Specification<LocalizacaoEstoque> descricaoContem(String descricao) {
        return textoVazio(descricao) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(builder.lower(root.get("descricao")), "%" + descricao.trim().toLowerCase() + "%");
    }

    public static Specification<LocalizacaoEstoque> ruaContem(String rua) {
        return textoVazio(rua) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(builder.lower(root.get("rua")), "%" + rua.trim().toLowerCase() + "%");
    }

    public static Specification<LocalizacaoEstoque> corredorContem(String corredor) {
        return textoVazio(corredor) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(builder.lower(root.get("corredor")), "%" + corredor.trim().toLowerCase() + "%");
    }

    public static Specification<LocalizacaoEstoque> prateleiraContem(String prateleira) {
        return textoVazio(prateleira) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(builder.lower(root.get("prateleira")), "%" + prateleira.trim().toLowerCase() + "%");
    }

    public static Specification<LocalizacaoEstoque> principalIgual(Boolean principal) {
        return principal == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("principal"), principal);
    }

    public static Specification<LocalizacaoEstoque> bloqueadaIgual(Boolean bloqueada) {
        return bloqueada == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("bloqueada"), bloqueada);
    }

    private static boolean textoVazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static Specification<LocalizacaoEstoque> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
