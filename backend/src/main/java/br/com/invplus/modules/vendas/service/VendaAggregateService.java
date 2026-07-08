package br.com.invplus.modules.vendas.service;

import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.mapper.ReservaEstoqueMapper;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaCompletoResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.mapper.PedidoVendaHistoricoMapper;
import br.com.invplus.modules.vendas.mapper.PedidoVendaItemMapper;
import br.com.invplus.modules.vendas.mapper.PedidoVendaMapper;
import br.com.invplus.modules.vendas.repository.PedidoVendaHistoricoRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaItemRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monta leituras agregadas do modulo Vendas para reduzir chamadas do frontend.
 */
@Service
public class VendaAggregateService {

    private final PedidoVendaRepository pedidoRepository;
    private final PedidoVendaItemRepository itemRepository;
    private final ReservaEstoqueRepository reservaRepository;
    private final PedidoVendaHistoricoRepository historicoRepository;
    private final PedidoVendaMapper pedidoMapper;
    private final PedidoVendaItemMapper itemMapper;
    private final ReservaEstoqueMapper reservaMapper;
    private final PedidoVendaHistoricoMapper historicoMapper;

    public VendaAggregateService(
            PedidoVendaRepository pedidoRepository,
            PedidoVendaItemRepository itemRepository,
            ReservaEstoqueRepository reservaRepository,
            PedidoVendaHistoricoRepository historicoRepository,
            PedidoVendaMapper pedidoMapper,
            PedidoVendaItemMapper itemMapper,
            ReservaEstoqueMapper reservaMapper,
            PedidoVendaHistoricoMapper historicoMapper
    ) {
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
        this.reservaRepository = reservaRepository;
        this.historicoRepository = historicoRepository;
        this.pedidoMapper = pedidoMapper;
        this.itemMapper = itemMapper;
        this.reservaMapper = reservaMapper;
        this.historicoMapper = historicoMapper;
    }

    @Transactional(readOnly = true)
    public PedidoVendaCompletoResponse buscarPedido(UUID pedidoId) {
        PedidoVenda pedido = pedidoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venda nao encontrado"));
        return new PedidoVendaCompletoResponse(
                pedidoMapper.toResponse(pedido),
                itemRepository.findByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(pedidoId)
                        .stream().map(itemMapper::toResponse).toList(),
                reservaRepository.findByOrigemAndOrigemIdAndAtivoTrueAndDeletedAtIsNull(MovimentacaoOrigem.VENDA, pedidoId)
                        .stream().map(reservaMapper::toResponse).toList(),
                historicoRepository.findByPedidoVendaIdOrderByCriadoEmDesc(pedidoId)
                        .stream().map(historicoMapper::toResponse).toList()
        );
    }
}
