package br.com.invplus.modules.vendas.repository;

import br.com.invplus.modules.vendas.entity.PedidoVendaItem;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistencia dos itens de pedido de venda.
 */
public interface PedidoVendaItemRepository extends BaseRepository<PedidoVendaItem> {

    List<PedidoVendaItem> findByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(UUID pedidoVendaId);

    @Query("""
            select coalesce(sum(item.valorTotal), 0)
            from PedidoVendaItem item
            where item.pedidoVenda.id = :pedidoId
              and item.ativo = true
              and item.deletedAt is null
            """)
    BigDecimal somarValorTotal(@Param("pedidoId") UUID pedidoId);
}
