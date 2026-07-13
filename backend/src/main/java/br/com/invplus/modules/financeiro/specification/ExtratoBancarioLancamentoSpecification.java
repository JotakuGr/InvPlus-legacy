package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consulta paginada de lancamentos bancarios.
 */
public final class ExtratoBancarioLancamentoSpecification {

    private ExtratoBancarioLancamentoSpecification() {
    }

    public static Specification<ExtratoBancarioLancamento> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ExtratoBancarioLancamento> pertenceEmpresa(UUID empresaId) {
        return (root, query, cb) -> cb.equal(root.get("extratoBancario")
                .get("contaBancaria").get("empresa").get("id"), empresaId);
    }

    public static Specification<ExtratoBancarioLancamento> extratoIgual(UUID extratoBancarioId) {
        return extratoBancarioId == null ? null
                : (root, query, cb) -> cb.equal(root.get("extratoBancario").get("id"), extratoBancarioId);
    }

    public static Specification<ExtratoBancarioLancamento> contaBancariaIgual(UUID contaBancariaId) {
        return contaBancariaId == null ? null
                : (root, query, cb) -> cb.equal(root.get("extratoBancario").get("contaBancaria").get("id"),
                        contaBancariaId);
    }

    public static Specification<ExtratoBancarioLancamento> tipoIgual(TipoLancamentoBancario tipo) {
        return tipo == null ? null : (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<ExtratoBancarioLancamento> categoriaIgual(CategoriaLancamentoBancario categoria) {
        return categoria == null ? null : (root, query, cb) -> cb.equal(root.get("categoriaBancaria"), categoria);
    }

    public static Specification<ExtratoBancarioLancamento> conciliadoIgual(Boolean conciliado) {
        return conciliado == null ? null : (root, query, cb) -> cb.equal(root.get("conciliado"), conciliado);
    }

    public static Specification<ExtratoBancarioLancamento> dataLancamentoMaiorOuIgual(LocalDate dataDe) {
        return dataDe == null ? null
                : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataLancamento"), dataDe);
    }

    public static Specification<ExtratoBancarioLancamento> dataLancamentoMenorOuIgual(LocalDate dataAte) {
        return dataAte == null ? null
                : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataLancamento"), dataAte);
    }

    public static Specification<ExtratoBancarioLancamento> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }
}
