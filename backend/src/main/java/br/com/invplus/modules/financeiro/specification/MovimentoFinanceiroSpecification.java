package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para a consulta paginada do fluxo financeiro.
 */
public final class MovimentoFinanceiroSpecification {

    private MovimentoFinanceiroSpecification() {
    }

    public static Specification<MovimentoFinanceiro> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<MovimentoFinanceiro> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<MovimentoFinanceiro> empresaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("empresa").get("id"), id);
    }

    public static Specification<MovimentoFinanceiro> pertenceEmpresa(UUID empresaId) {
        return (root, query, cb) -> cb.equal(root.get("empresa").get("id"), empresaId);
    }

    public static Specification<MovimentoFinanceiro> filialIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("filial").get("id"), id);
    }

    public static Specification<MovimentoFinanceiro> tipoIgual(TipoMovimentoFinanceiro tipo) {
        return tipo == null ? null : (root, query, cb) -> cb.equal(root.get("tipoMovimento"), tipo);
    }

    public static Specification<MovimentoFinanceiro> origemIgual(OrigemMovimentoFinanceiro origem) {
        return origem == null ? null : (root, query, cb) -> cb.equal(root.get("origem"), origem);
    }

    public static Specification<MovimentoFinanceiro> origemIdIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("origemId"), id);
    }

    public static Specification<MovimentoFinanceiro> formaPagamentoIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("formaPagamento").get("id"), id);
    }

    public static Specification<MovimentoFinanceiro> clienteIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("cliente").get("id"), id);
    }

    public static Specification<MovimentoFinanceiro> fornecedorIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("fornecedor").get("id"), id);
    }

    public static Specification<MovimentoFinanceiro> centroCustoIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("centroCusto").get("id"), id);
    }

    public static Specification<MovimentoFinanceiro> planoContaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("planoConta").get("id"), id);
    }

    public static Specification<MovimentoFinanceiro> statusIgual(StatusMovimentoFinanceiro status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<MovimentoFinanceiro> movimentoDe(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataMovimento"), data);
    }

    public static Specification<MovimentoFinanceiro> movimentoAte(LocalDate data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataMovimento"), data);
    }
}
