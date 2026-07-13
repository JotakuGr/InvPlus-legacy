package br.com.invplus.modules.estoque.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.estoque.dto.request.DepositoRequest;
import br.com.invplus.modules.estoque.dto.response.DepositoResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.mapper.DepositoMapper;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.specification.DepositoSpecification;
import br.com.invplus.modules.estoque.validator.DepositoValidator;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.service.UsuarioEmpresaAccessValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para Depositos do modulo Estoque.
 */
@Service
public class DepositoService extends BaseCrudService<Deposito, DepositoRequest, DepositoResponse> {

    private final DepositoRepository repository;
    private final DepositoMapper mapper;
    private final DepositoValidator validator;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final EmpresaContext empresaContext;
    private final UsuarioEmpresaAccessValidator accessValidator;

    public DepositoService(
            DepositoRepository repository,
            DepositoMapper mapper,
            DepositoValidator validator,
            EmpresaRepository empresaRepository,
            FilialRepository filialRepository,
            EmpresaContext empresaContext,
            UsuarioEmpresaAccessValidator accessValidator
    ) {
        super(repository, mapper, "Deposito");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.empresaRepository = empresaRepository;
        this.filialRepository = filialRepository;
        this.empresaContext = empresaContext;
        this.accessValidator = accessValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepositoResponse> listar(Pageable pageable) {
        return listarComFiltros(null, null, null, null, null, null, null, null, pageable);
    }

    /**
     * Lista depositos com filtros usados pelas telas operacionais de estoque.
     */
    @Transactional(readOnly = true)
    public Page<DepositoResponse> listarComFiltros(
            UUID filialId,
            String codigo,
            String nome,
            TipoDeposito tipoDeposito,
            Boolean principal,
            Boolean controlaEstoque,
            Boolean bloqueado,
            Boolean ativo,
            Pageable pageable
    ) {
        UUID empresaId = empresaContext.getEmpresaId();
        validarAcessoFilial(filialId, empresaId);
        Specification<Deposito> specification = DepositoSpecification.pertenceEmpresa(empresaId)
                .and(DepositoSpecification.naoDeletado())
                .and(DepositoSpecification.ativo(ativo))
                .and(DepositoSpecification.filialIgual(filialId))
                .and(DepositoSpecification.codigoContem(codigo))
                .and(DepositoSpecification.nomeContem(nome))
                .and(DepositoSpecification.tipoIgual(tipoDeposito))
                .and(DepositoSpecification.principalIgual(principal))
                .and(DepositoSpecification.controlaEstoqueIgual(controlaEstoque))
                .and(DepositoSpecification.bloqueadoIgual(bloqueado));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DepositoResponse buscar(UUID id) {
        return mapper.toResponse(buscarNoContexto(id));
    }

    /**
     * Cria deposito associando empresa obrigatoria e filial opcional ja validadas.
     */
    @Override
    @Transactional
    public DepositoResponse criar(DepositoRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        validator.validarCriacao(empresaId, request);
        Empresa empresa = buscarEmpresaAtiva(empresaId);
        Filial filial = buscarFilialAtiva(request.filialId(), empresaId);
        validarAcessoFilial(request.filialId(), empresaId);

        Deposito deposito = mapper.toEntity(request);
        deposito.setEmpresa(empresa);
        deposito.setFilial(filial);
        return mapper.toResponse(repository.save(deposito));
    }

    /**
     * Atualiza deposito mantendo unicidade de codigo e principal por empresa.
     */
    @Override
    @Transactional
    public DepositoResponse atualizar(UUID id, DepositoRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        Deposito deposito = buscarNoContexto(id);
        validator.validarAtualizacao(empresaId, deposito, request);
        Empresa empresa = buscarEmpresaAtiva(empresaId);
        Filial filial = buscarFilialAtiva(request.filialId(), empresaId);
        validarAcessoFilial(request.filialId(), empresaId);

        deposito.setEmpresa(empresa);
        deposito.setFilial(filial);
        mapper.updateEntity(deposito, request);
        return mapper.toResponse(deposito);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        buscarNoContexto(id).excluirLogicamente();
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

    private Deposito buscarNoContexto(UUID id) {
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));
    }

    private void validarAcessoFilial(UUID filialId, UUID empresaId) {
        if (filialId != null) {
            accessValidator.validarContexto(empresaContext.getUsuarioId(), empresaId, filialId);
        }
    }
}
