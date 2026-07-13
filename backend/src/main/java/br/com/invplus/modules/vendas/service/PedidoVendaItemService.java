package br.com.invplus.modules.vendas.service;

import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.produtos.repository.ProdutoPrecoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoUnidadeRepository;
import br.com.invplus.modules.vendas.dto.request.PedidoVendaItemRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaItemResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.PedidoVendaItem;
import br.com.invplus.modules.vendas.events.PedidoVendaItemAlteradoEvent;
import br.com.invplus.modules.vendas.events.PedidoVendaItemCriadoEvent;
import br.com.invplus.modules.vendas.events.PedidoVendaItemRemovidoEvent;
import br.com.invplus.modules.vendas.mapper.PedidoVendaItemMapper;
import br.com.invplus.modules.vendas.repository.PedidoVendaItemRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.modules.vendas.specification.PedidoVendaItemSpecification;
import br.com.invplus.modules.vendas.validator.PedidoVendaItemValidator;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantem itens de venda, resolve preco vigente e recalcula automaticamente o total do pedido.
 */
@Service
public class PedidoVendaItemService extends BaseCrudService<PedidoVendaItem, PedidoVendaItemRequest, PedidoVendaItemResponse> {

    private final PedidoVendaItemRepository repository;
    private final PedidoVendaItemMapper mapper;
    private final PedidoVendaItemValidator validator;
    private final PedidoVendaRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoPrecoRepository produtoPrecoRepository;
    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final PedidoVendaService pedidoVendaService;
    private final ApplicationEventPublisher eventPublisher;
    private final EmpresaContext empresaContext;

    public PedidoVendaItemService(
            PedidoVendaItemRepository repository,
            PedidoVendaItemMapper mapper,
            PedidoVendaItemValidator validator,
            PedidoVendaRepository pedidoRepository,
            ProdutoRepository produtoRepository,
            ProdutoPrecoRepository produtoPrecoRepository,
            ProdutoUnidadeRepository produtoUnidadeRepository,
            PedidoVendaService pedidoVendaService,
            ApplicationEventPublisher eventPublisher,
            EmpresaContext empresaContext
    ) {
        super(repository, mapper, "Item do pedido de venda");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.produtoPrecoRepository = produtoPrecoRepository;
        this.produtoUnidadeRepository = produtoUnidadeRepository;
        this.pedidoVendaService = pedidoVendaService;
        this.eventPublisher = eventPublisher;
        this.empresaContext = empresaContext;
    }

    @Transactional(readOnly = true)
    public Page<PedidoVendaItemResponse> listarComFiltros(UUID pedidoVendaId, UUID produtoId, Boolean ativo, Pageable pageable) {
        Specification<PedidoVendaItem> specification = PedidoVendaItemSpecification
                .pertenceEmpresa(empresaContext.getEmpresaId())
                .and(PedidoVendaItemSpecification.naoDeletado())
                .and(PedidoVendaItemSpecification.ativo(ativo))
                .and(PedidoVendaItemSpecification.pedidoIgual(pedidoVendaId))
                .and(PedidoVendaItemSpecification.produtoIgual(produtoId));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public PedidoVendaItemResponse criar(PedidoVendaItemRequest request) {
        PedidoVenda pedido = buscarPedido(request.pedidoVendaId());
        Produto produto = buscarProduto(request.produtoId());
        ProdutoPreco preco = buscarProdutoPreco(request.produtoPrecoId());
        ProdutoUnidade unidade = buscarProdutoUnidade(request.produtoUnidadeId());
        validator.validar(pedido, produto, preco, unidade, empresaContext.getEmpresaId(),
                request.quantidade(), request.desconto(), request.acrescimo());

        PedidoVendaItem entity = mapper.toEntity(request);
        associar(entity, pedido, produto, preco, unidade, request);
        PedidoVendaItem salvo = repository.save(entity);
        pedidoVendaService.recalcularTotal(pedido);
        eventPublisher.publishEvent(new PedidoVendaItemCriadoEvent(
                salvo.getId(), pedido.getId(), produto.getId(), salvo.getValorTotal()
        ));
        return mapper.toResponse(salvo);
    }

    @Override
    @Transactional
    public PedidoVendaItemResponse atualizar(UUID id, PedidoVendaItemRequest request) {
        PedidoVendaItem entity = buscarNoContexto(id);
        PedidoVenda pedido = buscarPedido(request.pedidoVendaId());
        Produto produto = buscarProduto(request.produtoId());
        ProdutoPreco preco = buscarProdutoPreco(request.produtoPrecoId());
        ProdutoUnidade unidade = buscarProdutoUnidade(request.produtoUnidadeId());
        validator.validar(pedido, produto, preco, unidade, empresaContext.getEmpresaId(),
                request.quantidade(), request.desconto(), request.acrescimo());

        associar(entity, pedido, produto, preco, unidade, request);
        pedidoVendaService.recalcularTotal(pedido);
        eventPublisher.publishEvent(new PedidoVendaItemAlteradoEvent(
                entity.getId(), pedido.getId(), produto.getId(), entity.getValorTotal()
        ));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        PedidoVendaItem entity = buscarNoContexto(id);
        PedidoVenda pedido = entity.getPedidoVenda();
        pedido.validarEditavel();
        entity.excluirLogicamente();
        pedidoVendaService.recalcularTotal(pedido);
        eventPublisher.publishEvent(new PedidoVendaItemRemovidoEvent(
                entity.getId(), pedido.getId(), entity.getProduto().getId()
        ));
    }

    private void associar(PedidoVendaItem entity, PedidoVenda pedido, Produto produto, ProdutoPreco preco,
                          ProdutoUnidade unidade, PedidoVendaItemRequest request) {
        entity.setPedidoVenda(pedido);
        entity.setProduto(produto);
        entity.setProdutoPreco(preco);
        entity.setProdutoUnidade(unidade);
        entity.atualizar(request.quantidade(), preco.getPreco(), request.desconto(), request.acrescimo(), request.observacao());
    }

    private PedidoVenda buscarPedido(UUID id) {
        return pedidoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venda nao encontrado"));
    }

    private Produto buscarProduto(UUID id) {
        return produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    private ProdutoPreco buscarProdutoPreco(UUID id) {
        return produtoPrecoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                        id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("ProdutoPreco nao encontrado"));
    }

    private ProdutoUnidade buscarProdutoUnidade(UUID id) {
        return produtoUnidadeRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProdutoUnidade nao encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoVendaItemResponse buscar(UUID id) {
        return mapper.toResponse(buscarNoContexto(id));
    }

    private PedidoVendaItem buscarNoContexto(UUID id) {
        return repository.findByIdAndPedidoVendaEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                        id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Item do pedido de venda nao encontrado"));
    }
}
