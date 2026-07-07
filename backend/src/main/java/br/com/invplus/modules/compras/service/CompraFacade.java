package br.com.invplus.modules.compras.service;

import br.com.invplus.modules.compras.dto.request.EntradaMercadoriaRequest;
import br.com.invplus.modules.compras.dto.request.PedidoCompraRequest;
import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaResponse;
import br.com.invplus.modules.compras.dto.response.PedidoCompraResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * API interna para que outros modulos usem compras sem conhecer services menores.
 */
@Service
public class CompraFacade {

    private final PedidoCompraService pedidoCompraService;
    private final EntradaMercadoriaService entradaMercadoriaService;

    public CompraFacade(PedidoCompraService pedidoCompraService, EntradaMercadoriaService entradaMercadoriaService) {
        this.pedidoCompraService = pedidoCompraService;
        this.entradaMercadoriaService = entradaMercadoriaService;
    }

    public PedidoCompraResponse criarPedido(PedidoCompraRequest request) {
        return pedidoCompraService.criar(request);
    }

    public PedidoCompraResponse aprovarPedido(UUID pedidoId) {
        return pedidoCompraService.aprovar(pedidoId);
    }

    public PedidoCompraResponse cancelarPedido(UUID pedidoId) {
        return pedidoCompraService.cancelar(pedidoId);
    }

    public EntradaMercadoriaResponse registrarEntrada(EntradaMercadoriaRequest request) {
        return entradaMercadoriaService.criar(request);
    }

    public EntradaMercadoriaResponse efetivarEntrada(UUID entradaId) {
        return entradaMercadoriaService.efetivar(entradaId);
    }
}
