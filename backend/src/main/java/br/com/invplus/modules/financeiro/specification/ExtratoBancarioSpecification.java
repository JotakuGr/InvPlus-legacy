package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinamicos para consulta paginada de extratos bancarios.
 */
public final class ExtratoBancarioSpecification {

    private ExtratoBancarioSpecification() {
    }

    public static Specification<ExtratoBancario> naoDeletado() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ExtratoBancario> contaBancariaIgual(UUID contaBancariaId) {
        return contaBancariaId == null ? null
                : (root, query, cb) -> cb.equal(root.get("contaBancaria").get("id"), contaBancariaId);
    }

    public static Specification<ExtratoBancario> origemIgual(OrigemExtratoBancario origem) {
        return origem == null ? null : (root, query, cb) -> cb.equal(root.get("origem"), origem);
    }

    public static Specification<ExtratoBancario> statusIgual(StatusExtratoBancario status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ExtratoBancario> dataInicioMaiorOuIgual(LocalDate dataInicioDe) {
        return dataInicioDe == null ? null
                : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataInicio"), dataInicioDe);
    }

    public static Specification<ExtratoBancario> dataInicioMenorOuIgual(LocalDate dataInicioAte) {
        return dataInicioAte == null ? null
                : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataInicio"), dataInicioAte);
    }

    public static Specification<ExtratoBancario> dataFimMaiorOuIgual(LocalDate dataFimDe) {
        return dataFimDe == null ? null
                : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataFim"), dataFimDe);
    }

    public static Specification<ExtratoBancario> dataFimMenorOuIgual(LocalDate dataFimAte) {
        return dataFimAte == null ? null
                : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataFim"), dataFimAte);
    }

    public static Specification<ExtratoBancario> ativo(Boolean ativo) {
        return ativo == null ? null : (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }
}
