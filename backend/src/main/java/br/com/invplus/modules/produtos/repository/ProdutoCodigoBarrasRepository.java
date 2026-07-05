package br.com.invplus.modules.produtos.repository;

import br.com.invplus.modules.produtos.entity.ProdutoCodigoBarras;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de codigos de barras e codigos alternativos de Produto.
 */
public interface ProdutoCodigoBarrasRepository extends BaseRepository<ProdutoCodigoBarras> {

    boolean existsByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String codigo);

    boolean existsByProdutoIdAndCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(UUID produtoId, String codigo);

    boolean existsByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    List<ProdutoCodigoBarras> findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    Optional<ProdutoCodigoBarras> findByCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(String codigo);

    Optional<ProdutoCodigoBarras> findByProdutoIdAndCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            String codigo
    );

    Optional<ProdutoCodigoBarras> findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);
}
