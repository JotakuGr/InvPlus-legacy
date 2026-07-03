package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;

/**
 * Acesso as marcas comerciais cadastradas.
 */
public interface MarcaRepository extends BaseRepository<Marca> {

    /**
     * Localiza marca ativa pelo nome, ignorando diferencas de caixa.
     */
    Optional<Marca> findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String nome);

    /**
     * Localiza marca pelo nome unico, mantida para compatibilidade com testes antigos.
     */
    Optional<Marca> findByNomeAndAtivoTrueAndDeletedAtIsNull(String nome);
}
