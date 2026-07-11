package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.dto.response.MovimentoFinanceiroResponse;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.entity.Recebimento;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.mapper.MovimentoFinanceiroMapper;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.modules.financeiro.specification.MovimentoFinanceiroSpecification;
import br.com.invplus.modules.financeiro.validator.MovimentoFinanceiroValidator;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
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
 * Centraliza a criacao e o cancelamento dos registros do fluxo financeiro.
 */
@Service
public class MovimentoFinanceiroService
        extends BaseCrudService<MovimentoFinanceiro, MovimentoFinanceiroRequest, MovimentoFinanceiroResponse> {

    private final MovimentoFinanceiroRepository repository;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final MovimentoFinanceiroMapper mapper;
    private final CentroCustoRepository centroCustoRepository;
    private final PlanoContaRepository planoContaRepository;
    private final MovimentoFinanceiroValidator validator;
    private final ContaBancariaRepository contaBancariaRepository;

    public MovimentoFinanceiroService(MovimentoFinanceiroRepository repository,
            EmpresaRepository empresaRepository, FilialRepository filialRepository,
            FormaPagamentoRepository formaPagamentoRepository, ClienteRepository clienteRepository,
            FornecedorRepository fornecedorRepository, MovimentoFinanceiroMapper mapper,
            MovimentoFinanceiroValidator validator, CentroCustoRepository centroCustoRepository,
            PlanoContaRepository planoContaRepository, ContaBancariaRepository contaBancariaRepository) {
        super(repository, mapper, "Movimento financeiro");
        this.repository = repository;
        this.empresaRepository = empresaRepository;
        this.filialRepository = filialRepository;
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.clienteRepository = clienteRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.mapper = mapper;
        this.validator = validator;
        this.centroCustoRepository = centroCustoRepository;
        this.planoContaRepository = planoContaRepository;
        this.contaBancariaRepository = contaBancariaRepository;
    }

    @Override
    @Transactional
    public MovimentoFinanceiroResponse criar(MovimentoFinanceiroRequest request) {
        return criarManual(request);
    }

    @Transactional
    public MovimentoFinanceiroResponse criarManual(MovimentoFinanceiroRequest request) {
        Empresa empresa = buscarEmpresa(request.empresaId());
        Filial filial = buscarFilial(request.filialId());
        FormaPagamento formaPagamento = buscarFormaPagamento(request.formaPagamentoId());
        Cliente cliente = buscarCliente(request.clienteId());
        Fornecedor fornecedor = buscarFornecedor(request.fornecedorId());
        CentroCusto centroCusto = buscarCentroCusto(request.centroCustoId());
        PlanoConta planoConta = buscarPlanoConta(request.planoContaId());
        ContaBancaria contaBancaria = buscarContaBancaria(request.contaBancariaId());
        validarContaBancaria(contaBancaria, empresa, filial);
        validator.validarManual(request, empresa, filial, formaPagamento, cliente, fornecedor, centroCusto, planoConta);

        MovimentoFinanceiro movimento = new MovimentoFinanceiro();
        movimento.registrar(empresa, filial, request.tipoMovimento(), request.origem(), request.origemId(),
                formaPagamento, cliente, fornecedor, centroCusto, planoConta, contaBancaria,
                request.valor(), request.dataMovimento(),
                request.descricao(), request.observacao());
        return mapper.toResponse(repository.save(movimento));
    }

    @Transactional
    public MovimentoFinanceiro registrarRecebimento(Recebimento recebimento) {
        return registrarRecebimento(recebimento, null);
    }

    @Transactional
    public MovimentoFinanceiro registrarRecebimento(Recebimento recebimento, ContaBancaria contaBancaria) {
        validarOrigemUnica(OrigemMovimentoFinanceiro.RECEBIMENTO, recebimento.getId());
        MovimentoFinanceiro movimento = new MovimentoFinanceiro();
        movimento.registrar(recebimento.getEmpresa(), recebimento.getFilial(), TipoMovimentoFinanceiro.ENTRADA,
                OrigemMovimentoFinanceiro.RECEBIMENTO, recebimento.getId(), recebimento.getFormaPagamento(),
                recebimento.getCliente(), null,
                recebimento.getContaReceber() == null ? null : recebimento.getContaReceber().getCentroCusto(),
                recebimento.getContaReceber() == null ? null : recebimento.getContaReceber().getPlanoConta(), contaBancaria,
                recebimento.getValorLiquido(), recebimento.getDataRecebimento(),
                "Recebimento " + recebimento.getId(), recebimento.getObservacao());
        return repository.save(movimento);
    }

    @Transactional
    public MovimentoFinanceiro registrarPagamento(Pagamento pagamento) {
        return registrarPagamento(pagamento, null);
    }

    @Transactional
    public MovimentoFinanceiro registrarPagamento(Pagamento pagamento, ContaBancaria contaBancaria) {
        validarOrigemUnica(OrigemMovimentoFinanceiro.PAGAMENTO, pagamento.getId());
        MovimentoFinanceiro movimento = new MovimentoFinanceiro();
        movimento.registrar(pagamento.getEmpresa(), pagamento.getFilial(), TipoMovimentoFinanceiro.SAIDA,
                OrigemMovimentoFinanceiro.PAGAMENTO, pagamento.getId(), pagamento.getFormaPagamento(),
                null, pagamento.getFornecedor(),
                pagamento.getContaPagar() == null ? null : pagamento.getContaPagar().getCentroCusto(),
                pagamento.getContaPagar() == null ? null : pagamento.getContaPagar().getPlanoConta(), contaBancaria,
                pagamento.getValorLiquido(), pagamento.getDataPagamento(),
                "Pagamento " + pagamento.getId(), pagamento.getObservacao());
        return repository.save(movimento);
    }

    @Transactional
    public MovimentoFinanceiroResponse cancelar(UUID id) {
        MovimentoFinanceiro movimento = buscarEntidade(id);
        validator.validarCancelamento(movimento);
        movimento.cancelar();
        return mapper.toResponse(movimento);
    }

    @Transactional
    public void cancelarPorOrigem(OrigemMovimentoFinanceiro origem, UUID origemId) {
        MovimentoFinanceiro movimento = repository
                .findByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                        origem, origemId, StatusMovimentoFinanceiro.ATIVO)
                .orElseThrow(() -> new ResourceNotFoundException("Movimento financeiro da origem nao encontrado"));
        validator.validarCancelamento(movimento);
        movimento.cancelar();
    }

    @Transactional(readOnly = true)
    public Page<MovimentoFinanceiroResponse> listarComFiltros(UUID empresaId, UUID filialId,
            TipoMovimentoFinanceiro tipoMovimento, OrigemMovimentoFinanceiro origem, UUID origemId,
            UUID formaPagamentoId, UUID clienteId, UUID fornecedorId, UUID centroCustoId, UUID planoContaId,
            StatusMovimentoFinanceiro status,
            LocalDate dataMovimentoDe, LocalDate dataMovimentoAte, Boolean ativo, Pageable pageable) {
        Specification<MovimentoFinanceiro> specification = MovimentoFinanceiroSpecification.naoDeletado();
        specification = and(specification, MovimentoFinanceiroSpecification.empresaIgual(empresaId));
        specification = and(specification, MovimentoFinanceiroSpecification.filialIgual(filialId));
        specification = and(specification, MovimentoFinanceiroSpecification.tipoIgual(tipoMovimento));
        specification = and(specification, MovimentoFinanceiroSpecification.origemIgual(origem));
        specification = and(specification, MovimentoFinanceiroSpecification.origemIdIgual(origemId));
        specification = and(specification, MovimentoFinanceiroSpecification.formaPagamentoIgual(formaPagamentoId));
        specification = and(specification, MovimentoFinanceiroSpecification.clienteIgual(clienteId));
        specification = and(specification, MovimentoFinanceiroSpecification.fornecedorIgual(fornecedorId));
        specification = and(specification, MovimentoFinanceiroSpecification.centroCustoIgual(centroCustoId));
        specification = and(specification, MovimentoFinanceiroSpecification.planoContaIgual(planoContaId));
        specification = and(specification, MovimentoFinanceiroSpecification.statusIgual(status));
        specification = and(specification, MovimentoFinanceiroSpecification.movimentoDe(dataMovimentoDe));
        specification = and(specification, MovimentoFinanceiroSpecification.movimentoAte(dataMovimentoAte));
        specification = and(specification, MovimentoFinanceiroSpecification.ativo(ativo));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    private void validarOrigemUnica(OrigemMovimentoFinanceiro origem, UUID origemId) {
        boolean existe = repository.existsByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                origem, origemId, StatusMovimentoFinanceiro.ATIVO);
        if (existe) {
            throw new ConflictException("Ja existe movimento financeiro ativo para esta origem");
        }
    }

    private Empresa buscarEmpresa(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    private Filial buscarFilial(UUID id) {
        return id == null ? null : filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filial nao encontrada"));
    }

    private FormaPagamento buscarFormaPagamento(UUID id) {
        return formaPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forma de pagamento nao encontrada"));
    }

    private Cliente buscarCliente(UUID id) {
        return id == null ? null : clienteRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));
    }

    private Fornecedor buscarFornecedor(UUID id) {
        return id == null ? null : fornecedorRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado"));
    }

    private CentroCusto buscarCentroCusto(UUID id) {
        return id == null ? null : centroCustoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de custo nao encontrado"));
    }

    private PlanoConta buscarPlanoConta(UUID id) {
        return id == null ? null : planoContaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de conta nao encontrado"));
    }

    private ContaBancaria buscarContaBancaria(UUID id) {
        return id == null ? null : contaBancariaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancaria nao encontrada"));
    }

    private void validarContaBancaria(ContaBancaria conta, Empresa empresa, Filial filial) {
        if (conta == null) return;
        if (!conta.getEmpresa().getId().equals(empresa.getId())) {
            throw new br.com.invplus.shared.domain.exception.InvalidOperationException(
                    "Conta bancaria deve pertencer a mesma empresa do movimento");
        }
        if (conta.getFilial() != null
                && (filial == null || !conta.getFilial().getId().equals(filial.getId()))) {
            throw new br.com.invplus.shared.domain.exception.InvalidOperationException(
                    "Conta bancaria deve ser compativel com a filial do movimento");
        }
    }

    private Specification<MovimentoFinanceiro> and(Specification<MovimentoFinanceiro> base,
            Specification<MovimentoFinanceiro> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
