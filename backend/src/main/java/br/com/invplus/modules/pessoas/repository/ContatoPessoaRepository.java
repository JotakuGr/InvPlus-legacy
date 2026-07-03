package br.com.invplus.modules.pessoas.repository;

import br.com.invplus.modules.pessoas.entity.ContatoPessoa;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio de contatos ativos vinculados a Pessoa.
 */
public interface ContatoPessoaRepository extends BaseRepository<ContatoPessoa> {

    List<ContatoPessoa> findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);
}
