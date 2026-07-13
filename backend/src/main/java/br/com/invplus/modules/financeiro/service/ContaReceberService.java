package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaReceberRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaReceberResponse;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.mapper.ContaReceberMapper;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.specification.ContaReceberSpecification;
import br.com.invplus.modules.financeiro.validator.ContaReceberValidator;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.vendas.entity.VendaFaturamento;
import br.com.invplus.modules.vendas.repository.VendaFaturamentoRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import br.com.invplus.security.context.EmpresaContext;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra o ciclo inicial das contas a receber.
 */
@Service
public class ContaReceberService extends BaseCrudService<ContaReceber, ContaReceberRequest, ContaReceberResponse> {

    private final ContaReceberRepository repository;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final ClienteRepository clienteRepository;
    private final VendaFaturamentoRepository vendaFaturamentoRepository;
    private final ParcelaReceberService parcelaReceberService;
    private final CentroCustoRepository centroCustoRepository;
    private final PlanoContaRepository planoContaRepository;
    private final ContaReceberMapper mapper;
    private final ContaReceberValidator validator;
    private final EmpresaContext empresaContext;

    public ContaReceberService(
            ContaReceberRepository repository,
            EmpresaRepository empresaRepository,
            FilialRepository filialRepository,
            ClienteRepository clienteRepository,
            VendaFaturamentoRepository vendaFaturamentoRepository,
            ParcelaReceberService parcelaReceberService,
            CentroCustoRepository centroCustoRepository,
            PlanoContaRepository planoContaRepository,
            ContaReceberMapper mapper,
            ContaReceberValidator validator,
            EmpresaContext empresaContext
    ) {
        super(repository, mapper, "Conta a receber");
        this.repository = repository;
        this.empresaRepository = empresaRepository;
        this.filialRepository = filialRepository;
        this.clienteRepository = clienteRepository;
        this.vendaFaturamentoRepository = vendaFaturamentoRepository;
        this.parcelaReceberService = parcelaReceberService;
        this.centroCustoRepository = centroCustoRepository;
        this.planoContaRepository = planoContaRepository;
        this.mapper = mapper;
        this.validator = validator;
        this.empresaContext = empresaContext;
    }

