package br.com.invplus.modules.vendas.mapper;

import br.com.invplus.modules.vendas.dto.response.PedidoVendaHistoricoResponse;
import br.com.invplus.modules.vendas.entity.PedidoVendaHistorico;
import org.springframework.stereotype.Component;

/**
 * Converte historico de pedido de venda para DTO de leitura.
 */
@Component
public class PedidoVendaHistoricoMapper {

    public PedidoVendaHistoricoResponse toResponse(PedidoVendaHistorico entity) {
        return new PedidoVendaHistoricoResponse(
                entity.getId(),
                entity.getPedidoVenda().getId(),
                entity.getStatusAnterior(),
                entity.getStatusNovo(),
                entity.getObservacao(),
                entity.getCriadoPor(),
                entity.getCriadoEm()
        );
    }
}
