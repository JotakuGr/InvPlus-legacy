package br.com.invplus.modules.vendas.dto.response;

import br.com.invplus.modules.estoque.dto.response.ReservaEstoqueResponse;
import br.com.invplus.modules.financeiro.dto.response.ContaReceberResponse;
import br.com.invplus.modules.financeiro.dto.response.ParcelaReceberResponse;
import java.util.List;

/**
 * Visao agregada inicial do pedido de venda para telas comerciais.
 */
public record PedidoVendaCompletoResponse(
        PedidoVendaResponse pedido,
        List<PedidoVendaItemResponse> itens,
        VendaFaturamentoResponse faturamento,
        ContaReceberResponse contaReceber,
        List<ParcelaReceberResponse> parcelasReceber,
        List<ReservaEstoqueResponse> reservas,
        List<PedidoVendaHistoricoResponse> historico
) {
}
