package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;

/**
 * Acesso aos registros de empresas e filiais cadastradas.
 */
public interface EmpresaRepository extends BaseRepository<Empresa> {

    /**
     * Lista apenas empresas ativas, ocultando registros excluidos logicamente.
     */
    List<Empresa> findByAtivoTrueAndDeletedAtIsNull();

    /**
     * Localiza empresa pelo CNPJ normalizado.
     */
    Optional<Empresa> findByCnpjAndAtivoTrueAndDeletedAtIsNull(String cnpj);
}
