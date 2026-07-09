package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.enums.StatusParcelaPagar;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia das parcelas de contas a pagar.
 */
public interface ParcelaPagarRepository extends BaseRepository<ParcelaPagar> {

    Optional<ParcelaPagar> findByContaPagarIdAndNumeroParcelaAndAtivoTrueAndDeletedAtIsNull(
            UUID contaPagarId, Integer numeroParcela);

    boolean existsByContaPagarIdAndAtivoTrueAndDeletedAtIsNull(UUID contaPagarId);

    List<ParcelaPagar> findByContaPagarIdAndAtivoTrueAndDeletedAtIsNull(UUID contaPagarId);

    List<ParcelaPagar> findByContaPagarIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
            UUID contaPagarId, StatusParcelaPagar status);
}
