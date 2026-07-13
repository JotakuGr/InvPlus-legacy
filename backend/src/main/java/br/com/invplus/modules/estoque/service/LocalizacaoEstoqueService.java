package br.com.invplus.modules.estoque.service;

import br.com.invplus.modules.estoque.dto.request.LocalizacaoEstoqueRequest;
import br.com.invplus.modules.estoque.dto.response.LocalizacaoEstoqueResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.mapper.LocalizacaoEstoqueMapper;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.estoque.specification.LocalizacaoEstoqueSpecification;
import br.com.invplus.modules.estoque.validator.LocalizacaoEstoqueValidator;
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
 * Regras de negocio para localizacoes internas dos depositos.
 */
@Service
public class LocalizacaoEstoqueService extends BaseCrudService<LocalizacaoEstoque, LocalizacaoEstoqueRequest, LocalizacaoEstoqueResponse> {

    private final LocalizacaoEstoqueRepository repository;
    private final LocalizacaoEstoqueMapper mapper;
    private final LocalizacaoEstoqueValidator validator;
    private final DepositoRepository depositoRepository;
    private final EmpresaContext empresaContext;
    private final UsuarioEmpresaAccessValidator accessValidator;

    public LocalizacaoEstoqueService(
            LocalizacaoEstoqueRepository repository,
            LocalizacaoEstoqueMapper mapper,
            LocalizacaoEstoqueValidator validator,
            DepositoRepository depositoRepository,
            EmpresaContext empresaContext,
            UsuarioEmpresaAccessValidator accessValidator
    ) {
        super(repository, mapper, "LocalizacaoEstoque");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.depositoRepository = depositoRepository;
        this.empresaContext = empresaContext;
        this.accessValidator = accessValidator;
    }

    @Transactional(readOnly = true)
    public Page<LocalizacaoEstoqueResponse> listarComFiltros(
            UUID depositoId,
            String codigo,
            String descricao,
            String rua,
            String corredor,
            String prateleira,
            Boolean principal,
            Boolean bloqueada,
            Boolean ativo,
            Pageable pageable
    ) {
        UUID empresaId = empresaContext.getEmpresaId();
        Specification<LocalizacaoEstoque> specification = LocalizacaoEstoqueSpecification.pertenceEmpresa(empresaId)
                .and(LocalizacaoEstoqueSpecification.naoDeletado())
                .and(LocalizacaoEstoqueSpecification.ativo(ativo))
                .and(LocalizacaoEstoqueSpecification.depositoIgual(depositoId))
                .and(LocalizacaoEstoqueSpecification.codigoContem(codigo))
                .and(LocalizacaoEstoqueSpecification.descricaoContem(descricao))
                .and(LocalizacaoEstoqueSpecification.ruaContem(rua))
                .and(LocalizacaoEstoqueSpecification.corredorContem(corredor))
                .and(LocalizacaoEstoqueSpecification.prateleiraContem(prateleira))
                .and(LocalizacaoEstoqueSpecification.principalIgual(principal))
                .and(LocalizacaoEstoqueSpecification.bloqueadaIgual(bloqueada));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LocalizacaoEstoqueResponse buscar(UUID id) {
        return mapper.toResponse(buscarNoContexto(id));
    }

    @Override
    @Transactional
    public LocalizacaoEstoqueResponse criar(LocalizacaoEstoqueRequest request) {
        Deposito deposito = buscarDepositoAtivo(request.depositoId());
        validator.validarCriacao(request);
        LocalizacaoEstoque entity = mapper.toEntity(request);
        entity.setDeposito(deposito);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public LocalizacaoEstoqueResponse atualizar(UUID id, LocalizacaoEstoqueRequest request) {
        LocalizacaoEstoque entity = buscarNoContexto(id);
        Deposito deposito = buscarDepositoAtivo(request.depositoId());
        validator.validarAtualizacao(entity, request);
        entity.setDeposito(deposito);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        buscarNoContexto(id).excluirLogicamente();
    }

    private Deposito buscarDepositoAtivo(UUID id) {
        UUID empresaId = empresaContext.getEmpresaId();
        Deposito deposito = depositoRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));
        if (deposito.getFilial() != null) {
            accessValidator.validarContexto(
                    empresaContext.getUsuarioId(), empresaId, deposito.getFilial().getId());
        }
        return deposito;
    }

    private LocalizacaoEstoque buscarNoContexto(UUID id) {
        return repository.findByIdAndDepositoEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                        id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("LocalizacaoEstoque nao encontrado"));
    }
}
