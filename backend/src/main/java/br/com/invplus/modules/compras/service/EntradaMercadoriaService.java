package br.com.invplus.modules.compras.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.compras.dto.request.EntradaMercadoriaRequest;
import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaResponse;
import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.compras.entity.EntradaMercadoriaItem;
import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.modules.compras.enums.StatusEntradaMercadoria;
import br.com.invplus.modules.compras.events.EntradaMercadoriaEfetivadaEvent;
import br.com.invplus.modules.compras.mapper.EntradaMercadoriaMapper;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaItemRepository;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaRepository;
import br.com.invplus.modules.compras.repository.PedidoCompraRepository;
import br.com.invplus.modules.compras.specification.EntradaMercadoriaSpecification;
import br.com.invplus.modules.compras.validator.EntradaMercadoriaValidator;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.service.EstoqueFacade;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra entradas de mercadoria e a efetivacao no estoque.
 */
@Service
public class EntradaMercadoriaService extends BaseCrudService<EntradaMercadoria, EntradaMercadoriaRequest, EntradaMercadoriaResponse> {

    private final EntradaMercadoriaRepository repository;
    private final EntradaMercadoriaMapper mapper;
    private final EntradaMercadoriaValidator validator;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final FornecedorRepository fornecedorRepository;
    private final PedidoCompraRepository pedidoCompraRepository;
    private final EntradaMercadoriaItemRepository itemRepository;
    private final EstoqueRepository estoqueRepository;
    private final EstoqueFacade estoqueFacade;
    private final PedidoCompraService pedidoCompraService;
    private final CompraHistoricoService historicoService;
    private final ApplicationEventPublisher eventPublisher;

