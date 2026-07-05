package br.com.invplus.modules.produtos.repository;

import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.shared.domain.repository.BaseRepository;

/**
 * Acesso aos produtos ativos do catalogo.
 */
public interface ProdutoRepository extends BaseRepository<Produto> {

    boolean existsByCodigoAndAtivoTrueAndDeletedAtIsNull(String codigo);

    boolean existsBySkuAndAtivoTrueAndDeletedAtIsNull(String sku);

    boolean existsByCodigoBarrasAndAtivoTrueAndDeletedAtIsNull(String codigoBarras);
}
