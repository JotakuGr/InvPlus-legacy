package br.com.invplus.modules.pessoas.repository;

import br.com.invplus.common.enums.PapelPessoa;
import br.com.invplus.modules.pessoas.entity.PessoaRole;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio do indice auxiliar de papeis ativos de Pessoa.
 */
public interface PessoaRoleRepository extends BaseRepository<PessoaRole> {

    boolean existsByPessoaIdAndRoleAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId, PapelPessoa role);

    Optional<PessoaRole> findByPessoaIdAndRoleAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId, PapelPessoa role);

    List<PessoaRole> findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);
}
