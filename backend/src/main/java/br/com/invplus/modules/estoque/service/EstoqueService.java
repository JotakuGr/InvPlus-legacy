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
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.service.UsuarioEmpresaAccessValidator;
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
    private final EmpresaContext empresaContext;
    private final UsuarioEmpresaAccessValidator accessValidator;

    public EstoqueService(
            EstoqueRepository repository,
            EstoqueMapper mapper,
            EstoqueValidator validator,
            ProdutoRepository produtoRepository,
            EmpresaRepository empresaRepository,
            FilialRepository filialRepository,
            DepositoRepository depositoRepository,
            LocalizacaoEstoqueRepository localizacaoRepository,
            EstoqueDomainService estoqueDomainService,
            EmpresaContext empresaContext,
            UsuarioEmpresaAccessValidator accessValidator
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
        this.empresaContext = empresaContext;
        this.accessValidator = accessValidator;
    }

    @Transactional(readOnly = true)
    public Page<EstoqueResponse> listarComFiltros(
            UUID produtoId,
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
        UUID empresaId = empresaContext.getEmpresaId();
        validarAcessoFilial(filialId, empresaId);
        Specification<Estoque> specification = EstoqueSpecification.pertenceEmpresa(empresaId)
                .and(EstoqueSpecification.naoDeletado())
                .and(EstoqueSpecification.ativo(ativo))
                .and(EstoqueSpecification.produtoIgual(produtoId))
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
    public EstoqueResponse buscarSaldo(UUID produtoId, UUID depositoId, UUID localizacaoId) {
        UUID empresaId = empresaContext.getEmpresaId();
        validarParametrosSaldo(produtoId, depositoId, localizacaoId);
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
    @Transactional(readOnly = true)
    public EstoqueResponse buscar(UUID id) {
        return mapper.toResponse(buscarNoContexto(id));
    }

    @Override
    @Transactional
    public EstoqueResponse criar(EstoqueRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        Produto produto = buscarProdutoAtivo(request.produtoId());
        Empresa empresa = buscarEmpresaAtiva(empresaId);
        Filial filial = buscarFilialAtiva(request.filialId(), empresaId);
        Deposito deposito = buscarDepositoAtivo(request.depositoId(), empresaId);
        LocalizacaoEstoque localizacao = buscarLocalizacaoAtiva(request.localizacaoId(), empresaId);
        validarAcessoFilial(request.filialId(), empresaId);

        validator.validarCriacao(empresaId, request, filial, deposito, localizacao);

        Estoque entity = mapper.toEntity(request);
        associar(entity, produto, empresa, filial, deposito, localizacao);
        Estoque salvo = repository.save(entity);
        estoqueDomainService.registrarEntradaInicial(salvo);
        return mapper.toResponse(salvo);
    }

    @Override
    @Transactional
    public EstoqueResponse atualizar(UUID id, EstoqueRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        Estoque entity = buscarNoContexto(id);
        Produto produto = buscarProdutoAtivo(request.produtoId());
        Empresa empresa = buscarEmpresaAtiva(empresaId);
        Filial filial = buscarFilialAtiva(request.filialId(), empresaId);
        Deposito deposito = buscarDepositoAtivo(request.depositoId(), empresaId);
        LocalizacaoEstoque localizacao = buscarLocalizacaoAtiva(request.localizacaoId(), empresaId);
        validarAcessoFilial(request.filialId(), empresaId);

        validator.validarAtualizacao(empresaId, entity, request, filial, deposito, localizacao);

        BigDecimal saldoAnterior = entity.getQuantidade();
        associar(entity, produto, empresa, filial, deposito, localizacao);
        mapper.updateEntity(entity, request);
        estoqueDomainService.ajuste(entity, saldoAnterior, MovimentacaoOrigem.API, entity.getId(),
                null, "Ajuste manual do saldo de estoque");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        buscarNoContexto(id).excluirLogicamente();
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

    private Filial buscarFilialAtiva(UUID id, UUID empresaId) {
        if (id == null) {
            return null;
        }
        return filialRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Filial nao encontrada"));
    }

    private Deposito buscarDepositoAtivo(UUID id, UUID empresaId) {
        return depositoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));
    }

    private LocalizacaoEstoque buscarLocalizacaoAtiva(UUID id, UUID empresaId) {
        return localizacaoRepository.findByIdAndDepositoEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Localizacao nao encontrada"));
    }

    private void validarParametrosSaldo(UUID produtoId, UUID depositoId, UUID localizacaoId) {
        if (produtoId == null || depositoId == null || localizacaoId == null) {
            throw new BusinessException("Produto, deposito e localizacao sao obrigatorios para consultar saldo");
        }
    }

    private Estoque buscarNoContexto(UUID id) {
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Estoque nao encontrado"));
    }

    private void validarAcessoFilial(UUID filialId, UUID empresaId) {
        if (filialId != null) {
            accessValidator.validarContexto(empresaContext.getUsuarioId(), empresaId, filialId);
        }
    }
}
