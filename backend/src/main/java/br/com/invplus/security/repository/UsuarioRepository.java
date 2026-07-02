package br.com.invplus.security.repository;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.shared.domain.repository.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends BaseRepository<Usuario> {

    Optional<Usuario> findByEmailAndAtivoTrueAndDeletedAtIsNull(String email);

    @EntityGraph(attributePaths = {"perfis", "perfis.permissoes"})
    @Query("select u from Usuario u where u.email = :email and u.ativo = true and u.deletedAt is null")
    Optional<Usuario> findByEmailWithPerfisAndPermissoes(@Param("email") String email);

    boolean existsByEmailAndAtivoTrueAndDeletedAtIsNull(String email);
}
