package br.com.invplus.modules.produtos.service;

import br.com.invplus.modules.produtos.dto.request.ProdutoFornecedorPrecoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoFornecedorPrecoResponse;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedor;
import br.com.invplus.modules.produtos.entity.ProdutoFornecedorPreco;
import br.com.invplus.modules.produtos.mapper.ProdutoFornecedorPrecoMapper;
import br.com.invplus.modules.produtos.repository.ProdutoFornecedorPrecoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoFornecedorRepository;
import br.com.invplus.modules.produtos.specification.ProdutoFornecedorPrecoSpecification;
import br.com.invplus.modules.produtos.validator.ProdutoFornecedorPrecoValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para historico de preco do fornecedor do produto.
 */
@Service
public class ProdutoFornecedorPrecoService extends BaseCrudService<ProdutoFornecedorPreco, ProdutoFornecedorPrecoRequest, ProdutoFornecedorPrecoResponse> {

    private final ProdutoFornecedorPrecoRepository repository;
    private final ProdutoFornecedorPrecoMapper mapper;
    private final ProdutoFornecedorPrecoValidator validator;
    private final ProdutoFornecedorRepository produtoFornecedorRepository;

    public ProdutoFornecedorPrecoService(
            ProdutoFornecedorPrecoRepository repository,
            ProdutoFornecedorPrecoMapper mapper,
            ProdutoFornecedorPrecoValidator validator,
            ProdutoFornecedorRepository produtoFornecedorRepository
    ) {
        super(repository, mapper, "ProdutoFornecedorPreco");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.produtoFornecedorRepository = produtoFornecedorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoFornecedorPrecoResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista historico de preco por vinculo, intervalo de vigencia e status ativo.
     */
    @Transactional(readOnly = true)
    public Page<ProdutoFornecedorPrecoResponse> listarComFiltros(
            UUID produtoFornecedorId,
            LocalDate vigenciaInicioDe,
            LocalDate vigenciaFimAte,
            Boolean ativo,
            Pageable pageable
    ) {
        Specification<ProdutoFornecedorPreco> specification = ProdutoFornecedorPrecoSpecification.naoDeletado()
                .and(ProdutoFornecedorPrecoSpecification.ativo(ativo))
                .and(ProdutoFornecedorPrecoSpecification.produtoFornecedorIgual(produtoFornecedorId))
                .and(ProdutoFornecedorPrecoSpecification.vigenciaInicioMaiorOuIgual(vigenciaInicioDe))
                .and(ProdutoFornecedorPrecoSpecification.vigenciaFimMenorOuIgual(vigenciaFimAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoFornecedorPrecoResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Registra novo preco historico para um vinculo ativo de ProdutoFornecedor.
     */
    @Override
    @Transactional
    public ProdutoFornecedorPrecoResponse criar(ProdutoFornecedorPrecoRequest request) {
        validator.validarCriacao(request);
        ProdutoFornecedor vinculo = buscarVinculoAtivo(request.produtoFornecedorId());
        ProdutoFornecedorPreco entity = mapper.toEntity(request);
        entity.setProdutoFornecedor(vinculo);
        return mapper.toResponse(repository.save(entity));
    }

    /**
     * Atualiza preco e vigencia sem perder o vinculo de ProdutoFornecedor.
     */
    @Override
    @Transactional
    public ProdutoFornecedorPrecoResponse atualizar(UUID id, ProdutoFornecedorPrecoRequest request) {
        ProdutoFornecedorPreco entity = buscarEntidade(id);
        validator.validarAtualizacao(entity, request);
        entity.setProdutoFornecedor(buscarVinculoAtivo(request.produtoFornecedorId()));
        mapper.updateEntity(entity, request);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }

    private ProdutoFornecedor buscarVinculoAtivo(UUID id) {
        return produtoFornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProdutoFornecedor nao encontrado"));
    }
}
