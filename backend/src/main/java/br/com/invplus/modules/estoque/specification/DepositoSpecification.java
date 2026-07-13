package br.com.invplus.modules.estoque.specification;

import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros dinamicos de Deposito.
 */
public final class DepositoSpecification {

    private DepositoSpecification() {
    }

    public static Specification<Deposito> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    public static Specification<Deposito> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<Deposito> pertenceEmpresa(UUID empresaId) {
        return (root, query, builder) -> builder.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<Deposito> filialIgual(UUID filialId) {
        return filialId == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("filial").get("id"), filialId);
    }

    public static Specification<Deposito> codigoContem(String codigo) {
        return textoVazio(codigo) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(builder.lower(root.get("codigo")), "%" + codigo.trim().toLowerCase() + "%");
    }

    public static Specification<Deposito> nomeContem(String nome) {
        return textoVazio(nome) ? sempreVerdadeiro()
                : (root, query, builder) -> builder.like(builder.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%");
    }

    public static Specification<Deposito> tipoIgual(TipoDeposito tipoDeposito) {
        return tipoDeposito == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("tipoDeposito"), tipoDeposito);
    }

    public static Specification<Deposito> principalIgual(Boolean principal) {
        return principal == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("principal"), principal);
    }

    public static Specification<Deposito> controlaEstoqueIgual(Boolean controlaEstoque) {
        return controlaEstoque == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("controlaEstoque"), controlaEstoque);
    }

    public static Specification<Deposito> bloqueadoIgual(Boolean bloqueado) {
        return bloqueado == null ? sempreVerdadeiro()
                : (root, query, builder) -> builder.equal(root.get("bloqueado"), bloqueado);
    }

    private static boolean textoVazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static Specification<Deposito> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }
}
