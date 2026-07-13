package br.com.invplus.modules.produtos.repository;

import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio do historico de precos comerciais de Produto.
 */
public interface ProdutoPrecoRepository extends BaseRepository<ProdutoPreco> {

    List<ProdutoPreco> findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(UUID produtoId);

    List<ProdutoPreco> findByProdutoIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId, UUID empresaId);

    Optional<ProdutoPreco> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    Optional<ProdutoPreco> findByProdutoIdAndTipoPrecoAndEmpresaIdAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            TipoPrecoProduto tipoPreco,
            UUID empresaId,
            LocalDate vigenciaInicio
    );

    Optional<ProdutoPreco> findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndVigenciaInicioAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            TipoPrecoProduto tipoPreco,
            LocalDate vigenciaInicio
    );

    Optional<ProdutoPreco> findByProdutoIdAndTipoPrecoAndEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            TipoPrecoProduto tipoPreco,
            UUID empresaId
    );

    Optional<ProdutoPreco> findByProdutoIdAndTipoPrecoAndEmpresaIsNullAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            TipoPrecoProduto tipoPreco
    );

    @Query("""
            select p
            from ProdutoPreco p
            where p.produto.id = :produtoId
              and p.tipoPreco = :tipoPreco
              and p.empresa.id = :empresaId
              and p.ativo = true
              and p.deletedAt is null
              and p.vigenciaInicio <= :data
              and (p.vigenciaFim is null or p.vigenciaFim >= :data)
            order by p.principal desc, p.vigenciaInicio desc
            """)
    List<ProdutoPreco> buscarPrecosVigentes(
            @Param("produtoId") UUID produtoId,
            @Param("empresaId") UUID empresaId,
            @Param("tipoPreco") TipoPrecoProduto tipoPreco,
            @Param("data") LocalDate data
    );
}
