package br.com.invplus.modules.produtos.repository;

import br.com.invplus.modules.produtos.entity.ProdutoFornecedorPreco;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio do historico de precos de ProdutoFornecedor.
 */
public interface ProdutoFornecedorPrecoRepository extends BaseRepository<ProdutoFornecedorPreco> {

    Optional<ProdutoFornecedorPreco> findByProdutoFornecedorIdAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoFornecedorId,
            LocalDate vigenciaInicio
    );
}
