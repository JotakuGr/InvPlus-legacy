package br.com.invplus.modules.produtos.repository;

import br.com.invplus.modules.produtos.entity.ProdutoFiscal;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio dos dados fiscais ativos de Produto.
 */
public interface ProdutoFiscalRepository extends BaseRepository<ProdutoFiscal> {

    boolean existsByProdutoIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    Optional<ProdutoFiscal> findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);
}
