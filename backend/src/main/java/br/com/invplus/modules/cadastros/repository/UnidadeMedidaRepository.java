package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;

/**
 * Acesso as unidades de medida usadas pelas rotinas operacionais.
 */
public interface UnidadeMedidaRepository extends BaseRepository<UnidadeMedida> {

    /**
     * Lista apenas unidades ativas, ocultando registros excluidos logicamente.
     */
    List<UnidadeMedida> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Localiza unidade pela sigla normalizada.
     */
    Optional<UnidadeMedida> findBySiglaAndAtivoTrueAndDeletedAtIsNull(String sigla);
}
