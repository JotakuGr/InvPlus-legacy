package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Acesso as filiais cadastradas.
 */
public interface FilialRepository extends BaseRepository<Filial> {

    /**
     * Lista apenas filiais ativas.
     */
    List<Filial> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Lista filiais ativas por empresa.
     */
    List<Filial> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID empresaId);

    /**
     * Lista filiais ativas por empresa com paginacao.
     */
    Page<Filial> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, Pageable pageable);

    Optional<Filial> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    Optional<Filial> findByIdAndEmpresaIdAndDeletedAtIsNull(UUID id, UUID empresaId);

    /**
     * Verifica duplicidade de codigo dentro de uma empresa.
     */
    boolean existsByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(UUID empresaId, String codigo);

    boolean existsByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNullAndIdNot(
            UUID empresaId, String codigo, UUID id);

    boolean existsByEmpresaIdAndMatrizTrueAndAtivoTrueAndDeletedAtIsNull(UUID empresaId);

    boolean existsByEmpresaIdAndMatrizTrueAndAtivoTrueAndDeletedAtIsNullAndIdNot(UUID empresaId, UUID id);
}
