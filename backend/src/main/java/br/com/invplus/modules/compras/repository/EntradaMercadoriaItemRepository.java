package br.com.invplus.modules.compras.repository;

import br.com.invplus.modules.compras.entity.EntradaMercadoriaItem;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistencia dos itens recebidos em entradas de mercadoria.
 */
public interface EntradaMercadoriaItemRepository extends BaseRepository<EntradaMercadoriaItem> {

    List<EntradaMercadoriaItem> findByEntradaMercadoriaIdAndAtivoTrueAndDeletedAtIsNull(UUID entradaMercadoriaId);

    @Query("""
            select coalesce(sum(item.valorTotal), 0)
            from EntradaMercadoriaItem item
            where item.entradaMercadoria.id = :entradaId
              and item.ativo = true
              and item.deletedAt is null
            """)
    BigDecimal somarValorTotalPorEntrada(@Param("entradaId") UUID entradaId);
}
