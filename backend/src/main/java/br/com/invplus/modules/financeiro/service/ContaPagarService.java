package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaPagarRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaPagarResponse;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.mapper.ContaPagarMapper;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.specification.ContaPagarSpecification;
import br.com.invplus.modules.financeiro.validator.ContaPagarValidator;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
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
 * Orquestra cadastro, consulta, alteracao e cancelamento de contas a pagar.
 */
@Service
public class ContaPagarService extends BaseCrudService<ContaPagar, ContaPagarRequest, ContaPagarResponse> {

    private final ContaPagarRepository repository;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final FornecedorRepository fornecedorRepository;
    private final EntradaMercadoriaRepository entradaMercadoriaRepository;
    private final CentroCustoRepository centroCustoRepository;
    private final PlanoContaRepository planoContaRepository;
    private final ContaPagarMapper mapper;
    private final ContaPagarValidator validator;

    public ContaPagarService(
            ContaPagarRepository repository,
            EmpresaRepository empresaRepository,
            FilialRepository filialRepository,
            FornecedorRepository fornecedorRepository,
            EntradaMercadoriaRepository entradaMercadoriaRepository,
            CentroCustoRepository centroCustoRepository,
            PlanoContaRepository planoContaRepository,
            ContaPagarMapper mapper,
            ContaPagarValidator validator
    ) {
        super(repository, mapper, "Conta a pagar");
        this.repository = repository;
        this.empresaRepository = empresaRepository;
        this.filialRepository = filialRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.entradaMercadoriaRepository = entradaMercadoriaRepository;
        this.centroCustoRepository = centroCustoRepository;
        this.planoContaRepository = planoContaRepository;
        this.mapper = mapper;
        this.validator = validator;
    }

