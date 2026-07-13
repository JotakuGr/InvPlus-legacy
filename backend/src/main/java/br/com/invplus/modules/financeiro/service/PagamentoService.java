package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.financeiro.dto.request.PagamentoRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarBaixaRequest;
import br.com.invplus.modules.financeiro.dto.response.PagamentoResponse;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusPagamentoFinanceiro;
import br.com.invplus.modules.financeiro.mapper.PagamentoMapper;
import br.com.invplus.modules.financeiro.repository.ParcelaPagarRepository;
import br.com.invplus.modules.financeiro.repository.PagamentoRepository;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.modules.financeiro.specification.PagamentoSpecification;
import br.com.invplus.modules.financeiro.validator.PagamentoValidator;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.exception.AcessoEmpresaNegadoException;

/**
 * Registra eventos de pagamento e delega a baixa financeira para ParcelaPagarService.
 */
@Service
public class PagamentoService extends BaseCrudService<Pagamento, PagamentoRequest, PagamentoResponse> {

    private final PagamentoRepository repository;
    private final ParcelaPagarRepository parcelaPagarRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;
    private final ParcelaPagarService parcelaPagarService;
    private final MovimentoFinanceiroService movimentoFinanceiroService;
    private final PagamentoMapper mapper;
    private final PagamentoValidator validator;
    private final ContaBancariaRepository contaBancariaRepository;
    private final EmpresaContext empresaContext;

    public PagamentoService(
            PagamentoRepository repository,
            ParcelaPagarRepository parcelaPagarRepository,
            FormaPagamentoRepository formaPagamentoRepository,
            ParcelaPagarService parcelaPagarService,
            MovimentoFinanceiroService movimentoFinanceiroService,
            PagamentoMapper mapper,
            PagamentoValidator validator,
            ContaBancariaRepository contaBancariaRepository,
            EmpresaContext empresaContext
    ) {
        super(repository, mapper, "Pagamento");
        this.repository = repository;
        this.parcelaPagarRepository = parcelaPagarRepository;
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.parcelaPagarService = parcelaPagarService;
        this.movimentoFinanceiroService = movimentoFinanceiroService;
        this.mapper = mapper;
        this.validator = validator;
        this.contaBancariaRepository = contaBancariaRepository;
        this.empresaContext = empresaContext;
    }

