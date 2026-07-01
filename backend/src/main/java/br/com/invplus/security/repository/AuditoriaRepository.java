package br.com.invplus.security.repository;

import br.com.invplus.security.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditoriaRepository extends JpaRepository<Auditoria, UUID> {
}
