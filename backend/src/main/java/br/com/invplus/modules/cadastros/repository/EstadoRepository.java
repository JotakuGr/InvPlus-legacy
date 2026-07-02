package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Estado;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Acesso aos estados cadastrados.
 */
public interface EstadoRepository extends BaseRepository<Estado> {

    /**
     * Lista apenas estados ativos.
     */
    List<Estado> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Lista estados ativos por pais.
     */
    List<Estado> findByPaisIdAndAtivoTrueAndDeletedAtIsNull(UUID paisId);

    /**
     * Lista estados ativos por pais com paginacao.
     */
    Page<Estado> findByPaisIdAndAtivoTrueAndDeletedAtIsNull(UUID paisId, Pageable pageable);

    /**
     * Verifica duplicidade de UF dentro de um pais.
     */
    boolean existsByPaisIdAndUfAndAtivoTrueAndDeletedAtIsNull(UUID paisId, String uf);
}
