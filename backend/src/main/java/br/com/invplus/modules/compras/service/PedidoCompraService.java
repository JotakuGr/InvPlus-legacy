package br.com.invplus.modules.compras.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.compras.dto.request.PedidoCompraRequest;
import br.com.invplus.modules.compras.dto.response.PedidoCompraResponse;
import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.modules.compras.enums.StatusPedidoCompra;
import br.com.invplus.modules.compras.events.PedidoCompraAprovadoEvent;
import br.com.invplus.modules.compras.events.PedidoCompraCriadoEvent;
import br.com.invplus.modules.compras.mapper.PedidoCompraMapper;
import br.com.invplus.modules.compras.repository.PedidoCompraItemRepository;
import br.com.invplus.modules.compras.repository.PedidoCompraRepository;
import br.com.invplus.modules.compras.specification.PedidoCompraSpecification;
import br.com.invplus.modules.compras.validator.PedidoCompraValidator;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.service.UsuarioEmpresaAccessValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra o ciclo de vida do pedido de compra.
 */
@Service
public class PedidoCompraService extends BaseCrudService<PedidoCompra, PedidoCompraRequest, PedidoCompraResponse> {

    private final PedidoCompraRepository repository;
    private final PedidoCompraMapper mapper;
    private final PedidoCompraValidator validator;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final FornecedorRepository fornecedorRepository;
    private final PedidoCompraItemRepository itemRepository;
    private final CompraHistoricoService historicoService;
    private final ApplicationEventPublisher eventPublisher;
    private final EmpresaContext empresaContext;
    private final UsuarioEmpresaAccessValidator accessValidator;

    public PedidoCompraService(
            PedidoCompraRepository repository,
            PedidoCompraMapper mapper,
            PedidoCompraValidator validator,
            EmpresaRepository empresaRepository,
            FilialRepository filialRepository,
            FornecedorRepository fornecedorRepository,
            PedidoCompraItemRepository itemRepository,
            CompraHistoricoService historicoService,
            ApplicationEventPublisher eventPublisher,
            EmpresaContext empresaContext,
            UsuarioEmpresaAccessValidator accessValidator
    ) {
        super(repository, mapper, "Pedido de compra");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.empresaRepository = empresaRepository;
        this.filialRepository = filialRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.itemRepository = itemRepository;
        this.historicoService = historicoService;
        this.eventPublisher = eventPublisher;
        this.empresaContext = empresaContext;
        this.accessValidator = accessValidator;
    }

    @Transactional(readOnly = true)
    public Page<PedidoCompraResponse> listarComFiltros(UUID fornecedorId, StatusPedidoCompra status,
                                                       String numero, Boolean ativo, Pageable pageable) {
        UUID empresaId = empresaContext.getEmpresaId();
        Specification<PedidoCompra> specification = PedidoCompraSpecification.pertenceEmpresa(empresaId)
                .and(PedidoCompraSpecification.naoDeletado())
                .and(PedidoCompraSpecification.ativo(ativo))
                .and(PedidoCompraSpecification.fornecedorIgual(fornecedorId))
                .and(PedidoCompraSpecification.statusIgual(status))
                .and(PedidoCompraSpecification.numeroContem(numero));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoCompraResponse buscar(UUID id) {
        return mapper.toResponse(buscarNoContexto(id));
    }

    @Override
    @Transactional
    public PedidoCompraResponse criar(PedidoCompraRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        validator.validarCriacao(request, empresaId);
        PedidoCompra entity = mapper.toEntity(request);
        associar(entity, request);
        PedidoCompra salvo = repository.save(entity);
        historicoService.registrarPedido(salvo, null, salvo.getStatus(), "Pedido de compra criado");
        eventPublisher.publishEvent(new PedidoCompraCriadoEvent(
                salvo.getId(),
                salvo.getEmpresa().getId(),
                salvo.getFornecedor().getId(),
                salvo.getValorTotal()
        ));
        return mapper.toResponse(salvo);
    }

    @Override
    @Transactional
    public PedidoCompraResponse atualizar(UUID id, PedidoCompraRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        PedidoCompra entity = buscarNoContexto(id);
        validator.validarAtualizacao(entity, request, empresaId);
        mapper.updateEntity(entity, request);
        associar(entity, request);
        return mapper.toResponse(entity);
    }

    @Transactional
    public PedidoCompraResponse aprovar(UUID id) {
        PedidoCompra entity = buscarNoContexto(id);
        StatusPedidoCompra anterior = entity.getStatus();
        UUID usuarioId = historicoService.usuarioAtual().orElse(null);
        entity.aprovar(usuarioId);
        historicoService.registrarPedido(entity, anterior, entity.getStatus(), "Pedido de compra aprovado");
        eventPublisher.publishEvent(new PedidoCompraAprovadoEvent(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getFornecedor().getId(),
                entity.getAprovadoPor()
        ));
        return mapper.toResponse(entity);
    }

    @Transactional
    public PedidoCompraResponse cancelar(UUID id) {
        PedidoCompra entity = buscarNoContexto(id);
        StatusPedidoCompra anterior = entity.getStatus();
        entity.cancelar();
        historicoService.registrarPedido(entity, anterior, entity.getStatus(), "Pedido de compra cancelado");
        return mapper.toResponse(entity);
    }

    @Transactional
    public void recalcularTotal(PedidoCompra pedido) {
        BigDecimal total = itemRepository.somarValorTotalPorPedido(pedido.getId());
        pedido.atualizarValorTotal(total);
    }

    @Transactional
    public void marcarRecebimento(PedidoCompra pedido, boolean parcial) {
        if (pedido != null) {
            StatusPedidoCompra anterior = pedido.getStatus();
            pedido.marcarRecebido(parcial);
            historicoService.registrarPedido(pedido, anterior, pedido.getStatus(), "Pedido atualizado por entrada de mercadoria");
        }
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        buscarNoContexto(id).excluirLogicamente();
    }

    private void associar(PedidoCompra entity, PedidoCompraRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        entity.setEmpresa(buscarEmpresa(empresaId));
        entity.setFilial(buscarFilial(request.filialId(), empresaId));
        entity.setFornecedor(buscarFornecedor(request.fornecedorId()));
    }

    private Empresa buscarEmpresa(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    private Filial buscarFilial(UUID id, UUID empresaId) {
        if (id == null) {
            return null;
        }
        Filial filial = filialRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Filial nao encontrada"));
        accessValidator.validarContexto(empresaContext.getUsuarioId(), empresaId, id);
        return filial;
    }

    private Fornecedor buscarFornecedor(UUID id) {
        return fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado"));
    }

    private PedidoCompra buscarNoContexto(UUID id) {
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
    }
}
