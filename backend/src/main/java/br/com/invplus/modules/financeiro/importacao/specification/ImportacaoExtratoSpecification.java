package br.com.invplus.modules.financeiro.importacao.specification;

import br.com.invplus.modules.financeiro.importacao.entity.ImportacaoExtratoBancario;
import br.com.invplus.modules.financeiro.importacao.enums.FormatoArquivoExtrato;
import br.com.invplus.modules.financeiro.importacao.enums.StatusImportacaoExtrato;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Filtros para acompanhamento paginado das importacoes. */
public final class ImportacaoExtratoSpecification {
    private ImportacaoExtratoSpecification() {
    }

    public static Specification<ImportacaoExtratoBancario> pertenceEmpresa(UUID empresaId) {
        return (root, query, cb) -> cb.equal(root.get("contaBancaria").get("empresa").get("id"), empresaId);
    }

    public static Specification<ImportacaoExtratoBancario> contaIgual(UUID id) {
        return id == null ? null : (root, query, cb) -> cb.equal(root.get("contaBancaria").get("id"), id);
    }

    public static Specification<ImportacaoExtratoBancario> formatoIgual(FormatoArquivoExtrato formato) {
        return formato == null ? null : (root, query, cb) -> cb.equal(root.get("formato"), formato);
    }

    public static Specification<ImportacaoExtratoBancario> statusIgual(StatusImportacaoExtrato status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ImportacaoExtratoBancario> hashIgual(String hash) {
        return hash == null || hash.isBlank() ? null
                : (root, query, cb) -> cb.equal(cb.lower(root.get("arquivoHash")), hash.trim().toLowerCase());
    }

    public static Specification<ImportacaoExtratoBancario> iniciadaDe(OffsetDateTime data) {
        return data == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("iniciadaEm"), data);
    }

    public static Specification<ImportacaoExtratoBancario> iniciadaAte(OffsetDateTime data) {
        return data == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("iniciadaEm"), data);
    }
}
