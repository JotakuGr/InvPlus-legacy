package br.com.invplus.security.repository;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.shared.domain.repository.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends BaseRepository<Usuario> {

    Optional<Usuario> findByEmailAndAtivoTrueAndDeletedAtIsNull(String email);

    @EntityGraph(attributePaths = {"perfis", "perfis.permissoes"})
    @Query("select u from Usuario u where u.email = :email and u.ativo = true and u.deletedAt is null")
    Optional<Usuario> findByEmailWithPerfisAndPermissoes(@Param("email") String email);

    boolean existsByEmailAndAtivoTrueAndDeletedAtIsNull(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Usuario u set u.ultimoLogin = :ultimoLogin where u.id = :id")
    int atualizarUltimoLogin(@Param("id") UUID id, @Param("ultimoLogin") OffsetDateTime ultimoLogin);

    @EntityGraph(attributePaths = {"perfis", "perfis.permissoes"})
    @Query("select u from Usuario u where u.id=:id and u.ativo=true and u.deletedAt is null")
    Optional<Usuario> findByIdWithPerfisAndPermissoes(@Param("id") UUID id);
}
