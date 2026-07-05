package br.com.invplus.modules.produtos.repository;

import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio das unidades alternativas do produto.
 */
public interface ProdutoUnidadeRepository extends BaseRepository<ProdutoUnidade> {

    boolean existsByProdutoIdAndUnidadeMedidaIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId, UUID unidadeMedidaId);

    boolean existsByProdutoIdAndUnidadePadraoTrueAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    List<ProdutoUnidade> findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    Optional<ProdutoUnidade> findByProdutoIdAndUnidadeMedidaIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId, UUID unidadeMedidaId);

    Optional<ProdutoUnidade> findByProdutoIdAndUnidadePadraoTrueAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);
}
