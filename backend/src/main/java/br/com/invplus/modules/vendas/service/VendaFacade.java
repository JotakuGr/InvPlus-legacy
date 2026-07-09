package br.com.invplus.modules.vendas.service;

import br.com.invplus.modules.vendas.dto.request.PedidoVendaRequest;
import br.com.invplus.modules.vendas.dto.request.PedidoVendaItemRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaItemResponse;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * API interna para que outros modulos usem vendas sem conhecer services menores.
 */
@Service
public class VendaFacade {

    private final PedidoVendaService pedidoVendaService;
    private final PedidoVendaItemService pedidoVendaItemService;
    private final VendaFaturamentoService vendaFaturamentoService;

    public VendaFacade(
            PedidoVendaService pedidoVendaService,
            PedidoVendaItemService pedidoVendaItemService,
            VendaFaturamentoService vendaFaturamentoService
    ) {
        this.pedidoVendaService = pedidoVendaService;
        this.pedidoVendaItemService = pedidoVendaItemService;
        this.vendaFaturamentoService = vendaFaturamentoService;
    }

    public PedidoVendaResponse criarPedido(PedidoVendaRequest request) {
        return pedidoVendaService.criar(request);
    }

    public PedidoVendaResponse aprovarPedido(UUID pedidoId) {
        return pedidoVendaService.aprovar(pedidoId);
    }

    public PedidoVendaResponse cancelarPedido(UUID pedidoId) {
        return pedidoVendaService.cancelar(pedidoId);
    }

    public PedidoVendaResponse faturarPedido(UUID pedidoId) {
        vendaFaturamentoService.faturarPedido(pedidoId);
        return pedidoVendaService.buscar(pedidoId);
    }

    public PedidoVendaResponse faturarPedido(UUID pedidoId, UUID condicaoPagamentoId) {
        vendaFaturamentoService.faturarPedido(pedidoId, condicaoPagamentoId);
        return pedidoVendaService.buscar(pedidoId);
    }

    public PedidoVendaItemResponse adicionarItem(PedidoVendaItemRequest request) {
        return pedidoVendaItemService.criar(request);
    }

    public PedidoVendaItemResponse alterarItem(UUID itemId, PedidoVendaItemRequest request) {
        return pedidoVendaItemService.atualizar(itemId, request);
    }

    public void removerItem(UUID itemId) {
        pedidoVendaItemService.excluir(itemId);
    }
}
