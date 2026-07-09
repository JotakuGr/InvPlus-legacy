package br.com.invplus.modules.pagamentos.repository;

import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;

/**
 * Persistencia de condicoes de pagamento com suporte a consultas por codigo ativo.
 */
public interface CondicaoPagamentoRepository extends BaseRepository<CondicaoPagamento> {

    Optional<CondicaoPagamento> findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String codigo);

    boolean existsByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String codigo);
}
