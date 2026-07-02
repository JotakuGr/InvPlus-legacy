package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Pais;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;

/**
 * Acesso aos paises cadastrados.
 */
public interface PaisRepository extends BaseRepository<Pais> {

    /**
     * Lista apenas paises ativos, ocultando registros excluidos logicamente.
     */
    List<Pais> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Verifica duplicidade de codigo ISO2 entre paises ativos.
     */
    boolean existsByCodigoIso2AndAtivoTrueAndDeletedAtIsNull(String codigoIso2);
}
