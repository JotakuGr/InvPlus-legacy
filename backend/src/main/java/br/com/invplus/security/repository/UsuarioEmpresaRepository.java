package br.com.invplus.security.repository;

import br.com.invplus.security.entity.UsuarioEmpresa;
import java.util.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioEmpresaRepository extends JpaRepository<UsuarioEmpresa, UUID> {
    @EntityGraph(attributePaths = {"usuario", "empresa"})
    Optional<UsuarioEmpresa> findByUsuarioIdAndEmpresaIdAndAtivoTrue(UUID usuarioId, UUID empresaId);
    @EntityGraph(attributePaths = {"empresa"})
    List<UsuarioEmpresa> findByUsuarioIdAndAtivoTrueOrderByPrincipalDescEmpresaNomeFantasiaAsc(UUID usuarioId);
    boolean existsByUsuarioIdAndEmpresaId(UUID usuarioId, UUID empresaId);
    boolean existsByUsuarioIdAndPrincipalTrueAndAtivoTrue(UUID usuarioId);
}
