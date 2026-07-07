package br.com.invplus.modules.estoque.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.estoque.dto.request.EstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.EstoqueResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.mapper.EstoqueMapper;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.estoque.specification.EstoqueSpecification;
import br.com.invplus.modules.estoque.validator.EstoqueValidator;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import br.com.invplus.shared.domain.service.EstoqueDomainService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para o saldo fisico atual de estoque.
 */
@Service
public class EstoqueService extends BaseCrudService<Estoque, EstoqueRequest, EstoqueResponse> {

    private final EstoqueRepository repository;
    private final EstoqueMapper mapper;
    private final EstoqueValidator validator;
    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final DepositoRepository depositoRepository;
    private final LocalizacaoEstoqueRepository localizacaoRepository;
    private final EstoqueDomainService estoqueDomainService;

    public EstoqueService(
            EstoqueRepository repository,
            EstoqueMapper mapper,
            EstoqueValidator validator,
            ProdutoRepository produtoRepository,
            EmpresaRepository empresaRepository,
            FilialRepository filialRepository,
            DepositoRepository depositoRepository,
            LocalizacaoEstoqueRepository localizacaoRepository,
            EstoqueDomainService estoqueDomainService
    ) {
        super(repository, mapper, "Estoque");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.produtoRepository = produtoRepository;
        this.empresaRepository = empresaRepository;
        this.filialRepository = filialRepository;
        this.depositoRepository = depositoRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.estoqueDomainService = estoqueDomainService;
    }

    @Transactional(readOnly = true)
    public Page<EstoqueResponse> listarComFiltros(
            UUID produtoId,
            UUID empresaId,
            UUID filialId,
            UUID depositoId,
            UUID localizacaoId,
            Boolean ativo,
            Boolean estoqueBaixo,
            Boolean estoqueZerado,
            Boolean comSaldo,
            Boolean semSaldo,
            Pageable pageable
    ) {
        Specification<Estoque> specification = EstoqueSpecification.naoDeletado()
                .and(EstoqueSpecification.ativo(ativo))
                .and(EstoqueSpecification.produtoIgual(produtoId))
                .and(EstoqueSpecification.empresaIgual(empresaId))
                .and(EstoqueSpecification.filialIgual(filialId))
                .and(EstoqueSpecification.depositoIgual(depositoId))
                .and(EstoqueSpecification.localizacaoIgual(localizacaoId))
                .and(EstoqueSpecification.estoqueBaixo(estoqueBaixo))
                .and(EstoqueSpecification.estoqueZerado(estoqueZerado))
                .and(EstoqueSpecification.comSaldo(comSaldo))
                .and(EstoqueSpecification.semSaldo(semSaldo));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EstoqueResponse buscarSaldo(UUID produtoId, UUID empresaId, UUID depositoId, UUID localizacaoId) {
        validarParametrosSaldo(produtoId, empresaId, depositoId, localizacaoId);
        return repository.findByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
                        produtoId,
                        empresaId,
                        depositoId,
                        localizacaoId
                )
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo de estoque nao encontrado"));
    }

    @Override
    @Transactional
    public EstoqueResponse criar(EstoqueRequest request) {
        Produto produto = buscarProdutoAtivo(request.produtoId());
        Empresa empresa = buscarEmpresaAtiva(request.empresaId());
        Filial filial = buscarFilialAtiva(request.filialId());
        Deposito deposito = buscarDepositoAtivo(request.depositoId());
        LocalizacaoEstoque localizacao = buscarLocalizacaoAtiva(request.localizacaoId());

        validator.validarCriacao(request, filial, deposito, localizacao);

        Estoque entity = mapper.toEntity(request);
        associar(entity, produto, empresa, filial, deposito, localizacao);
        Estoque salvo = repository.save(entity);
        estoqueDomainService.registrarEntradaInicial(salvo);
        return mapper.toResponse(salvo);
    }

    @Override
    @Transactional
    public EstoqueResponse atualizar(UUID id, EstoqueRequest request) {
        Estoque entity = buscarEntidade(id);
        Produto produto = buscarProdutoAtivo(request.produtoId());
        Empresa empresa = buscarEmpresaAtiva(request.empresaId());
        Filial filial = buscarFilialAtiva(request.filialId());
        Deposito deposito = buscarDepositoAtivo(request.depositoId());
        LocalizacaoEstoque localizacao = buscarLocalizacaoAtiva(request.localizacaoId());

        validator.validarAtualizacao(entity, request, filial, deposito, localizacao);

        BigDecimal saldoAnterior = entity.getQuantidade();
        associar(entity, produto, empresa, filial, deposito, localizacao);
        mapper.updateEntity(entity, request);
        estoqueDomainService.ajuste(entity, saldoAnterior, MovimentacaoOrigem.API, entity.getId(),
                null, "Ajuste manual do saldo de estoque");
        return mapper.toResponse(entity);
    }

    private void associar(
            Estoque entity,
            Produto produto,
            Empresa empresa,
            Filial filial,
            Deposito deposito,
            LocalizacaoEstoque localizacao
    ) {
        entity.setProduto(produto);
        entity.setEmpresa(empresa);
        entity.setFilial(filial);
        entity.setDeposito(deposito);
        entity.setLocalizacao(localizacao);
    }

    private Produto buscarProdutoAtivo(UUID id) {
        return produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    private Empresa buscarEmpresaAtiva(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    private Filial buscarFilialAtiva(UUID id) {
        if (id == null) {
            return null;
        }
        return filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filial nao encontrada"));
    }

    private Deposito buscarDepositoAtivo(UUID id) {
        return depositoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));
    }

    private LocalizacaoEstoque buscarLocalizacaoAtiva(UUID id) {
        return localizacaoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Localizacao nao encontrada"));
    }

    private void validarParametrosSaldo(UUID produtoId, UUID empresaId, UUID depositoId, UUID localizacaoId) {
        if (produtoId == null || empresaId == null || depositoId == null || localizacaoId == null) {
            throw new BusinessException("Produto, empresa, deposito e localizacao sao obrigatorios para consultar saldo");
        }
    }
}
