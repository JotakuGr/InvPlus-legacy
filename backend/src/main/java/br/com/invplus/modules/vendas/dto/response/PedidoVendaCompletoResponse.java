package br.com.invplus.modules.vendas.dto.response;

import br.com.invplus.modules.estoque.dto.response.ReservaEstoqueResponse;
import java.util.List;

/**
 * Visao agregada inicial do pedido de venda para telas comerciais.
 */
public record PedidoVendaCompletoResponse(
        PedidoVendaResponse pedido,
        List<PedidoVendaItemResponse> itens,
        List<ReservaEstoqueResponse> reservas,
        List<PedidoVendaHistoricoResponse> historico
) {
}
