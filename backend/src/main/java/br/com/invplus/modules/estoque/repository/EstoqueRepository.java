package br.com.invplus.modules.estoque.repository;

import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acesso aos saldos atuais de estoque.
 */
public interface EstoqueRepository extends BaseRepository<Estoque> {

    Optional<Estoque> findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(UUID id, UUID empresaId);

    boolean existsByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            UUID empresaId,
            UUID depositoId,
            UUID localizacaoId
    );

    Optional<Estoque> findByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
            UUID produtoId,
            UUID empresaId,
            UUID depositoId,
            UUID localizacaoId
    );

    @Query("""
            select estoque
            from Estoque estoque
            where estoque.produto.id = :produtoId
              and estoque.empresa.id = :empresaId
              and estoque.ativo = true
              and estoque.deletedAt is null
              and estoque.quantidadeDisponivel >= :quantidade
            order by
              case when estoque.deposito.principal = true then 0 else 1 end,
              case when estoque.localizacao.principal = true then 0 else 1 end,
              estoque.quantidadeDisponivel desc
            """)
    List<Estoque> findDisponiveisParaReserva(
            @Param("produtoId") UUID produtoId,
            @Param("empresaId") UUID empresaId,
            @Param("quantidade") BigDecimal quantidade
    );
}
