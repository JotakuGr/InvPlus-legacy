package br.com.invplus.shared.domain.repository;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repository base para entidades com UUID, soft delete e suporte nativo a Specifications.
 */
@NoRepositoryBean
public interface BaseRepository<T extends SoftDeleteEntity>
        extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {

    /**
     * Busca um registro ativo sem expor registros excluidos logicamente por padrao.
     */
    Optional<T> findByIdAndAtivoTrueAndDeletedAtIsNull(UUID id);

    /**
     * Lista registros ativos com paginacao obrigatoria para evitar respostas sem limite.
     */
    Page<T> findByAtivoTrueAndDeletedAtIsNull(Pageable pageable);
}
