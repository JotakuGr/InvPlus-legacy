package br.com.invplus.modules.produtos.service;

import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFornecedorResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedor;
import br.com.invplus.modules.produtos.enums.FornecedorProdutoStatus;
import br.com.invplus.modules.produtos.mapper.ProdutoFornecedorMapper;
import br.com.invplus.modules.produtos.repository.ProdutoFornecedorRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.specification.ProdutoFornecedorSpecification;
import br.com.invplus.modules.produtos.validator.ProdutoFornecedorValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para fornecedores vinculados a produtos.
 */
@Service
public class ProdutoFornecedorService extends BaseCrudService<ProdutoFornecedor, ProdutoFornecedorRequest, ProdutoFornecedorResponse> {

    private final ProdutoFornecedorRepository produtoFornecedorRepository;
    private final ProdutoFornecedorMapper produtoFornecedorMapper;
    private final ProdutoFornecedorValidator produtoFornecedorValidator;
    private final ProdutoRepository produtoRepository;
    private final FornecedorRepository fornecedorRepository;

    public ProdutoFornecedorService(
            ProdutoFornecedorRepository produtoFornecedorRepository,
            ProdutoFornecedorMapper produtoFornecedorMapper,
            ProdutoFornecedorValidator produtoFornecedorValidator,
            ProdutoRepository produtoRepository,
            FornecedorRepository fornecedorRepository
    ) {
        super(produtoFornecedorRepository, produtoFornecedorMapper, "ProdutoFornecedor");
        this.produtoFornecedorRepository = produtoFornecedorRepository;
        this.produtoFornecedorMapper = produtoFornecedorMapper;
        this.produtoFornecedorValidator = produtoFornecedorValidator;
        this.produtoRepository = produtoRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoFornecedorResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista vinculos com filtros opcionais usados por compras e cotacoes.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoFornecedorResponse> listarComFiltros(
            UUID produtoId,
            UUID fornecedorId,
            Boolean principal,
            Boolean ativo,
            String codigoFornecedor,
            FornecedorProdutoStatus status,
            Pageable pageable
    ) {
        Specification<ProdutoFornecedor> specification = ProdutoFornecedorSpecification.naoDeletado()
                .and(ProdutoFornecedorSpecification.ativo(ativo))
                .and(ProdutoFornecedorSpecification.produtoIgual(produtoId))
                .and(ProdutoFornecedorSpecification.fornecedorIgual(fornecedorId))
                .and(ProdutoFornecedorSpecification.principalIgual(principal))
                .and(ProdutoFornecedorSpecification.codigoFornecedorContem(codigoFornecedor))
                .and(ProdutoFornecedorSpecification.statusIgual(status));
        return produtoFornecedorRepository.findAll(specification, pageable).map(produtoFornecedorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoFornecedorResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria o vinculo apos confirmar que Produto e Fornecedor estao ativos.
     */
    @Override
    @Transactional
    public ProdutoFornecedorResponse criar(ProdutoFornecedorRequest request) {
        produtoFornecedorValidator.validarCriacao(request);
        Produto produto = buscarProdutoAtivo(request.produtoId());
        Fornecedor fornecedor = buscarFornecedorAtivo(request.fornecedorId());
        ProdutoFornecedor entity = produtoFornecedorMapper.toEntity(request);
        entity.setProduto(produto);
        entity.setFornecedor(fornecedor);
        return produtoFornecedorMapper.toResponse(produtoFornecedorRepository.save(entity));
    }

    /**
     * Atualiza o vinculo permitindo trocar Produto ou Fornecedor quando a regra permitir.
     */
    @Override
    @Transactional
    public ProdutoFornecedorResponse atualizar(UUID id, ProdutoFornecedorRequest request) {
        ProdutoFornecedor entity = buscarEntidade(id);
        produtoFornecedorValidator.validarAtualizacao(entity, request);
        Produto produto = buscarProdutoAtivo(request.produtoId());
        Fornecedor fornecedor = buscarFornecedorAtivo(request.fornecedorId());
        entity.setProduto(produto);
        entity.setFornecedor(fornecedor);
        produtoFornecedorMapper.updateEntity(entity, request);
        return produtoFornecedorMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }

    private Produto buscarProdutoAtivo(UUID produtoId) {
        return produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    private Fornecedor buscarFornecedorAtivo(UUID fornecedorId) {
        return fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fornecedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado"));
    }
}
