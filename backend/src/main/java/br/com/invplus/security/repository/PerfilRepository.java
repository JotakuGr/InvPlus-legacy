package br.com.invplus.security.repository;

import br.com.invplus.security.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilRepository extends JpaRepository<Perfil, UUID> {

    Optional<Perfil> findByNome(String nome);

    boolean existsByNome(String nome);
}
