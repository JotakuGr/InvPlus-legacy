package br.com.invplus.modules.pagamentos.repository;

import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;

/**
 * Persistencia de formas de pagamento com soft delete por padrao.
 */
public interface FormaPagamentoRepository extends BaseRepository<FormaPagamento> {

    Optional<FormaPagamento> findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String codigo);

    boolean existsByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String codigo);
}
