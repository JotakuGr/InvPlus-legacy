package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Acesso aos metadados de arquivos anexados.
 */
public interface ArquivoRepository extends BaseRepository<Arquivo> {

    /**
     * Lista apenas arquivos ativos, ocultando registros excluidos logicamente.
     */
    List<Arquivo> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Lista anexos associados a uma entidade especifica.
     */
    List<Arquivo> findByEntidadeAndEntidadeIdAndAtivoTrueAndDeletedAtIsNull(String entidade, UUID entidadeId);

    /**
     * Lista anexos associados a uma entidade especifica com paginacao.
     */
    Page<Arquivo> findByEntidadeAndEntidadeIdAndAtivoTrueAndDeletedAtIsNull(
            String entidade,
            UUID entidadeId,
            Pageable pageable
    );
}
