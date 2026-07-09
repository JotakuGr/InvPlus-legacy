package br.com.invplus.modules.vendas.service;

import br.com.invplus.modules.estoque.entity.ReservaEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.modules.estoque.service.ReservaEstoqueService;
import br.com.invplus.modules.financeiro.dto.response.ContaReceberResponse;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.events.ContaReceberGeradaPorVendaEvent;
import br.com.invplus.modules.financeiro.service.ContaReceberService;
import br.com.invplus.modules.vendas.dto.request.VendaFaturamentoRequest;
import br.com.invplus.modules.vendas.dto.response.VendaFaturamentoResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.VendaFaturamento;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.enums.StatusVendaFaturamento;
import br.com.invplus.modules.vendas.events.PedidoVendaFaturadoEvent;
import br.com.invplus.modules.vendas.events.VendaFaturamentoCriadoEvent;
import br.com.invplus.modules.vendas.mapper.VendaFaturamentoMapper;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.modules.vendas.repository.VendaFaturamentoRepository;
import br.com.invplus.modules.vendas.specification.VendaFaturamentoSpecification;
import br.com.invplus.modules.vendas.validator.VendaFaturamentoValidator;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra faturamento da venda, baixa de reservas e transicao do pedido para FATURADO.
 */
@Service
public class VendaFaturamentoService extends BaseCrudService<VendaFaturamento, VendaFaturamentoRequest, VendaFaturamentoResponse> {

    private final VendaFaturamentoRepository repository;
    private final PedidoVendaRepository pedidoRepository;
    private final ReservaEstoqueRepository reservaRepository;
    private final ReservaEstoqueService reservaService;
    private final ContaReceberService contaReceberService;
    private final VendaFaturamentoMapper mapper;
    private final VendaFaturamentoValidator validator;
    private final VendaHistoricoService historicoService;
    private final ApplicationEventPublisher eventPublisher;

    public VendaFaturamentoService(
            VendaFaturamentoRepository repository,
            PedidoVendaRepository pedidoRepository,
            ReservaEstoqueRepository reservaRepository,
            ReservaEstoqueService reservaService,
            ContaReceberService contaReceberService,
            VendaFaturamentoMapper mapper,
            VendaFaturamentoValidator validator,
            VendaHistoricoService historicoService,
            ApplicationEventPublisher eventPublisher
    ) {
        super(repository, mapper, "Faturamento de venda");
        this.repository = repository;
        this.pedidoRepository = pedidoRepository;
        this.reservaRepository = reservaRepository;
        this.reservaService = reservaService;
        this.contaReceberService = contaReceberService;
        this.mapper = mapper;
        this.validator = validator;
        this.historicoService = historicoService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<VendaFaturamentoResponse> listarComFiltros(
            UUID pedidoVendaId,
            UUID empresaId,
            UUID filialId,
            UUID clienteId,
            String numero,
            StatusVendaFaturamento status,
            Boolean ativo,
            Pageable pageable
    ) {
        Specification<VendaFaturamento> specification = VendaFaturamentoSpecification.naoDeletado();
        specification = and(specification, VendaFaturamentoSpecification.ativo(ativo));
        specification = and(specification, VendaFaturamentoSpecification.pedidoIgual(pedidoVendaId));
        specification = and(specification, VendaFaturamentoSpecification.empresaIgual(empresaId));
        specification = and(specification, VendaFaturamentoSpecification.filialIgual(filialId));
        specification = and(specification, VendaFaturamentoSpecification.clienteIgual(clienteId));
        specification = and(specification, VendaFaturamentoSpecification.numeroContem(numero));
        specification = and(specification, VendaFaturamentoSpecification.statusIgual(status));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Transactional
    public VendaFaturamentoResponse faturar(VendaFaturamentoRequest request) {
        PedidoVenda pedido = buscarPedido(request.pedidoVendaId());
        validator.validarFaturamento(request, pedido);
        List<ReservaEstoque> reservas = reservaRepository.findByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA,
                pedido.getId(),
                StatusReservaEstoque.ATIVA
        );

        VendaFaturamento faturamento = new VendaFaturamento();
        faturamento.faturar(pedido, request.numero(), request.observacao());
        VendaFaturamento salvo = repository.save(faturamento);

        reservas.forEach(reserva -> reservaService.baixar(reserva.getId()));

        StatusPedidoVenda anterior = pedido.getStatus();
        pedido.setStatus(StatusPedidoVenda.FATURADO);
        historicoService.registrarPedido(pedido, anterior, pedido.getStatus(), "Pedido de venda faturado");

        ContaReceberResponse contaResponse = contaReceberService.gerarPorVendaFaturamento(
                salvo,
                request.condicaoPagamentoId(),
                request.descricaoFinanceira()
        );
        ContaReceber contaReceber = contaReceberService.buscarEntidade(contaResponse.id());
        salvo.vincularContaReceber(contaReceber);

        eventPublisher.publishEvent(new VendaFaturamentoCriadoEvent(
                salvo.getId(),
                pedido.getId(),
                pedido.getEmpresa().getId(),
                pedido.getCliente().getId(),
                salvo.getValorTotal()
        ));
        eventPublisher.publishEvent(new PedidoVendaFaturadoEvent(
                pedido.getId(),
                salvo.getId(),
                pedido.getEmpresa().getId(),
                pedido.getCliente().getId(),
                salvo.getValorTotal()
        ));
        eventPublisher.publishEvent(new ContaReceberGeradaPorVendaEvent(
                salvo.getId(),
                contaResponse.id(),
                pedido.getId(),
                pedido.getCliente().getId(),
                salvo.getValorTotal()
        ));
        return mapper.toResponse(salvo);
    }

    @Transactional
    public VendaFaturamentoResponse faturarPedido(UUID pedidoVendaId) {
        throw new InvalidOperationException("Condicao de pagamento e obrigatoria para faturar pedido");
    }

    @Transactional
    public VendaFaturamentoResponse faturarPedido(UUID pedidoVendaId, UUID condicaoPagamentoId) {
        PedidoVenda pedido = buscarPedido(pedidoVendaId);
        return faturar(new VendaFaturamentoRequest(
                pedidoVendaId,
                condicaoPagamentoId,
                pedido.getNumero(),
                "Conta a receber do pedido " + pedido.getNumero(),
                "Faturamento do pedido " + pedido.getNumero()
        ));
    }

    @Transactional
    public VendaFaturamentoResponse cancelar(UUID id) {
        VendaFaturamento faturamento = buscarEntidade(id);
        validator.validarCancelamento(faturamento);
        faturamento.cancelar("Faturamento de venda cancelado");
        return mapper.toResponse(faturamento);
    }

    private PedidoVenda buscarPedido(UUID id) {
        return pedidoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venda nao encontrado"));
    }

    private Specification<VendaFaturamento> and(Specification<VendaFaturamento> base, Specification<VendaFaturamento> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
