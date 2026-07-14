package br.com.invplus.modules.compras.service;

import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaCompletaResponse;
import br.com.invplus.modules.compras.dto.response.PedidoCompraCompletoResponse;
import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.modules.compras.mapper.EntradaMercadoriaHistoricoMapper;
import br.com.invplus.modules.compras.mapper.EntradaMercadoriaItemMapper;
import br.com.invplus.modules.compras.mapper.EntradaMercadoriaMapper;
import br.com.invplus.modules.compras.mapper.PedidoCompraHistoricoMapper;
import br.com.invplus.modules.compras.mapper.PedidoCompraItemMapper;
import br.com.invplus.modules.compras.mapper.PedidoCompraMapper;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaHistoricoRepository;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaItemRepository;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaRepository;
import br.com.invplus.modules.compras.repository.PedidoCompraHistoricoRepository;
import br.com.invplus.modules.compras.repository.PedidoCompraItemRepository;
import br.com.invplus.modules.compras.repository.PedidoCompraRepository;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.mapper.MovimentacaoEstoqueMapper;
import br.com.invplus.modules.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monta leituras completas de compras para evitar varias chamadas do frontend.
 */
@Service
public class CompraAggregateService {

    private final PedidoCompraRepository pedidoRepository;
    private final PedidoCompraItemRepository pedidoItemRepository;
    private final EntradaMercadoriaRepository entradaRepository;
    private final EntradaMercadoriaItemRepository entradaItemRepository;
    private final PedidoCompraHistoricoRepository pedidoHistoricoRepository;
    private final EntradaMercadoriaHistoricoRepository entradaHistoricoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final PedidoCompraMapper pedidoMapper;
    private final PedidoCompraItemMapper pedidoItemMapper;
    private final EntradaMercadoriaMapper entradaMapper;
    private final EntradaMercadoriaItemMapper entradaItemMapper;
    private final PedidoCompraHistoricoMapper pedidoHistoricoMapper;
    private final EntradaMercadoriaHistoricoMapper entradaHistoricoMapper;
    private final MovimentacaoEstoqueMapper movimentacaoMapper;

    public CompraAggregateService(
            PedidoCompraRepository pedidoRepository,
            PedidoCompraItemRepository pedidoItemRepository,
            EntradaMercadoriaRepository entradaRepository,
            EntradaMercadoriaItemRepository entradaItemRepository,
            PedidoCompraHistoricoRepository pedidoHistoricoRepository,
            EntradaMercadoriaHistoricoRepository entradaHistoricoRepository,
            MovimentacaoEstoqueRepository movimentacaoRepository,
            PedidoCompraMapper pedidoMapper,
            PedidoCompraItemMapper pedidoItemMapper,
            EntradaMercadoriaMapper entradaMapper,
            EntradaMercadoriaItemMapper entradaItemMapper,
            PedidoCompraHistoricoMapper pedidoHistoricoMapper,
            EntradaMercadoriaHistoricoMapper entradaHistoricoMapper,
            MovimentacaoEstoqueMapper movimentacaoMapper
    ) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
        this.entradaRepository = entradaRepository;
        this.entradaItemRepository = entradaItemRepository;
        this.pedidoHistoricoRepository = pedidoHistoricoRepository;
        this.entradaHistoricoRepository = entradaHistoricoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.pedidoMapper = pedidoMapper;
        this.pedidoItemMapper = pedidoItemMapper;
        this.entradaMapper = entradaMapper;
        this.entradaItemMapper = entradaItemMapper;
        this.pedidoHistoricoMapper = pedidoHistoricoMapper;
        this.entradaHistoricoMapper = entradaHistoricoMapper;
        this.movimentacaoMapper = movimentacaoMapper;
    }

    @Transactional(readOnly = true)
    public PedidoCompraCompletoResponse buscarPedido(UUID pedidoId) {
        PedidoCompra pedido = pedidoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
        var itens = pedidoItemRepository.findByPedidoCompraIdAndAtivoTrueAndDeletedAtIsNull(pedidoId);
        BigDecimal quantidadeTotal = itens.stream()
                .map(item -> item.getQuantidade() == null ? BigDecimal.ZERO : item.getQuantidade())
                .reduce(BigDecimal.ZERO, (total, quantidade) -> total.add(quantidade));

        return new PedidoCompraCompletoResponse(
                pedidoMapper.toResponse(pedido),
                itens.stream().map(pedidoItemMapper::toResponse).toList(),
                quantidadeTotal,
                pedido.getValorTotal(),
                entradaRepository.findByPedidoCompraIdAndAtivoTrueAndDeletedAtIsNull(pedidoId)
                        .stream().map(entradaMapper::toResponse).toList(),
                pedidoHistoricoRepository.findByPedidoCompraIdOrderByDataAlteracaoDesc(pedidoId)
                        .stream().map(pedidoHistoricoMapper::toResponse).toList()
        );
    }

    @Transactional(readOnly = true)
    public EntradaMercadoriaCompletaResponse buscarEntrada(UUID entradaId) {
        EntradaMercadoria entrada = entradaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(entradaId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrada de mercadoria nao encontrada"));

        return new EntradaMercadoriaCompletaResponse(
                entradaMapper.toResponse(entrada),
                entradaItemRepository.findByEntradaMercadoriaIdAndAtivoTrueAndDeletedAtIsNull(entradaId)
                        .stream().map(entradaItemMapper::toResponse).toList(),
                movimentacaoRepository.findByOrigemAndOrigemIdOrderByDataMovimentacaoDesc(MovimentacaoOrigem.COMPRA, entradaId)
                        .stream().map(movimentacaoMapper::toResponse).toList(),
                entradaHistoricoRepository.findByEntradaMercadoriaIdOrderByDataAlteracaoDesc(entradaId)
                        .stream().map(entradaHistoricoMapper::toResponse).toList()
        );
    }
}
