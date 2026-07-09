package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.enums.StatusParcelaReceber;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de parcelas a receber.
 */
public interface ParcelaReceberRepository extends BaseRepository<ParcelaReceber> {

    boolean existsByContaReceberIdAndNumeroParcelaAndAtivoTrueAndDeletedAtIsNull(UUID contaReceberId, Integer numeroParcela);

    Optional<ParcelaReceber> findByContaReceberIdAndNumeroParcelaAndAtivoTrueAndDeletedAtIsNull(UUID contaReceberId, Integer numeroParcela);

    boolean existsByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(UUID contaReceberId);

    List<ParcelaReceber> findByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(UUID contaReceberId);

    List<ParcelaReceber> findByContaReceberIdAndStatusAndAtivoTrueAndDeletedAtIsNull(UUID contaReceberId, StatusParcelaReceber status);
}
