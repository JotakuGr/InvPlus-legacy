package br.com.invplus.modules.compras.repository;

import br.com.invplus.modules.compras.entity.PedidoCompraItem;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistencia dos itens de pedido de compra.
 */
public interface PedidoCompraItemRepository extends BaseRepository<PedidoCompraItem> {

    Optional<PedidoCompraItem> findByIdAndPedidoCompraEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
            UUID id, UUID empresaId);

    List<PedidoCompraItem> findByPedidoCompraIdAndAtivoTrueAndDeletedAtIsNull(UUID pedidoCompraId);

    @Query("""
            select coalesce(sum(item.valorTotal), 0)
            from PedidoCompraItem item
            where item.pedidoCompra.id = :pedidoId
              and item.ativo = true
              and item.deletedAt is null
            """)
    BigDecimal somarValorTotalPorPedido(@Param("pedidoId") UUID pedidoId);
}
