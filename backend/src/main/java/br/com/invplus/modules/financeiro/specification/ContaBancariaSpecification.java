package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consulta paginada de contas bancarias.
 */
public final class ContaBancariaSpecification {

    private ContaBancariaSpecification() {
    }

    public static Specification<ContaBancaria> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ContaBancaria> empresaIgual(UUID empresaId) {
        return empresaId == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<ContaBancaria> filialIgual(UUID filialId) {
        return filialId == null ? null : (root, query, cb) -> cb.equal(root.get("filial").get("id"), filialId);
    }

    public static Specification<ContaBancaria> bancoCodigoContem(String bancoCodigo) {
        return textoContem("bancoCodigo", bancoCodigo);
    }

    public static Specification<ContaBancaria> bancoNomeContem(String bancoNome) {
        return textoContem("bancoNome", bancoNome);
    }

    public static Specification<ContaBancaria> tipoIgual(TipoContaBancaria tipoConta) {
        return tipoConta == null ? null : (root, query, cb) -> cb.equal(root.get("tipoConta"), tipoConta);
    }

    public static Specification<ContaBancaria> principalIgual(Boolean principal) {
        return principal == null ? null : (root, query, cb) -> cb.equal(root.get("principal"), principal);
    }

    public static Specification<ContaBancaria> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    private static Specification<ContaBancaria> textoContem(String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String termo = "%" + valor.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(campo)), termo);
    }
}
