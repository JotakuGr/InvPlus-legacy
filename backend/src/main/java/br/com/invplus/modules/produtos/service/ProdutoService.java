package br.com.invplus.modules.produtos.service;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.repository.MarcaRepository;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.mapper.ProdutoMapper;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.specification.ProdutoSpecification;
import br.com.invplus.modules.produtos.validator.ProdutoValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio do catalogo de Produtos.
 */
@Service
public class ProdutoService extends BaseCrudService<Produto, ProdutoRequest, ProdutoResponse> {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;
    private final ProdutoValidator produtoValidator;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            ProdutoMapper produtoMapper,
            ProdutoValidator produtoValidator,
            CategoriaRepository categoriaRepository,
            MarcaRepository marcaRepository,
            UnidadeMedidaRepository unidadeMedidaRepository
    ) {
        super(produtoRepository, produtoMapper, "Produto");
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
        this.produtoValidator = produtoValidator;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.unidadeMedidaRepository = unidadeMedidaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista produtos com filtros compostos, mantendo registros deletados fora do resultado.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarComFiltros(
            String nome,
            String codigo,
            String sku,
            String codigoBarras,
            UUID categoriaId,
            UUID marcaId,
            TipoProduto tipoProduto,
            NaturezaProduto naturezaProduto,
            StatusProduto statusProduto,
            Boolean controlaEstoque,
            Boolean permiteCompra,
            Boolean permiteVenda,
            Boolean ativo,
            Pageable pageable
    ) {
        Specification<Produto> specification = ProdutoSpecification.naoDeletado()
                .and(ProdutoSpecification.ativo(ativo))
                .and(ProdutoSpecification.nomeContem(nome))
                .and(ProdutoSpecification.codigoContem(codigo))
                .and(ProdutoSpecification.skuContem(sku))
                .and(ProdutoSpecification.codigoBarrasContem(codigoBarras))
                .and(ProdutoSpecification.categoriaIgual(categoriaId))
                .and(ProdutoSpecification.marcaIgual(marcaId))
                .and(ProdutoSpecification.tipoIgual(tipoProduto))
                .and(ProdutoSpecification.naturezaIgual(naturezaProduto))
                .and(ProdutoSpecification.statusIgual(statusProduto))
                .and(ProdutoSpecification.controlaEstoqueIgual(controlaEstoque))
                .and(ProdutoSpecification.permiteCompraIgual(permiteCompra))
                .and(ProdutoSpecification.permiteVendaIgual(permiteVenda));
        return produtoRepository.findAll(specification, pageable).map(produtoMapper::toResponse);
    }

    /**
     * Executa busca rapida por nome, codigo, SKU ou codigo de barras.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoResponse> buscarRapido(String termo, Pageable pageable) {
        Specification<Produto> specification = ProdutoSpecification.naoDeletado()
                .and(ProdutoSpecification.ativo(Boolean.TRUE))
                .and(ProdutoSpecification.termoRapido(termo));
        return produtoRepository.findAll(specification, pageable).map(produtoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria produto associando cadastros base ativos.
     */
    @Override
    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        produtoValidator.validarCriacao(request);
        Categoria categoria = buscarCategoriaAtiva(request.categoriaId());
        Marca marca = buscarMarcaAtivaSeInformada(request.marcaId());
        UnidadeMedida unidade = buscarUnidadeAtiva(request.unidadeMedidaId());
        Produto produto = produtoMapper.toEntity(request);
        produto.setCategoria(categoria);
        produto.setMarca(marca);
        produto.setUnidadeMedida(unidade);
        return produtoMapper.toResponse(produtoRepository.save(produto));
    }

    /**
     * Atualiza produto e permite trocar categoria, marca e unidade.
     */
    @Override
    @Transactional
    public ProdutoResponse atualizar(UUID id, ProdutoRequest request) {
        Produto produto = buscarEntidade(id);
        produtoValidator.validarAtualizacao(produto, request);
        produto.setCategoria(buscarCategoriaAtiva(request.categoriaId()));
        produto.setMarca(buscarMarcaAtivaSeInformada(request.marcaId()));
        produto.setUnidadeMedida(buscarUnidadeAtiva(request.unidadeMedidaId()));
        produtoMapper.updateEntity(produto, request);
        return produtoMapper.toResponse(produto);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }

    private Categoria buscarCategoriaAtiva(UUID categoriaId) {
        return categoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria nao encontrada"));
    }

    private Marca buscarMarcaAtivaSeInformada(UUID marcaId) {
        if (marcaId == null) {
            return null;
        }
        return marcaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(marcaId)
                .orElseThrow(() -> new ResourceNotFoundException("Marca nao encontrada"));
    }

    private UnidadeMedida buscarUnidadeAtiva(UUID unidadeMedidaId) {
        return unidadeMedidaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(unidadeMedidaId)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade de medida nao encontrada"));
    }
}
