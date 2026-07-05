package br.com.invplus.modules.produtos.service;

import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.cadastros.repository.ArquivoRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoImagemRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoImagemResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoImagem;
import br.com.invplus.modules.produtos.mapper.ProdutoImagemMapper;
import br.com.invplus.modules.produtos.repository.ProdutoImagemRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.specification.ProdutoImagemSpecification;
import br.com.invplus.modules.produtos.validator.ProdutoImagemValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para imagens vinculadas a produtos.
 */
@Service
public class ProdutoImagemService extends BaseCrudService<ProdutoImagem, ProdutoImagemRequest, ProdutoImagemResponse> {

    private final ProdutoImagemRepository produtoImagemRepository;
    private final ProdutoImagemMapper produtoImagemMapper;
    private final ProdutoImagemValidator produtoImagemValidator;
    private final ProdutoRepository produtoRepository;
    private final ArquivoRepository arquivoRepository;

    public ProdutoImagemService(
            ProdutoImagemRepository produtoImagemRepository,
            ProdutoImagemMapper produtoImagemMapper,
            ProdutoImagemValidator produtoImagemValidator,
            ProdutoRepository produtoRepository,
            ArquivoRepository arquivoRepository
    ) {
        super(produtoImagemRepository, produtoImagemMapper, "ProdutoImagem");
        this.produtoImagemRepository = produtoImagemRepository;
        this.produtoImagemMapper = produtoImagemMapper;
        this.produtoImagemValidator = produtoImagemValidator;
        this.produtoRepository = produtoRepository;
        this.arquivoRepository = arquivoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoImagemResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista imagens com filtros opcionais por Produto, Arquivo, principal e ativo.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoImagemResponse> listarComFiltros(
            UUID produtoId,
            UUID arquivoId,
            Boolean principal,
            Boolean ativo,
            Pageable pageable
    ) {
        Specification<ProdutoImagem> specification = ProdutoImagemSpecification.naoDeletado()
                .and(ProdutoImagemSpecification.ativo(ativo))
                .and(ProdutoImagemSpecification.produtoIgual(produtoId))
                .and(ProdutoImagemSpecification.arquivoIgual(arquivoId))
                .and(ProdutoImagemSpecification.principalIgual(principal));
        return produtoImagemRepository.findAll(specification, pageable).map(produtoImagemMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoImagemResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria o vinculo apos confirmar Produto e Arquivo ativos.
     */
    @Override
    @Transactional
    public ProdutoImagemResponse criar(ProdutoImagemRequest request) {
        produtoImagemValidator.validarCriacao(request);
        Produto produto = buscarProdutoAtivo(request.produtoId());
        Arquivo arquivo = buscarArquivoAtivo(request.arquivoId());
        ProdutoImagem entity = produtoImagemMapper.toEntity(request);
        entity.setProduto(produto);
        entity.setArquivo(arquivo);
        return produtoImagemMapper.toResponse(produtoImagemRepository.save(entity));
    }

    /**
     * Atualiza o vinculo permitindo trocar Produto ou Arquivo quando as regras permitirem.
     */
    @Override
    @Transactional
    public ProdutoImagemResponse atualizar(UUID id, ProdutoImagemRequest request) {
        ProdutoImagem entity = buscarEntidade(id);
        produtoImagemValidator.validarAtualizacao(entity, request);
        entity.setProduto(buscarProdutoAtivo(request.produtoId()));
        entity.setArquivo(buscarArquivoAtivo(request.arquivoId()));
        produtoImagemMapper.updateEntity(entity, request);
        return produtoImagemMapper.toResponse(entity);
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

    private Arquivo buscarArquivoAtivo(UUID arquivoId) {
        return arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(arquivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Arquivo nao encontrado"));
    }
}
