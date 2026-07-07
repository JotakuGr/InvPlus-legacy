package br.com.invplus.modules.compras.service;

import br.com.invplus.modules.compras.dto.request.PedidoCompraItemRequest;
import br.com.invplus.modules.compras.dto.response.PedidoCompraItemResponse;
import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.modules.compras.entity.PedidoCompraItem;
import br.com.invplus.modules.compras.mapper.PedidoCompraItemMapper;
import br.com.invplus.modules.compras.repository.PedidoCompraItemRepository;
import br.com.invplus.modules.compras.repository.PedidoCompraRepository;
import br.com.invplus.modules.compras.specification.PedidoCompraItemSpecification;
import br.com.invplus.modules.compras.validator.PedidoCompraItemValidator;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoUnidadeRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantem itens e recalcula automaticamente o total do pedido.
 */
@Service
public class PedidoCompraItemService extends BaseCrudService<PedidoCompraItem, PedidoCompraItemRequest, PedidoCompraItemResponse> {

    private final PedidoCompraItemRepository repository;
    private final PedidoCompraItemMapper mapper;
    private final PedidoCompraItemValidator validator;
    private final PedidoCompraRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final PedidoCompraService pedidoCompraService;

    public PedidoCompraItemService(
            PedidoCompraItemRepository repository,
            PedidoCompraItemMapper mapper,
            PedidoCompraItemValidator validator,
            PedidoCompraRepository pedidoRepository,
            ProdutoRepository produtoRepository,
            ProdutoUnidadeRepository produtoUnidadeRepository,
            PedidoCompraService pedidoCompraService
    ) {
        super(repository, mapper, "Item do pedido de compra");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.produtoUnidadeRepository = produtoUnidadeRepository;
        this.pedidoCompraService = pedidoCompraService;
    }

    @Transactional(readOnly = true)
    public Page<PedidoCompraItemResponse> listarComFiltros(UUID pedidoCompraId, UUID produtoId, Boolean ativo, Pageable pageable) {
        Specification<PedidoCompraItem> specification = PedidoCompraItemSpecification.naoDeletado()
                .and(PedidoCompraItemSpecification.ativo(ativo))
                .and(PedidoCompraItemSpecification.pedidoIgual(pedidoCompraId))
                .and(PedidoCompraItemSpecification.produtoIgual(produtoId));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public PedidoCompraItemResponse criar(PedidoCompraItemRequest request) {
        PedidoCompra pedido = buscarPedido(request.pedidoCompraId());
        validator.validar(pedido, request.quantidade(), request.valorUnitario());
        PedidoCompraItem entity = mapper.toEntity(request);
        associar(entity, pedido, request);
        PedidoCompraItem salvo = repository.save(entity);
        pedidoCompraService.recalcularTotal(pedido);
        return mapper.toResponse(salvo);
    }

    @Override
    @Transactional
    public PedidoCompraItemResponse atualizar(UUID id, PedidoCompraItemRequest request) {
        PedidoCompraItem entity = buscarEntidade(id);
        PedidoCompra pedido = buscarPedido(request.pedidoCompraId());
        validator.validar(pedido, request.quantidade(), request.valorUnitario());
        mapper.updateEntity(entity, request);
        associar(entity, pedido, request);
        pedidoCompraService.recalcularTotal(pedido);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        PedidoCompraItem entity = buscarEntidade(id);
        PedidoCompra pedido = entity.getPedidoCompra();
        pedido.validarEditavel();
        entity.excluirLogicamente();
        pedidoCompraService.recalcularTotal(pedido);
    }

    private void associar(PedidoCompraItem entity, PedidoCompra pedido, PedidoCompraItemRequest request) {
        entity.setPedidoCompra(pedido);
        entity.setProduto(buscarProduto(request.produtoId()));
        entity.setProdutoUnidade(buscarProdutoUnidade(request.produtoUnidadeId()));
    }

    private PedidoCompra buscarPedido(UUID id) {
        return pedidoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
    }

    private Produto buscarProduto(UUID id) {
        return produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    private ProdutoUnidade buscarProdutoUnidade(UUID id) {
        if (id == null) {
            return null;
        }
        return produtoUnidadeRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade do produto nao encontrada"));
    }
}
