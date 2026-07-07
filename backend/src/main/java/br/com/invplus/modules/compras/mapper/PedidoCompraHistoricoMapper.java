package br.com.invplus.modules.compras.mapper;

import br.com.invplus.modules.compras.dto.response.PedidoCompraHistoricoResponse;
import br.com.invplus.modules.compras.entity.PedidoCompraHistorico;
import org.springframework.stereotype.Component;

/**
 * Converte historico de pedido para DTO de leitura.
 */
@Component
public class PedidoCompraHistoricoMapper {

    public PedidoCompraHistoricoResponse toResponse(PedidoCompraHistorico entity) {
        return new PedidoCompraHistoricoResponse(
                entity.getId(),
                entity.getPedidoCompra().getId(),
                entity.getStatusAnterior(),
                entity.getNovoStatus(),
                entity.getUsuarioId(),
                entity.getDataAlteracao(),
                entity.getObservacao()
        );
    }
}