    public EntradaMercadoriaService(
            EntradaMercadoriaRepository repository,
            EntradaMercadoriaMapper mapper,
            EntradaMercadoriaValidator validator,
            EmpresaRepository empresaRepository,
            FilialRepository filialRepository,
            FornecedorRepository fornecedorRepository,
            PedidoCompraRepository pedidoCompraRepository,
            EntradaMercadoriaItemRepository itemRepository,
            EstoqueRepository estoqueRepository,
            EstoqueFacade estoqueFacade,
            PedidoCompraService pedidoCompraService,
            CompraHistoricoService historicoService,
            ApplicationEventPublisher eventPublisher
    ) {
        super(repository, mapper, "Entrada de mercadoria");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.empresaRepository = empresaRepository;
        this.filialRepository = filialRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.pedidoCompraRepository = pedidoCompraRepository;
        this.itemRepository = itemRepository;
        this.estoqueRepository = estoqueRepository;
        this.estoqueFacade = estoqueFacade;
        this.pedidoCompraService = pedidoCompraService;
        this.historicoService = historicoService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<EntradaMercadoriaResponse> listarComFiltros(UUID empresaId, UUID fornecedorId, UUID pedidoCompraId,
                                                            StatusEntradaMercadoria status, Boolean ativo,
                                                            Pageable pageable) {
        Specification<EntradaMercadoria> specification = EntradaMercadoriaSpecification.naoDeletado()
                .and(EntradaMercadoriaSpecification.ativo(ativo))
                .and(EntradaMercadoriaSpecification.empresaIgual(empresaId))
                .and(EntradaMercadoriaSpecification.fornecedorIgual(fornecedorId))
                .and(EntradaMercadoriaSpecification.pedidoIgual(pedidoCompraId))
                .and(EntradaMercadoriaSpecification.statusIgual(status));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public EntradaMercadoriaResponse criar(EntradaMercadoriaRequest request) {
        validator.validarCriacao(request);
        EntradaMercadoria entity = mapper.toEntity(request);
        associar(entity, request);
        EntradaMercadoria salva = repository.save(entity);
        historicoService.registrarEntrada(salva, null, salva.getStatus(), "Entrada de mercadoria criada");
        return mapper.toResponse(salva);
    }

    @Override
    @Transactional
    public EntradaMercadoriaResponse atualizar(UUID id, EntradaMercadoriaRequest request) {
        EntradaMercadoria entity = buscarEntidade(id);
        validator.validarAtualizacao(entity, request);
        mapper.updateEntity(entity, request);
        associar(entity, request);
        return mapper.toResponse(entity);
    }

    @Transactional
    public EntradaMercadoriaResponse conferir(UUID id) {
        EntradaMercadoria entity = buscarEntidade(id);
        StatusEntradaMercadoria anterior = entity.getStatus();
        entity.conferir();
        historicoService.registrarEntrada(entity, anterior, entity.getStatus(), "Entrada de mercadoria conferida");
        return mapper.toResponse(entity);
    }

    @Transactional
    public EntradaMercadoriaResponse cancelar(UUID id) {
        EntradaMercadoria entity = buscarEntidade(id);
        StatusEntradaMercadoria anterior = entity.getStatus();
        entity.cancelar();
        historicoService.registrarEntrada(entity, anterior, entity.getStatus(), "Entrada de mercadoria cancelada");
        return mapper.toResponse(entity);
    }

    @Transactional
    public EntradaMercadoriaResponse efetivar(UUID id) {
        EntradaMercadoria entrada = buscarEntidade(id);
        entrada.validarEditavel();
        StatusEntradaMercadoria anterior = entrada.getStatus();
        List<EntradaMercadoriaItem> itens = itemRepository.findByEntradaMercadoriaIdAndAtivoTrueAndDeletedAtIsNull(id);
        if (itens.isEmpty()) {
            throw new InvalidOperationException("Entrada de mercadoria sem itens nao pode ser efetivada");
        }
        for (EntradaMercadoriaItem item : itens) {
            Estoque estoque = estoqueRepository.findByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
                            item.getProduto().getId(),
                            entrada.getEmpresa().getId(),
                            item.getDeposito().getId(),
                            item.getLocalizacao().getId()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("Estoque nao encontrado para efetivar entrada"));
            estoqueFacade.entrada(estoque.getId(), item.getQuantidade(), MovimentacaoOrigem.COMPRA,
                    entrada.getId(), entrada.getNumero(), "Entrada de mercadoria");
        }
        entrada.efetivar();
        historicoService.registrarEntrada(entrada, anterior, entrada.getStatus(), "Entrada de mercadoria efetivada");
        if (entrada.getPedidoCompra() != null) {
            pedidoCompraService.marcarRecebimento(entrada.getPedidoCompra(), false);
        }
        eventPublisher.publishEvent(new EntradaMercadoriaEfetivadaEvent(
                entrada.getId(),
                entrada.getPedidoCompra() == null ? null : entrada.getPedidoCompra().getId(),
                entrada.getEmpresa().getId(),
                entrada.getFornecedor().getId(),
                entrada.getValorTotal()
        ));
        return mapper.toResponse(entrada);
    }

    @Transactional
    public void recalcularTotal(EntradaMercadoria entrada) {
        BigDecimal total = itemRepository.somarValorTotalPorEntrada(entrada.getId());
        entrada.atualizarValorTotal(total);
    }

    private void associar(EntradaMercadoria entity, EntradaMercadoriaRequest request) {
        entity.setEmpresa(buscarEmpresa(request.empresaId()));
        entity.setFilial(buscarFilial(request.filialId()));
        entity.setFornecedor(buscarFornecedor(request.fornecedorId()));
        entity.setPedidoCompra(buscarPedido(request.pedidoCompraId()));
    }

    private Empresa buscarEmpresa(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    private Filial buscarFilial(UUID id) {
        if (id == null) {
            return null;
        }
        return filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filial nao encontrada"));
    }

    private Fornecedor buscarFornecedor(UUID id) {
        return fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado"));
    }

    private PedidoCompra buscarPedido(UUID id) {
        if (id == null) {
            return null;
        }
        return pedidoCompraRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
    }
}
