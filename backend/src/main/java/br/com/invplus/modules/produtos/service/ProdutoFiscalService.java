package br.com.invplus.modules.produtos.service;

import br.com.invplus.modules.produtos.dto.request.ProdutoFiscalRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFiscalResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoFiscal;
import br.com.invplus.modules.produtos.enums.OrigemMercadoria;
import br.com.invplus.modules.produtos.mapper.ProdutoFiscalMapper;
import br.com.invplus.modules.produtos.repository.ProdutoFiscalRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.specification.ProdutoFiscalSpecification;
import br.com.invplus.modules.produtos.validator.ProdutoFiscalValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio dos dados fiscais de Produto.
 */
@Service
public class ProdutoFiscalService extends BaseCrudService<ProdutoFiscal, ProdutoFiscalRequest, ProdutoFiscalResponse> {

    private final ProdutoFiscalRepository repository;
    private final ProdutoFiscalMapper mapper;
    private final ProdutoFiscalValidator validator;
    private final ProdutoRepository produtoRepository;

    public ProdutoFiscalService(
            ProdutoFiscalRepository repository,
            ProdutoFiscalMapper mapper,
            ProdutoFiscalValidator validator,
            ProdutoRepository produtoRepository
    ) {
        super(repository, mapper, "ProdutoFiscal");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.produtoRepository = produtoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoFiscalResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista dados fiscais com filtros por produto, classificacao fiscal e status.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoFiscalResponse> listarComFiltros(
            UUID produtoId,
            String ncm,
            String cest,
            OrigemMercadoria origemMercadoria,
            Boolean ativo,
            Pageable pageable
    ) {
        Specification<ProdutoFiscal> specification = ProdutoFiscalSpecification.naoDeletado()
                .and(ProdutoFiscalSpecification.ativo(ativo))
                .and(ProdutoFiscalSpecification.produtoIgual(produtoId))
                .and(ProdutoFiscalSpecification.ncmContem(ncm))
                .and(ProdutoFiscalSpecification.cestContem(cest))
                .and(ProdutoFiscalSpecification.origemIgual(origemMercadoria));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoFiscalResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria dados fiscais para um produto ativo, impedindo duplicidade ativa por produto.
     */
    @Override
    @Transactional
    public ProdutoFiscalResponse criar(ProdutoFiscalRequest request) {
        validator.validarCriacao(request);
        Produto produto = buscarProdutoAtivo(request.produtoId());
        ProdutoFiscal entity = mapper.toEntity(request);
        entity.setProduto(produto);
        return mapper.toResponse(repository.save(entity));
    }

    /**
     * Atualiza a ficha fiscal e permite trocar o produto quando a regra de unicidade permitir.
     */
    @Override
    @Transactional
    public ProdutoFiscalResponse atualizar(UUID id, ProdutoFiscalRequest request) {
        ProdutoFiscal entity = buscarEntidade(id);
        validator.validarAtualizacao(entity, request);
        entity.setProduto(buscarProdutoAtivo(request.produtoId()));
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
}
