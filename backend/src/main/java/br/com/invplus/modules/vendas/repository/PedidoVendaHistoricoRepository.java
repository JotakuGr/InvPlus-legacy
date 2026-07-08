package br.com.invplus.modules.vendas.repository;

import br.com.invplus.modules.vendas.entity.PedidoVendaHistorico;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consulta o historico operacional de pedidos de venda.
 */
public interface PedidoVendaHistoricoRepository extends JpaRepository<PedidoVendaHistorico, UUID> {

    List<PedidoVendaHistorico> findByPedidoVendaIdOrderByCriadoEmDesc(UUID pedidoVendaId);
}
