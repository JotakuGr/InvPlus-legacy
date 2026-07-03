package br.com.invplus.modules.pessoas.repository;

import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;

/**
 * Acesso aos cadastros de pessoas com soft delete aplicado por padrao.
 */
public interface PessoaRepository extends BaseRepository<Pessoa> {

    /**
     * Busca pessoa ativa pelo CPF/CNPJ normalizado.
     */
    Optional<Pessoa> findByCpfCnpjAndAtivoTrueAndDeletedAtIsNull(String cpfCnpj);

    /**
     * Verifica duplicidade ativa de CPF/CNPJ.
     */
    boolean existsByCpfCnpjAndAtivoTrueAndDeletedAtIsNull(String cpfCnpj);
}
