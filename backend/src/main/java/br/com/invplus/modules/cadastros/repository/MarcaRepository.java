package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;

/**
 * Acesso as marcas comerciais cadastradas.
 */
public interface MarcaRepository extends BaseRepository<Marca> {

    /**
     * Lista apenas marcas ativas, ocultando registros excluidos logicamente.
     */
    List<Marca> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Localiza marca pelo nome unico.
     */
    Optional<Marca> findByNomeAndAtivoTrueAndDeletedAtIsNull(String nome);
}
