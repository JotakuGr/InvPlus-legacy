package br.com.invplus.modules.vendas.specification;

import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consultas paginadas de pedidos de venda.
 */
public final class PedidoVendaSpecification {

    private PedidoVendaSpecification() {
    }

    public static Specification<PedidoVenda> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<PedidoVenda> ativo(Boolean ativo) {
        return ativo == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<PedidoVenda> pertenceEmpresa(UUID id) {
        return (root, query, cb) -> cb.equal(root.get("empresa").get("id"), id);
    }

    public static Specification<PedidoVenda> filialIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("filial").get("id"), id);
    }

    public static Specification<PedidoVenda> clienteIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("cliente").get("id"), id);
    }

    public static Specification<PedidoVenda> vendedorIgual(UUID id) {
        return id == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("vendedor").get("id"), id);
    }

    public static Specification<PedidoVenda> numeroContem(String numero) {
        return numero == null || numero.isBlank() ? sempreVerdadeiro()
                : (root, query, cb) -> cb.like(cb.lower(root.get("numero")), "%" + numero.toLowerCase() + "%");
    }

    public static Specification<PedidoVenda> statusIgual(StatusPedidoVenda status) {
        return status == null ? sempreVerdadeiro() : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<PedidoVenda> dataEmissaoMaiorOuIgual(LocalDate data) {
        return data == null ? sempreVerdadeiro() : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataEmissao"), data);
    }

    public static Specification<PedidoVenda> dataEmissaoMenorOuIgual(LocalDate data) {
        return data == null ? sempreVerdadeiro() : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataEmissao"), data);
    }

    private static Specification<PedidoVenda> sempreVerdadeiro() {
        return (root, query, cb) -> cb.conjunction();
    }
}
