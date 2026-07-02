package br.com.invplus.security.repository;

import br.com.invplus.security.entity.RefreshToken;
import br.com.invplus.security.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevogadoFalse(String tokenHash);

    void deleteByUsuarioAndRevogadoTrue(Usuario usuario);
}
