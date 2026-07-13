package br.com.invplus.modules.vendas.service;

import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.mapper.ReservaEstoqueMapper;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.mapper.ContaReceberMapper;
import br.com.invplus.modules.financeiro.mapper.ParcelaReceberMapper;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaReceberRepository;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaCompletoResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.VendaFaturamento;
import br.com.invplus.modules.vendas.mapper.PedidoVendaHistoricoMapper;
import br.com.invplus.modules.vendas.mapper.PedidoVendaItemMapper;
import br.com.invplus.modules.vendas.mapper.PedidoVendaMapper;
import br.com.invplus.modules.vendas.mapper.VendaFaturamentoMapper;
import br.com.invplus.modules.vendas.repository.PedidoVendaHistoricoRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaItemRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.modules.vendas.repository.VendaFaturamentoRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.security.context.EmpresaContext;
import java.util.List;
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
    private final VendaFaturamentoRepository faturamentoRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final ParcelaReceberRepository parcelaReceberRepository;
    private final PedidoVendaMapper pedidoMapper;
    private final PedidoVendaItemMapper itemMapper;
    private final ReservaEstoqueMapper reservaMapper;
    private final PedidoVendaHistoricoMapper historicoMapper;
    private final VendaFaturamentoMapper faturamentoMapper;
    private final ContaReceberMapper contaReceberMapper;
    private final ParcelaReceberMapper parcelaReceberMapper;
    private final EmpresaContext empresaContext;

    public VendaAggregateService(
            PedidoVendaRepository pedidoRepository,
            PedidoVendaItemRepository itemRepository,
            ReservaEstoqueRepository reservaRepository,
            PedidoVendaHistoricoRepository historicoRepository,
            VendaFaturamentoRepository faturamentoRepository,
            ContaReceberRepository contaReceberRepository,
            ParcelaReceberRepository parcelaReceberRepository,
            PedidoVendaMapper pedidoMapper,
            PedidoVendaItemMapper itemMapper,
            ReservaEstoqueMapper reservaMapper,
            PedidoVendaHistoricoMapper historicoMapper,
            VendaFaturamentoMapper faturamentoMapper,
            ContaReceberMapper contaReceberMapper,
            ParcelaReceberMapper parcelaReceberMapper,
            EmpresaContext empresaContext
    ) {
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
        this.reservaRepository = reservaRepository;
        this.historicoRepository = historicoRepository;
        this.faturamentoRepository = faturamentoRepository;
        this.contaReceberRepository = contaReceberRepository;
        this.parcelaReceberRepository = parcelaReceberRepository;
        this.pedidoMapper = pedidoMapper;
        this.itemMapper = itemMapper;
        this.reservaMapper = reservaMapper;
        this.historicoMapper = historicoMapper;
        this.faturamentoMapper = faturamentoMapper;
        this.contaReceberMapper = contaReceberMapper;
        this.parcelaReceberMapper = parcelaReceberMapper;
        this.empresaContext = empresaContext;
    }

    @Transactional(readOnly = true)
    public PedidoVendaCompletoResponse buscarPedido(UUID pedidoId) {
        UUID empresaId = empresaContext.getEmpresaId();
        PedidoVenda pedido = pedidoRepository
                .findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedidoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venda nao encontrado"));
        VendaFaturamento faturamento = faturamentoRepository
                .findByPedidoVendaIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(pedidoId, empresaId)
                .orElse(null);
        ContaReceber contaReceber = buscarContaReceber(faturamento);
        return new PedidoVendaCompletoResponse(
                pedidoMapper.toResponse(pedido),
                itemRepository.findByPedidoVendaIdAndAtivoTrueAndDeletedAtIsNull(pedidoId)
                        .stream().map(itemMapper::toResponse).toList(),
                faturamento == null ? null : faturamentoMapper.toResponse(faturamento),
                contaReceber == null ? null : contaReceberMapper.toResponse(contaReceber),
                contaReceber == null
                        ? List.of()
                        : parcelaReceberRepository.findByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(contaReceber.getId())
                                .stream().map(parcelaReceberMapper::toResponse).toList(),
                reservaRepository.findByOrigemAndOrigemIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                                MovimentacaoOrigem.VENDA, pedidoId, empresaId)
                        .stream().map(reservaMapper::toResponse).toList(),
                historicoRepository.findByPedidoVendaIdOrderByCriadoEmDesc(pedidoId)
                        .stream().map(historicoMapper::toResponse).toList()
        );
    }

    private ContaReceber buscarContaReceber(VendaFaturamento faturamento) {
        if (faturamento == null) {
            return null;
        }
        if (faturamento.getContaReceber() != null) {
            return faturamento.getContaReceber();
        }
        return contaReceberRepository.findByVendaFaturamentoIdAndAtivoTrueAndDeletedAtIsNull(faturamento.getId())
                .orElse(null);
    }
}
