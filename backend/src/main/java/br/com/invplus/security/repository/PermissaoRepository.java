package br.com.invplus.security.repository;

import br.com.invplus.security.entity.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissaoRepository extends JpaRepository<Permissao, UUID> {

    Optional<Permissao> findByRecursoAndAcao(String recurso, String acao);

    boolean existsByRecursoAndAcao(String recurso, String acao);
}
