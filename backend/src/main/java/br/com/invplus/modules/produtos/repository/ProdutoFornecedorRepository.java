package br.com.invplus.modules.produtos.repository;

import br.com.invplus.modules.produtos.entity.ProdutoFornecedor;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio do vinculo ProdutoFornecedor com consultas por registros ativos.
 */
public interface ProdutoFornecedorRepository extends BaseRepository<ProdutoFornecedor> {

    boolean existsByProdutoIdAndFornecedorIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId, UUID fornecedorId);

    boolean existsByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    Optional<ProdutoFornecedor> findByProdutoIdAndFornecedorIdAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            UUID fornecedorId
    );

    List<ProdutoFornecedor> findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    Optional<ProdutoFornecedor> findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);
}
