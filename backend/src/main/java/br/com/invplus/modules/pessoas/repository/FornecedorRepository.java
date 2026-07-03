package br.com.invplus.modules.pessoas.repository;

import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio do papel Fornecedor com filtros padrao de registros ativos.
 */
public interface FornecedorRepository extends BaseRepository<Fornecedor> {

    boolean existsByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);

    Optional<Fornecedor> findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);
}