    @Transactional(readOnly = true)
    public Page<ContaReceberResponse> listarComFiltros(UUID filialId, UUID clienteId,
            UUID vendaFaturamentoId, UUID centroCustoId, UUID planoContaId, String numero, StatusContaReceber status, LocalDate dataEmissaoDe,
            LocalDate dataEmissaoAte, Boolean ativo, Pageable pageable) {
        UUID empresaId = empresaContext.getEmpresaId();
        Specification<ContaReceber> specification = ContaReceberSpecification.naoDeletado();
        specification = and(specification, ContaReceberSpecification.ativo(ativo));
        specification = and(specification, ContaReceberSpecification.empresaIgual(empresaId));
        specification = and(specification, ContaReceberSpecification.filialIgual(filialId));
        specification = and(specification, ContaReceberSpecification.clienteIgual(clienteId));
        specification = and(specification, ContaReceberSpecification.vendaFaturamentoIgual(vendaFaturamentoId));
        specification = and(specification, ContaReceberSpecification.centroCustoIgual(centroCustoId));
        specification = and(specification, ContaReceberSpecification.planoContaIgual(planoContaId));
        specification = and(specification, ContaReceberSpecification.numeroContem(numero));
        specification = and(specification, ContaReceberSpecification.statusIgual(status));
        specification = and(specification, ContaReceberSpecification.emissaoDe(dataEmissaoDe));
        specification = and(specification, ContaReceberSpecification.emissaoAte(dataEmissaoAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ContaReceberResponse criar(ContaReceberRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        validator.validarCriacao(request, empresaId);
        Empresa empresa = buscarEmpresa(empresaId);
        Filial filial = buscarFilial(request.filialId(), empresa);
        Cliente cliente = buscarCliente(request.clienteId());
        VendaFaturamento vendaFaturamento = buscarFaturamento(request.vendaFaturamentoId());
        CentroCusto centroCusto = buscarCentroCusto(request.centroCustoId());
        PlanoConta planoConta = buscarPlanoConta(request.planoContaId());
        validator.validarCentroCusto(empresa, centroCusto);
        validator.validarPlanoConta(empresa, planoConta);

        ContaReceber conta = new ContaReceber();
        conta.abrir(empresa, filial, cliente, vendaFaturamento, centroCusto, planoConta,
                request.numero(), request.descricao(), request.dataEmissao(), request.valorTotal(), request.observacao());
        return mapper.toResponse(repository.save(conta));
    }

    @Override
    @Transactional
    public ContaReceberResponse atualizar(UUID id, ContaReceberRequest request) {
        ContaReceber conta = buscarEntidade(id);
        UUID empresaId = empresaContext.getEmpresaId();
        validator.validarAtualizacao(id, conta, request, empresaId);
        Empresa empresa = buscarEmpresa(empresaId);
        Filial filial = buscarFilial(request.filialId(), empresa);
        Cliente cliente = buscarCliente(request.clienteId());
        VendaFaturamento vendaFaturamento = buscarFaturamento(request.vendaFaturamentoId());
        CentroCusto centroCusto = buscarCentroCusto(request.centroCustoId());
        PlanoConta planoConta = buscarPlanoConta(request.planoContaId());
        validator.validarCentroCusto(empresa, centroCusto);
        validator.validarPlanoConta(empresa, planoConta);

        conta.atualizar(empresa, filial, cliente, vendaFaturamento, centroCusto, planoConta,
                request.numero(), request.descricao(), request.dataEmissao(), request.valorTotal(), request.observacao());
        return mapper.toResponse(conta);
    }

    @Transactional
    public ContaReceberResponse cancelar(UUID id) {
        ContaReceber conta = buscarEntidade(id);
        validator.validarCancelamento(conta);
        conta.cancelar();
        return mapper.toResponse(conta);
    }

    /**
     * Cria o titulo financeiro de uma venda faturada e gera suas parcelas pela condicao informada.
     */
    @Transactional
    public ContaReceberResponse gerarPorVendaFaturamento(VendaFaturamento vendaFaturamento, UUID condicaoPagamentoId) {
        return gerarPorVendaFaturamento(vendaFaturamento, condicaoPagamentoId, null);
    }

    /**
     * Variante interna que permite personalizar a descricao da conta gerada pelo faturamento.
     */
    @Transactional
    public ContaReceberResponse gerarPorVendaFaturamento(
            VendaFaturamento vendaFaturamento,
            UUID condicaoPagamentoId,
            String descricaoFinanceira
    ) {
        if (vendaFaturamento == null || vendaFaturamento.getId() == null) {
            throw new InvalidOperationException("Faturamento de venda e obrigatorio para gerar conta a receber");
        }
        if (condicaoPagamentoId == null) {
            throw new InvalidOperationException("Condicao de pagamento e obrigatoria para gerar conta a receber");
        }
        if (vendaFaturamento.getValorTotal() == null || vendaFaturamento.getValorTotal().signum() <= 0) {
            throw new InvalidOperationException("Valor total do faturamento deve ser maior que zero");
        }
        if (vendaFaturamento.getContaReceber() != null
                || repository.existsByVendaFaturamentoIdAndAtivoTrueAndDeletedAtIsNull(vendaFaturamento.getId())) {
            throw new ConflictException("Faturamento de venda ja possui conta a receber");
        }

        ContaReceber conta = new ContaReceber();
        conta.abrir(
                vendaFaturamento.getEmpresa(),
                vendaFaturamento.getFilial(),
                vendaFaturamento.getCliente(),
                vendaFaturamento,
                numeroFinanceiro(vendaFaturamento),
                descricaoFinanceira(descricaoFinanceira, vendaFaturamento),
                vendaFaturamento.getDataFaturamento().toLocalDate(),
                vendaFaturamento.getValorTotal(),
                vendaFaturamento.getObservacao()
        );
        ContaReceber salva = repository.save(conta);
        parcelaReceberService.gerarPorContaReceber(
                salva.getId(),
                condicaoPagamentoId,
                vendaFaturamento.getDataFaturamento().toLocalDate()
        );
        return mapper.toResponse(salva);
    }

    private Empresa buscarEmpresa(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public ContaReceber buscarEntidade(UUID id) {
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta a receber nao encontrada"));
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

    private Cliente buscarCliente(UUID id) {
        return clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));
    }

    private VendaFaturamento buscarFaturamento(UUID id) {
        if (id == null) {
            return null;
        }
        return vendaFaturamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faturamento de venda nao encontrado"));
    }

    private CentroCusto buscarCentroCusto(UUID id) {
        return id == null ? null : centroCustoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de custo nao encontrado"));
    }

    private PlanoConta buscarPlanoConta(UUID id) {
        return id == null ? null : planoContaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de conta nao encontrado"));
    }

    private String numeroFinanceiro(VendaFaturamento faturamento) {
        String base = "CR-VF-" + faturamento.getNumero() + "-" + faturamento.getId().toString().substring(0, 8);
        return base.length() <= 50 ? base : base.substring(0, 50);
    }

    private String descricaoFinanceira(String descricaoFinanceira, VendaFaturamento faturamento) {
        if (descricaoFinanceira != null && !descricaoFinanceira.isBlank()) {
            return descricaoFinanceira.trim();
        }
        return "Conta a receber do faturamento " + faturamento.getNumero();
    }

    private Specification<ContaReceber> and(Specification<ContaReceber> base, Specification<ContaReceber> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
