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
import br.com.invplus.security.context.EmpresaContext;
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
    private final EmpresaContext empresaContext;

    public VendaFaturamentoService(
            VendaFaturamentoRepository repository,
            PedidoVendaRepository pedidoRepository,
            ReservaEstoqueRepository reservaRepository,
            ReservaEstoqueService reservaService,
            ContaReceberService contaReceberService,
            VendaFaturamentoMapper mapper,
            VendaFaturamentoValidator validator,
            VendaHistoricoService historicoService,
            ApplicationEventPublisher eventPublisher,
            EmpresaContext empresaContext
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
        this.empresaContext = empresaContext;
    }

    @Transactional(readOnly = true)
    public Page<VendaFaturamentoResponse> listarComFiltros(
            UUID pedidoVendaId,
            UUID filialId,
            UUID clienteId,
            String numero,
            StatusVendaFaturamento status,
            Boolean ativo,
            Pageable pageable
    ) {
        Specification<VendaFaturamento> specification = VendaFaturamentoSpecification
                .pertenceEmpresa(empresaContext.getEmpresaId())
                .and(VendaFaturamentoSpecification.naoDeletado())
                .and(VendaFaturamentoSpecification.ativo(ativo))
                .and(VendaFaturamentoSpecification.pedidoIgual(pedidoVendaId))
                .and(VendaFaturamentoSpecification.filialIgual(filialId))
                .and(VendaFaturamentoSpecification.clienteIgual(clienteId))
                .and(VendaFaturamentoSpecification.numeroContem(numero))
                .and(VendaFaturamentoSpecification.statusIgual(status));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Transactional
    public VendaFaturamentoResponse faturar(VendaFaturamentoRequest request) {
        PedidoVenda pedido = buscarPedido(request.pedidoVendaId());
        UUID empresaId = empresaContext.getEmpresaId();
        validator.validarFaturamento(request, pedido, empresaId);
        List<ReservaEstoque> reservas = reservaRepository
                .findByOrigemAndOrigemIdAndStatusAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA,
                pedido.getId(),
                StatusReservaEstoque.ATIVA,
                empresaId
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
        return pedidoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venda nao encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public VendaFaturamentoResponse buscar(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Override
    @Transactional(readOnly = true)
    public VendaFaturamento buscarEntidade(UUID id) {
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Faturamento de venda nao encontrado"));
    }
}
