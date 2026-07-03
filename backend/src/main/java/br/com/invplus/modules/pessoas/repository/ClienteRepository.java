package br.com.invplus.modules.pessoas.repository;

import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso aos clientes ativos com soft delete aplicado por padrao.
 */
public interface ClienteRepository extends BaseRepository<Cliente> {

    /**
     * Verifica se uma pessoa ja possui papel de cliente ativo.
     */
    boolean existsByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);

    /**
     * Busca cliente ativo pelo identificador da pessoa vinculada.
     */
    Optional<Cliente> findByPessoaIdAndAtivoTrueAndDeletedAtIsNull(UUID pessoaId);
}
