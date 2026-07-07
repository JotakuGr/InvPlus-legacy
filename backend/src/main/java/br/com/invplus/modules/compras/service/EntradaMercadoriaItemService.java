package br.com.invplus.modules.compras.service;

import br.com.invplus.modules.compras.dto.request.EntradaMercadoriaItemRequest;
import br.com.invplus.modules.compras.dto.response.EntradaMercadoriaItemResponse;
import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.compras.entity.EntradaMercadoriaItem;
import br.com.invplus.modules.compras.entity.PedidoCompraItem;
import br.com.invplus.modules.compras.mapper.EntradaMercadoriaItemMapper;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaItemRepository;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaRepository;
import br.com.invplus.modules.compras.repository.PedidoCompraItemRepository;
import br.com.invplus.modules.compras.specification.EntradaMercadoriaItemSpecification;
import br.com.invplus.modules.compras.validator.EntradaMercadoriaItemValidator;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
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
 * Mantem itens recebidos e recalcula o total da entrada.
 */
@Service
public class EntradaMercadoriaItemService extends BaseCrudService<EntradaMercadoriaItem, EntradaMercadoriaItemRequest, EntradaMercadoriaItemResponse> {

    private final EntradaMercadoriaItemRepository repository;
    private final EntradaMercadoriaItemMapper mapper;
    private final EntradaMercadoriaItemValidator validator;
    private final EntradaMercadoriaRepository entradaRepository;
    private final PedidoCompraItemRepository pedidoCompraItemRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final DepositoRepository depositoRepository;
    private final LocalizacaoEstoqueRepository localizacaoRepository;
    private final EntradaMercadoriaService entradaMercadoriaService;

    public EntradaMercadoriaItemService(
            EntradaMercadoriaItemRepository repository,
            EntradaMercadoriaItemMapper mapper,
            EntradaMercadoriaItemValidator validator,
            EntradaMercadoriaRepository entradaRepository,
            PedidoCompraItemRepository pedidoCompraItemRepository,
            ProdutoRepository produtoRepository,
            ProdutoUnidadeRepository produtoUnidadeRepository,
            DepositoRepository depositoRepository,
            LocalizacaoEstoqueRepository localizacaoRepository,
            EntradaMercadoriaService entradaMercadoriaService
    ) {
        super(repository, mapper, "Item da entrada de mercadoria");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.entradaRepository = entradaRepository;
        this.pedidoCompraItemRepository = pedidoCompraItemRepository;
        this.produtoRepository = produtoRepository;
        this.produtoUnidadeRepository = produtoUnidadeRepository;
        this.depositoRepository = depositoRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.entradaMercadoriaService = entradaMercadoriaService;
    }

    @Transactional(readOnly = true)
    public Page<EntradaMercadoriaItemResponse> listarComFiltros(UUID entradaMercadoriaId, UUID produtoId,
                                                                Boolean ativo, Pageable pageable) {
        Specification<EntradaMercadoriaItem> specification = EntradaMercadoriaItemSpecification.naoDeletado()
                .and(EntradaMercadoriaItemSpecification.ativo(ativo))
                .and(EntradaMercadoriaItemSpecification.entradaIgual(entradaMercadoriaId))
                .and(EntradaMercadoriaItemSpecification.produtoIgual(produtoId));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public EntradaMercadoriaItemResponse criar(EntradaMercadoriaItemRequest request) {
        EntradaMercadoria entrada = buscarEntrada(request.entradaMercadoriaId());
        Deposito deposito = buscarDeposito(request.depositoId());
        LocalizacaoEstoque localizacao = buscarLocalizacao(request.localizacaoId());
        validator.validar(entrada, deposito, localizacao, request.quantidade(), request.valorUnitario());
        EntradaMercadoriaItem entity = mapper.toEntity(request);
        associar(entity, entrada, deposito, localizacao, request);
        EntradaMercadoriaItem salvo = repository.save(entity);
        entradaMercadoriaService.recalcularTotal(entrada);
        return mapper.toResponse(salvo);
    }

    @Override
    @Transactional
    public EntradaMercadoriaItemResponse atualizar(UUID id, EntradaMercadoriaItemRequest request) {
        EntradaMercadoriaItem entity = buscarEntidade(id);
        EntradaMercadoria entrada = buscarEntrada(request.entradaMercadoriaId());
        Deposito deposito = buscarDeposito(request.depositoId());
        LocalizacaoEstoque localizacao = buscarLocalizacao(request.localizacaoId());
        validator.validar(entrada, deposito, localizacao, request.quantidade(), request.valorUnitario());
        mapper.updateEntity(entity, request);
        associar(entity, entrada, deposito, localizacao, request);
        entradaMercadoriaService.recalcularTotal(entrada);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        EntradaMercadoriaItem entity = buscarEntidade(id);
        EntradaMercadoria entrada = entity.getEntradaMercadoria();
        entrada.validarEditavel();
        entity.excluirLogicamente();
        entradaMercadoriaService.recalcularTotal(entrada);
    }

    private void associar(EntradaMercadoriaItem entity, EntradaMercadoria entrada, Deposito deposito,
                          LocalizacaoEstoque localizacao, EntradaMercadoriaItemRequest request) {
        entity.setEntradaMercadoria(entrada);
        entity.setPedidoCompraItem(buscarPedidoCompraItem(request.pedidoCompraItemId()));
        entity.setProduto(buscarProduto(request.produtoId()));
        entity.setProdutoUnidade(buscarProdutoUnidade(request.produtoUnidadeId()));
        entity.setDeposito(deposito);
        entity.setLocalizacao(localizacao);
    }

    private EntradaMercadoria buscarEntrada(UUID id) {
        return entradaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrada de mercadoria nao encontrada"));
    }

    private PedidoCompraItem buscarPedidoCompraItem(UUID id) {
        if (id == null) {
            return null;
        }
        return pedidoCompraItemRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item do pedido de compra nao encontrado"));
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

    private Deposito buscarDeposito(UUID id) {
        return depositoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));
    }

    private LocalizacaoEstoque buscarLocalizacao(UUID id) {
        return localizacaoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Localizacao de estoque nao encontrada"));
    }
}