    @Transactional(readOnly = true)
    public Page<PagamentoResponse> listarComFiltros(UUID parcelaPagarId, UUID contaPagarId,
            UUID formaPagamentoId, UUID empresaId, UUID filialId, UUID fornecedorId,
            StatusPagamentoFinanceiro status, LocalDate dataPagamentoDe, LocalDate dataPagamentoAte,
            Boolean ativo, Pageable pageable) {
        empresaId = resolverEmpresa(empresaId);
        Specification<Pagamento> specification = PagamentoSpecification.naoDeletado();
        specification = and(specification, PagamentoSpecification.ativo(ativo));
        specification = and(specification, PagamentoSpecification.parcelaIgual(parcelaPagarId));
        specification = and(specification, PagamentoSpecification.contaIgual(contaPagarId));
        specification = and(specification, PagamentoSpecification.formaPagamentoIgual(formaPagamentoId));
        specification = and(specification, PagamentoSpecification.empresaIgual(empresaId));
        specification = and(specification, PagamentoSpecification.filialIgual(filialId));
        specification = and(specification, PagamentoSpecification.fornecedorIgual(fornecedorId));
        specification = and(specification, PagamentoSpecification.statusIgual(status));
        specification = and(specification, PagamentoSpecification.pagamentoDe(dataPagamentoDe));
        specification = and(specification, PagamentoSpecification.pagamentoAte(dataPagamentoAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public PagamentoResponse criar(PagamentoRequest request) {
        ParcelaPagar parcela = buscarParcela(request.parcelaPagarId());
        FormaPagamento formaPagamento = buscarFormaPagamento(request.formaPagamentoId());
        ContaBancaria contaBancaria = buscarContaBancaria(request.contaBancariaId(), parcela.getContaPagar().getEmpresa().getId());
        validator.validarCriacao(parcela, formaPagamento, request);

        Pagamento pagamento = new Pagamento();
        pagamento.registrar(parcela, formaPagamento, request.valorPago(), request.juros(), request.multa(),
                request.desconto(), request.dataPagamento(), request.observacao());
        Pagamento salvo = repository.save(pagamento);

        parcelaPagarService.baixar(parcela.getId(), new ParcelaPagarBaixaRequest(
                request.valorPago(),
                request.juros(),
                request.multa(),
                request.desconto(),
                request.dataPagamento(),
                request.observacao()
        ));
        movimentoFinanceiroService.registrarPagamento(salvo, contaBancaria);
        return mapper.toResponse(salvo);
    }

    @Transactional
    public PagamentoResponse cancelar(UUID id) {
        Pagamento pagamento = buscarEntidade(id);
        validator.validarCancelamento(pagamento);
        parcelaPagarService.estornarPagamento(
                pagamento.getParcelaPagar().getId(),
                pagamento.getValorPago(),
                pagamento.getJuros(),
                pagamento.getMulta(),
                pagamento.getDesconto(),
                "Estorno do pagamento " + pagamento.getId()
        );
        pagamento.cancelar();
        movimentoFinanceiroService.cancelarPorOrigem(
                OrigemMovimentoFinanceiro.PAGAMENTO,
                pagamento.getId()
        );
        return mapper.toResponse(pagamento);
    }

    private ParcelaPagar buscarParcela(UUID id) {
        UUID empresaId = empresaAtualOuNull();
        if (empresaId != null) return parcelaPagarRepository
                .findByIdAndContaPagarEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela a pagar nao encontrada"));
        return parcelaPagarRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela a pagar nao encontrada"));
    }

    private FormaPagamento buscarFormaPagamento(UUID id) {
        return formaPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forma de pagamento nao encontrada"));
    }

    private ContaBancaria buscarContaBancaria(UUID id, UUID empresaId) {
        UUID contexto = empresaAtualOuNull();
        if (contexto != null && !contexto.equals(empresaId)) {
            throw new ResourceNotFoundException("Parcela a pagar nao encontrada");
        }
        UUID empresaEfetiva = contexto == null ? empresaId : contexto;
        ContaBancaria conta = id == null
                ? contaBancariaRepository.findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(empresaEfetiva)
                        .orElse(null)
                : contaBancariaRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaEfetiva)
                        .orElseThrow(() -> new ResourceNotFoundException("Conta bancaria nao encontrada"));
        if (conta == null) return null;
        if (!conta.getEmpresa().getId().equals(empresaId)) {
            throw new br.com.invplus.shared.domain.exception.InvalidOperationException(
                    "Conta bancaria deve pertencer a mesma empresa do pagamento");
        }
        return conta;
    }

    @Override
    public Pagamento buscarEntidade(UUID id) {
        UUID empresaId = empresaAtualOuNull();
        if (empresaId == null) return super.buscarEntidade(id);
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado"));
    }

    private UUID resolverEmpresa(UUID recebida) {
        UUID contexto = empresaAtualOuNull();
        if (contexto == null) return recebida;
        if (recebida != null && !contexto.equals(recebida)) throw new AcessoEmpresaNegadoException();
        return contexto;
    }

    private UUID empresaAtualOuNull() {
        if (empresaContext == null) return null;
        try { return empresaContext.getEmpresaId(); }
        catch (br.com.invplus.security.exception.ContextoEmpresarialAusenteException
                | org.springframework.security.authentication.AuthenticationCredentialsNotFoundException ex) { return null; }
    }

    private Specification<Pagamento> and(Specification<Pagamento> base, Specification<Pagamento> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
