package br.com.invplus.modules.produtos.repository;

import br.com.invplus.modules.produtos.entity.ProdutoImagem;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio do vinculo ProdutoImagem com consultas por registros ativos.
 */
public interface ProdutoImagemRepository extends BaseRepository<ProdutoImagem> {

    boolean existsByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId, UUID arquivoId);

    boolean existsByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    List<ProdutoImagem> findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    Optional<ProdutoImagem> findByProdutoIdAndArquivoIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId, UUID arquivoId);

    Optional<ProdutoImagem> findByProdutoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);
}
