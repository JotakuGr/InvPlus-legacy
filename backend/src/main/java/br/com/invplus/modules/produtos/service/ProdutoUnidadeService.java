package br.com.invplus.modules.produtos.service;

import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoUnidadeRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoUnidadeResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.produtos.mapper.ProdutoUnidadeMapper;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoUnidadeRepository;
import br.com.invplus.modules.produtos.specification.ProdutoUnidadeSpecification;
import br.com.invplus.modules.produtos.validator.ProdutoUnidadeValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para unidades alternativas do produto.
 */
@Service
public class ProdutoUnidadeService extends BaseCrudService<ProdutoUnidade, ProdutoUnidadeRequest, ProdutoUnidadeResponse> {

    private final ProdutoUnidadeRepository repository;
    private final ProdutoUnidadeMapper mapper;
    private final ProdutoUnidadeValidator validator;
    private final ProdutoRepository produtoRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;

    public ProdutoUnidadeService(
            ProdutoUnidadeRepository repository,
            ProdutoUnidadeMapper mapper,
            ProdutoUnidadeValidator validator,
            ProdutoRepository produtoRepository,
            UnidadeMedidaRepository unidadeMedidaRepository
    ) {
        super(repository, mapper, "ProdutoUnidade");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.produtoRepository = produtoRepository;
        this.unidadeMedidaRepository = unidadeMedidaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoUnidadeResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista unidades do produto com filtros operacionais.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoUnidadeResponse> listarComFiltros(
            UUID produtoId,
            UUID unidadeMedidaId,
            Boolean unidadePadrao,
            Boolean permiteCompra,
            Boolean permiteVenda,
            Boolean permiteEstoque,
            Boolean permiteFracionamento,
            Boolean ativo,
            Pageable pageable
    ) {
        Specification<ProdutoUnidade> specification = ProdutoUnidadeSpecification.naoDeletado()
                .and(ProdutoUnidadeSpecification.ativo(ativo))
                .and(ProdutoUnidadeSpecification.produtoIgual(produtoId))
                .and(ProdutoUnidadeSpecification.unidadeIgual(unidadeMedidaId))
                .and(ProdutoUnidadeSpecification.unidadePadraoIgual(unidadePadrao))
                .and(ProdutoUnidadeSpecification.permiteCompraIgual(permiteCompra))
                .and(ProdutoUnidadeSpecification.permiteVendaIgual(permiteVenda))
                .and(ProdutoUnidadeSpecification.permiteEstoqueIgual(permiteEstoque))
                .and(ProdutoUnidadeSpecification.permiteFracionamentoIgual(permiteFracionamento));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoUnidadeResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria unidade alternativa para produto e unidade de medida ativos.
     */
    @Override
    @Transactional
    public ProdutoUnidadeResponse criar(ProdutoUnidadeRequest request) {
        validator.validarCriacao(request);
        Produto produto = buscarProdutoAtivo(request.produtoId());
        UnidadeMedida unidade = buscarUnidadeAtiva(request.unidadeMedidaId());
        ProdutoUnidade entity = mapper.toEntity(request);
        entity.setProduto(produto);
        entity.setUnidadeMedida(unidade);
        return mapper.toResponse(repository.save(entity));
    }

    /**
     * Atualiza conversao, flags operacionais e relacionamentos.
     */
    @Override
    @Transactional
    public ProdutoUnidadeResponse atualizar(UUID id, ProdutoUnidadeRequest request) {
        ProdutoUnidade entity = buscarEntidade(id);
        validator.validarAtualizacao(entity, request);
        entity.setProduto(buscarProdutoAtivo(request.produtoId()));
        entity.setUnidadeMedida(buscarUnidadeAtiva(request.unidadeMedidaId()));
        mapper.updateEntity(entity, request);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }

    private Produto buscarProdutoAtivo(UUID id) {
        return produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    private UnidadeMedida buscarUnidadeAtiva(UUID id) {
        return unidadeMedidaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade de medida nao encontrada"));
    }
}
