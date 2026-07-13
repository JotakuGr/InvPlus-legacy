package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia do plano de contas com consultas restritas aos registros ativos.
 */
public interface PlanoContaRepository extends BaseRepository<PlanoConta> {

    Optional<PlanoConta> findByEmpresaIdAndCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
            UUID empresaId, String codigo);

    Optional<PlanoConta> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);
}
