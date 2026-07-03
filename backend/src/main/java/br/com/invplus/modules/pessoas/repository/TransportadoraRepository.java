package br.com.invplus.modules.pessoas.repository;

import br.com.invplus.modules.pessoas.entity.Transportadora;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio do papel Transportadora com filtros padrao de registros ativos.
 */
public interface TransportadoraRepository extends BaseRepository<Transportadora> {

    boolean existsByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);

    Optional<Transportadora> findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);
}
