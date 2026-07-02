package br.com.invplus.modules.cadastros.repository;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;

/**
 * Acesso as categorias base do sistema.
 */
public interface CategoriaRepository extends BaseRepository<Categoria> {

    /**
     * Localiza categoria ativa por nome, ignorando diferencas de caixa.
     */
    Optional<Categoria> findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String nome);

    /**
     * Localiza categoria pelo par funcional nome/tipo mantido para compatibilidade.
     */
    Optional<Categoria> findByNomeAndTipoAndAtivoTrueAndDeletedAtIsNull(String nome, String tipo);
}
