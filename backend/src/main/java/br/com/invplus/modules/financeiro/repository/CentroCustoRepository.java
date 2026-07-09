package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia dos centros de custo com consultas restritas aos registros ativos.
 */
public interface CentroCustoRepository extends BaseRepository<CentroCusto> {

    Optional<CentroCusto> findByEmpresaIdAndCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
            UUID empresaId, String codigo);
}
