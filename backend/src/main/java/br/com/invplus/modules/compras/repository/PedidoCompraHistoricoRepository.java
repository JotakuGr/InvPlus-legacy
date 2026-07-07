package br.com.invplus.modules.compras.repository;

import br.com.invplus.modules.compras.entity.PedidoCompraHistorico;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consulta o historico de transicoes do pedido de compra.
 */
public interface PedidoCompraHistoricoRepository extends JpaRepository<PedidoCompraHistorico, UUID> {

    List<PedidoCompraHistorico> findByPedidoCompraIdOrderByDataAlteracaoDesc(UUID pedidoCompraId);
}
