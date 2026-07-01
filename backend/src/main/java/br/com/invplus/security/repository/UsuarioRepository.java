package br.com.invplus.security.repository;

import br.com.invplus.security.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    @EntityGraph(attributePaths = {"perfis", "perfis.permissoes"})
    @Query("select u from Usuario u where u.email = :email")
    Optional<Usuario> findByEmailWithPerfisAndPermissoes(@Param("email") String email);

    boolean existsByEmail(String email);
}
