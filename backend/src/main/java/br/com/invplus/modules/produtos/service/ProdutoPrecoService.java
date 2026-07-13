package br.com.invplus.modules.produtos.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.produtos.dto.request.ProdutoPrecoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoPrecoResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import br.com.invplus.modules.produtos.mapper.ProdutoPrecoMapper;
import br.com.invplus.modules.produtos.repository.ProdutoPrecoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.specification.ProdutoPrecoSpecification;
import br.com.invplus.modules.produtos.validator.ProdutoPrecoValidator;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para historico de precos comerciais de Produto.
 */
@Service
public class ProdutoPrecoService extends BaseCrudService<ProdutoPreco, ProdutoPrecoRequest, ProdutoPrecoResponse> {

    private final ProdutoPrecoRepository repository;
    private final ProdutoPrecoMapper mapper;
    private final ProdutoPrecoValidator validator;
    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpresaContext empresaContext;

    public ProdutoPrecoService(
            ProdutoPrecoRepository repository,
            ProdutoPrecoMapper mapper,
            ProdutoPrecoValidator validator,
            ProdutoRepository produtoRepository,
            EmpresaRepository empresaRepository,
            EmpresaContext empresaContext
    ) {
        super(repository, mapper, "ProdutoPreco");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.produtoRepository = produtoRepository;
        this.empresaRepository = empresaRepository;
        this.empresaContext = empresaContext;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoPrecoResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista precos com filtros comerciais e paginacao.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoPrecoResponse> listarComFiltros(
            UUID produtoId,
            TipoPrecoProduto tipoPreco,
            Boolean principal,
            LocalDate vigenciaInicioDe,
            LocalDate vigenciaFimAte,
            Boolean ativo,
            Pageable pageable
    ) {
        UUID empresaId = empresaContext.getEmpresaId();
        Specification<ProdutoPreco> specification = ProdutoPrecoSpecification.pertenceEmpresa(empresaId)
                .and(ProdutoPrecoSpecification.naoDeletado())
                .and(ProdutoPrecoSpecification.ativo(ativo))
                .and(ProdutoPrecoSpecification.produtoIgual(produtoId))
                .and(ProdutoPrecoSpecification.tipoIgual(tipoPreco))
                .and(ProdutoPrecoSpecification.principalIgual(principal))
                .and(ProdutoPrecoSpecification.vigenciaInicioMaiorOuIgual(vigenciaInicioDe))
                .and(ProdutoPrecoSpecification.vigenciaFimMenorOuIgual(vigenciaFimAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoPrecoResponse buscar(UUID id) {
        return mapper.toResponse(buscarNoContexto(id));
    }

    /**
     * Busca o preco global vigente para o produto, tipo e data informados.
     */
    @Transactional(readOnly = true)
    public Optional<ProdutoPrecoResponse> buscarPrecoVigente(UUID produtoId, TipoPrecoProduto tipoPreco, LocalDate data) {
        LocalDate dataConsulta = data == null ? LocalDate.now() : data;
        return repository.buscarPrecosVigentes(
                        produtoId, empresaContext.getEmpresaId(), tipoPreco, dataConsulta)
                .stream()
                .findFirst()
                .map(mapper::toResponse);
    }

    /**
     * Cria um preco comercial para produto ativo e empresa opcional ativa.
     */
    @Override
    @Transactional
    public ProdutoPrecoResponse criar(ProdutoPrecoRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        validator.validarCriacao(empresaId, request);
        Produto produto = buscarProdutoAtivo(request.produtoId());
        Empresa empresa = buscarEmpresaAtiva(empresaId);
        ProdutoPreco entity = mapper.toEntity(request);
        entity.setProduto(produto);
        entity.setEmpresa(empresa);
        return mapper.toResponse(repository.save(entity));
    }

    /**
     * Atualiza preco, vigencia e relacionamentos mantendo historico por soft delete.
     */
    @Override
    @Transactional
    public ProdutoPrecoResponse atualizar(UUID id, ProdutoPrecoRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        ProdutoPreco entity = buscarNoContexto(id);
        validator.validarAtualizacao(empresaId, entity, request);
        entity.setProduto(buscarProdutoAtivo(request.produtoId()));
        entity.setEmpresa(buscarEmpresaAtiva(empresaId));
        mapper.updateEntity(entity, request);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        buscarNoContexto(id).excluirLogicamente();
    }

    private Produto buscarProdutoAtivo(UUID id) {
        return produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    private Empresa buscarEmpresaAtiva(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    private ProdutoPreco buscarNoContexto(UUID id) {
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("ProdutoPreco nao encontrado"));
    }
}
