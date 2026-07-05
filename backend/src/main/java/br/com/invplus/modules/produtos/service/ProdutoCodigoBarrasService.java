package br.com.invplus.modules.produtos.service;

import br.com.invplus.modules.produtos.dto.request.ProdutoCodigoBarrasRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoCodigoBarrasResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoCodigoBarras;
import br.com.invplus.modules.produtos.enums.TipoCodigoBarras;
import br.com.invplus.modules.produtos.mapper.ProdutoCodigoBarrasMapper;
import br.com.invplus.modules.produtos.repository.ProdutoCodigoBarrasRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.specification.ProdutoCodigoBarrasSpecification;
import br.com.invplus.modules.produtos.validator.ProdutoCodigoBarrasValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para codigos de barras e codigos alternativos de Produto.
 */
@Service
public class ProdutoCodigoBarrasService extends BaseCrudService<ProdutoCodigoBarras, ProdutoCodigoBarrasRequest, ProdutoCodigoBarrasResponse> {

    private final ProdutoCodigoBarrasRepository produtoCodigoBarrasRepository;
    private final ProdutoCodigoBarrasMapper produtoCodigoBarrasMapper;
    private final ProdutoCodigoBarrasValidator produtoCodigoBarrasValidator;
    private final ProdutoRepository produtoRepository;

    public ProdutoCodigoBarrasService(
            ProdutoCodigoBarrasRepository produtoCodigoBarrasRepository,
            ProdutoCodigoBarrasMapper produtoCodigoBarrasMapper,
            ProdutoCodigoBarrasValidator produtoCodigoBarrasValidator,
            ProdutoRepository produtoRepository
    ) {
        super(produtoCodigoBarrasRepository, produtoCodigoBarrasMapper, "ProdutoCodigoBarras");
        this.produtoCodigoBarrasRepository = produtoCodigoBarrasRepository;
        this.produtoCodigoBarrasMapper = produtoCodigoBarrasMapper;
        this.produtoCodigoBarrasValidator = produtoCodigoBarrasValidator;
        this.produtoRepository = produtoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoCodigoBarrasResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista codigos com filtros opcionais por Produto, tipo, codigo, principal e ativo.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoCodigoBarrasResponse> listarComFiltros(
            UUID produtoId,
            TipoCodigoBarras tipoCodigo,
            String codigo,
            Boolean principal,
            Boolean ativo,
            Pageable pageable
    ) {
        Specification<ProdutoCodigoBarras> specification = ProdutoCodigoBarrasSpecification.naoDeletado()
                .and(ProdutoCodigoBarrasSpecification.ativo(ativo))
                .and(ProdutoCodigoBarrasSpecification.produtoIgual(produtoId))
                .and(ProdutoCodigoBarrasSpecification.tipoIgual(tipoCodigo))
                .and(ProdutoCodigoBarrasSpecification.codigoContem(codigo))
                .and(ProdutoCodigoBarrasSpecification.principalIgual(principal));
        return produtoCodigoBarrasRepository.findAll(specification, pageable).map(produtoCodigoBarrasMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoCodigoBarrasResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Cria codigo apos confirmar que Produto esta ativo.
     */
    @Override
    @Transactional
    public ProdutoCodigoBarrasResponse criar(ProdutoCodigoBarrasRequest request) {
        produtoCodigoBarrasValidator.validarCriacao(request);
        Produto produto = buscarProdutoAtivo(request.produtoId());
        ProdutoCodigoBarras entity = produtoCodigoBarrasMapper.toEntity(request);
        entity.setProduto(produto);
        return produtoCodigoBarrasMapper.toResponse(produtoCodigoBarrasRepository.save(entity));
    }

    /**
     * Atualiza codigo permitindo trocar Produto quando as regras permitirem.
     */
    @Override
    @Transactional
    public ProdutoCodigoBarrasResponse atualizar(UUID id, ProdutoCodigoBarrasRequest request) {
        ProdutoCodigoBarras entity = buscarEntidade(id);
        produtoCodigoBarrasValidator.validarAtualizacao(entity, request);
        entity.setProduto(buscarProdutoAtivo(request.produtoId()));
        produtoCodigoBarrasMapper.updateEntity(entity, request);
        return produtoCodigoBarrasMapper.toResponse(entity);
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
}
