package br.com.invplus.security.repository;

import br.com.invplus.security.entity.UsuarioEmpresaFilial;
import java.util.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioEmpresaFilialRepository extends JpaRepository<UsuarioEmpresaFilial, UUID> {
    @EntityGraph(attributePaths = {"filial", "filial.empresa"})
    List<UsuarioEmpresaFilial> findByUsuarioEmpresaIdAndAtivoTrueOrderByPrincipalDescFilialNomeAsc(UUID vinculoId);
    boolean existsByUsuarioEmpresaIdAndFilialIdAndAtivoTrue(UUID vinculoId, UUID filialId);
    boolean existsByUsuarioEmpresaIdAndFilialId(UUID vinculoId, UUID filialId);
    boolean existsByUsuarioEmpresaIdAndPrincipalTrueAndAtivoTrue(UUID vinculoId);
}