    /**
     * Lista contas nao excluidas aplicando somente os filtros informados.
     */
    @Transactional(readOnly = true)
    public Page<ContaPagarResponse> listarComFiltros(UUID empresaId, UUID filialId, UUID fornecedorId,
            UUID entradaMercadoriaId, UUID centroCustoId, UUID planoContaId, String numero, StatusContaPagar status, LocalDate dataEmissaoDe,
            LocalDate dataEmissaoAte, Boolean ativo, Pageable pageable) {
        Specification<ContaPagar> specification = ContaPagarSpecification.naoDeletado();
        specification = and(specification, ContaPagarSpecification.ativo(ativo));
        specification = and(specification, ContaPagarSpecification.empresaIgual(empresaId));
        specification = and(specification, ContaPagarSpecification.filialIgual(filialId));
        specification = and(specification, ContaPagarSpecification.fornecedorIgual(fornecedorId));
        specification = and(specification, ContaPagarSpecification.entradaMercadoriaIgual(entradaMercadoriaId));
        specification = and(specification, ContaPagarSpecification.centroCustoIgual(centroCustoId));
        specification = and(specification, ContaPagarSpecification.planoContaIgual(planoContaId));
        specification = and(specification, ContaPagarSpecification.numeroContem(numero));
        specification = and(specification, ContaPagarSpecification.statusIgual(status));
        specification = and(specification, ContaPagarSpecification.emissaoDe(dataEmissaoDe));
        specification = and(specification, ContaPagarSpecification.emissaoAte(dataEmissaoAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ContaPagarResponse criar(ContaPagarRequest request) {
        validator.validarCriacao(request);
        Relacionamentos relacionamentos = buscarRelacionamentos(request);
        validarEntradaMercadoria(relacionamentos);
        validator.validarCentroCusto(relacionamentos.empresa(), relacionamentos.centroCusto());
        validator.validarPlanoConta(relacionamentos.empresa(), relacionamentos.planoConta());

        ContaPagar conta = new ContaPagar();
        conta.abrir(relacionamentos.empresa(), relacionamentos.filial(), relacionamentos.fornecedor(),
                relacionamentos.entradaMercadoria(), relacionamentos.centroCusto(), relacionamentos.planoConta(),
                request.numero(), request.descricao(),
                request.dataEmissao(), request.valorTotal(), request.observacao());
        return mapper.toResponse(repository.save(conta));
    }

    @Override
    @Transactional
    public ContaPagarResponse atualizar(UUID id, ContaPagarRequest request) {
        ContaPagar conta = buscarEntidade(id);
        validator.validarAtualizacao(id, conta, request);
        Relacionamentos relacionamentos = buscarRelacionamentos(request);
        validarEntradaMercadoria(relacionamentos);
        validator.validarCentroCusto(relacionamentos.empresa(), relacionamentos.centroCusto());
        validator.validarPlanoConta(relacionamentos.empresa(), relacionamentos.planoConta());

        conta.atualizar(relacionamentos.empresa(), relacionamentos.filial(), relacionamentos.fornecedor(),
                relacionamentos.entradaMercadoria(), relacionamentos.centroCusto(), relacionamentos.planoConta(),
                request.numero(), request.descricao(),
                request.dataEmissao(), request.valorTotal(), request.observacao());
        return mapper.toResponse(conta);
    }

    /**
     * Cancela uma conta editavel e conserva o titulo para auditoria.
     */
    @Transactional
    public ContaPagarResponse cancelar(UUID id) {
        ContaPagar conta = buscarEntidade(id);
        validator.validarCancelamento(conta);
        conta.cancelar();
        return mapper.toResponse(conta);
    }

    private Relacionamentos buscarRelacionamentos(ContaPagarRequest request) {
        Empresa empresa = empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
        Filial filial = buscarFilial(request.filialId(), empresa);
        Fornecedor fornecedor = fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.fornecedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado"));
        EntradaMercadoria entrada = buscarEntradaMercadoria(request.entradaMercadoriaId());
        CentroCusto centroCusto = buscarCentroCusto(request.centroCustoId());
        PlanoConta planoConta = buscarPlanoConta(request.planoContaId());
        return new Relacionamentos(empresa, filial, fornecedor, entrada, centroCusto, planoConta);
    }

    private Filial buscarFilial(UUID id, Empresa empresa) {
        if (id == null) {
            return null;
        }
        Filial filial = filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filial nao encontrada"));
        if (!filial.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Filial informada nao pertence a empresa");
        }
        return filial;
    }

    private EntradaMercadoria buscarEntradaMercadoria(UUID id) {
        if (id == null) {
            return null;
        }
        return entradaMercadoriaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrada de mercadoria nao encontrada"));
    }

    private CentroCusto buscarCentroCusto(UUID id) {
        return id == null ? null : centroCustoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de custo nao encontrado"));
    }

    private PlanoConta buscarPlanoConta(UUID id) {
        return id == null ? null : planoContaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de conta nao encontrado"));
    }

    private void validarEntradaMercadoria(Relacionamentos relacionamentos) {
        EntradaMercadoria entrada = relacionamentos.entradaMercadoria();
        if (entrada == null) {
            return;
        }
        if (!entrada.getEmpresa().getId().equals(relacionamentos.empresa().getId())) {
            throw new InvalidOperationException("Entrada de mercadoria nao pertence a empresa");
        }
        if (!entrada.getFornecedor().getId().equals(relacionamentos.fornecedor().getId())) {
            throw new InvalidOperationException("Entrada de mercadoria nao pertence ao fornecedor");
        }
        UUID filialConta = relacionamentos.filial() == null ? null : relacionamentos.filial().getId();
        UUID filialEntrada = entrada.getFilial() == null ? null : entrada.getFilial().getId();
        if (!java.util.Objects.equals(filialConta, filialEntrada)) {
            throw new InvalidOperationException("Entrada de mercadoria nao pertence a filial informada");
        }
    }

    private Specification<ContaPagar> and(
            Specification<ContaPagar> base,
            Specification<ContaPagar> filtro
    ) {
        return filtro == null ? base : base.and(filtro);
    }

    private record Relacionamentos(
            Empresa empresa,
            Filial filial,
            Fornecedor fornecedor,
            EntradaMercadoria entradaMercadoria,
            CentroCusto centroCusto,
            PlanoConta planoConta
    ) {
    }
}
