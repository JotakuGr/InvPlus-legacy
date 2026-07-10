package br.com.invplus.modules.financeiro.specification;

import br.com.invplus.modules.financeiro.entity.ConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Filtros dinamicos para o historico de conciliacoes bancarias. */
public final class ConciliacaoBancariaSpecification {

    private ConciliacaoBancariaSpecification() {
    }

    public static Specification<ConciliacaoBancaria> lancamentoIgual(UUID id) {
        return id == null ? null : (root, query, cb) ->
                cb.equal(root.get("extratoBancarioLancamento").get("id"), id);
    }

    public static Specification<ConciliacaoBancaria> movimentoIgual(UUID id) {
        return id == null ? null : (root, query, cb) ->
                cb.equal(root.get("movimentoFinanceiro").get("id"), id);
    }

    public static Specification<ConciliacaoBancaria> contaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("contaBancaria").get("id"), id);
    }

    public static Specification<ConciliacaoBancaria> tipoIgual(TipoConciliacaoBancaria tipo) {
        return tipo == null ? null : (root, query, cb) -> cb.equal(root.get("tipoConciliacao"), tipo);
    }

    public static Specification<ConciliacaoBancaria> statusIgual(StatusConciliacaoBancaria status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ConciliacaoBancaria> conciliadoEmDe(OffsetDateTime data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("conciliadoEm"), data);
    }

    public static Specification<ConciliacaoBancaria> conciliadoEmAte(OffsetDateTime data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("conciliadoEm"), data);
    }
}
