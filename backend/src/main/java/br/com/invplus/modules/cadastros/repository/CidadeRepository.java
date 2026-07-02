package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Cidade;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Acesso as cidades cadastradas.
 */
public interface CidadeRepository extends BaseRepository<Cidade> {

    /**
     * Lista apenas cidades ativas.
     */
    List<Cidade> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Lista cidades ativas por estado.
     */
    List<Cidade> findByEstadoIdAndAtivoTrueAndDeletedAtIsNull(UUID estadoId);

    /**
     * Lista cidades ativas por estado com paginacao.
     */
    Page<Cidade> findByEstadoIdAndAtivoTrueAndDeletedAtIsNull(UUID estadoId, Pageable pageable);

    /**
     * Verifica duplicidade de nome dentro de um estado.
     */
    boolean existsByEstadoIdAndNomeAndAtivoTrueAndDeletedAtIsNull(UUID estadoId, String nome);
}
