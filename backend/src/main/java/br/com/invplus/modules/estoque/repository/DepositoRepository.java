package br.com.invplus.modules.estoque.repository;

import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso aos depositos do modulo Estoque.
 */
public interface DepositoRepository extends BaseRepository<Deposito> {

    Optional<Deposito> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    boolean existsByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String codigo);

    boolean existsByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID empresaId);

    Optional<Deposito> findByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String codigo);

    Optional<Deposito> findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID empresaId);
}
