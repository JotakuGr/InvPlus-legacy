package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.financeiro.dto.request.BaixaParcelaReceberRequest;
import br.com.invplus.modules.financeiro.dto.request.RecebimentoRequest;
import br.com.invplus.modules.financeiro.dto.response.RecebimentoResponse;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.Recebimento;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.mapper.RecebimentoMapper;
import br.com.invplus.modules.financeiro.repository.ParcelaReceberRepository;
import br.com.invplus.modules.financeiro.repository.RecebimentoRepository;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.modules.financeiro.specification.RecebimentoSpecification;
import br.com.invplus.modules.financeiro.validator.RecebimentoValidator;
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
 * Registra eventos de recebimento e delega a baixa financeira para ParcelaReceberService.
 */
@Service
public class RecebimentoService extends BaseCrudService<Recebimento, RecebimentoRequest, RecebimentoResponse> {

    private final RecebimentoRepository repository;
    private final ParcelaReceberRepository parcelaReceberRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;
    private final ParcelaReceberService parcelaReceberService;
    private final MovimentoFinanceiroService movimentoFinanceiroService;
    private final RecebimentoMapper mapper;
    private final RecebimentoValidator validator;
    private final ContaBancariaRepository contaBancariaRepository;
    private final EmpresaContext empresaContext;

    public RecebimentoService(
            RecebimentoRepository repository,
            ParcelaReceberRepository parcelaReceberRepository,
            FormaPagamentoRepository formaPagamentoRepository,
            ParcelaReceberService parcelaReceberService,
            MovimentoFinanceiroService movimentoFinanceiroService,
            RecebimentoMapper mapper,
            RecebimentoValidator validator,
            ContaBancariaRepository contaBancariaRepository,
            EmpresaContext empresaContext
    ) {
        super(repository, mapper, "Recebimento");
        this.repository = repository;
        this.parcelaReceberRepository = parcelaReceberRepository;
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.parcelaReceberService = parcelaReceberService;
        this.movimentoFinanceiroService = movimentoFinanceiroService;
        this.mapper = mapper;
        this.validator = validator;
        this.contaBancariaRepository = contaBancariaRepository;
        this.empresaContext = empresaContext;
    }

    @Transactional(readOnly = true)
    public Page<RecebimentoResponse> listarComFiltros(UUID parcelaReceberId, UUID contaReceberId,
            UUID formaPagamentoId, UUID empresaId, UUID filialId, UUID clienteId,
            LocalDate dataRecebimentoDe, LocalDate dataRecebimentoAte, Boolean ativo, Pageable pageable) {
        empresaId = resolverEmpresa(empresaId);
        Specification<Recebimento> specification = RecebimentoSpecification.naoDeletado();
        specification = and(specification, RecebimentoSpecification.ativo(ativo));
        specification = and(specification, RecebimentoSpecification.parcelaIgual(parcelaReceberId));
        specification = and(specification, RecebimentoSpecification.contaIgual(contaReceberId));
        specification = and(specification, RecebimentoSpecification.formaPagamentoIgual(formaPagamentoId));
        specification = and(specification, RecebimentoSpecification.empresaIgual(empresaId));
        specification = and(specification, RecebimentoSpecification.filialIgual(filialId));
        specification = and(specification, RecebimentoSpecification.clienteIgual(clienteId));
        specification = and(specification, RecebimentoSpecification.recebimentoDe(dataRecebimentoDe));
        specification = and(specification, RecebimentoSpecification.recebimentoAte(dataRecebimentoAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public RecebimentoResponse criar(RecebimentoRequest request) {
        ParcelaReceber parcela = buscarParcela(request.parcelaReceberId());
        FormaPagamento formaPagamento = buscarFormaPagamento(request.formaPagamentoId());
        ContaBancaria contaBancaria = buscarContaBancaria(request.contaBancariaId(), parcela.getContaReceber().getEmpresa().getId());
        validator.validarCriacao(parcela, formaPagamento, request);

        Recebimento recebimento = new Recebimento();
        recebimento.registrar(parcela, formaPagamento, request.valorRecebido(), request.juros(), request.multa(),
                request.desconto(), request.dataRecebimento(), request.observacao());
        Recebimento salvo = repository.save(recebimento);

        parcelaReceberService.baixar(parcela.getId(), new BaixaParcelaReceberRequest(
                request.valorRecebido(),
                request.juros(),
                request.multa(),
                request.desconto(),
                request.dataRecebimento(),
                request.observacao()
        ));
        movimentoFinanceiroService.registrarRecebimento(salvo, contaBancaria);
        return mapper.toResponse(salvo);
    }

    @Transactional
    public RecebimentoResponse cancelar(UUID id) {
        Recebimento recebimento = buscarEntidade(id);
        validator.validarCancelamento(recebimento);
        parcelaReceberService.estornarRecebimento(
                recebimento.getParcelaReceber().getId(),
                recebimento.getValorRecebido(),
                recebimento.getJuros(),
                recebimento.getMulta(),
                recebimento.getDesconto(),
                "Estorno do recebimento " + recebimento.getId()
        );
        recebimento.cancelar();
        movimentoFinanceiroService.cancelarPorOrigem(
                OrigemMovimentoFinanceiro.RECEBIMENTO,
                recebimento.getId()
        );
        return mapper.toResponse(recebimento);
    }

    private ParcelaReceber buscarParcela(UUID id) {
        UUID empresaId = empresaAtualOuNull();
        if (empresaId != null) return parcelaReceberRepository
                .findByIdAndContaReceberEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela a receber nao encontrada"));
        return parcelaReceberRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela a receber nao encontrada"));
    }

    private FormaPagamento buscarFormaPagamento(UUID id) {
        return formaPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forma de pagamento nao encontrada"));
    }

    private ContaBancaria buscarContaBancaria(UUID id, UUID empresaId) {
        UUID contexto = empresaAtualOuNull();
        if (contexto != null && !contexto.equals(empresaId)) {
            throw new ResourceNotFoundException("Parcela a receber nao encontrada");
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
                    "Conta bancaria deve pertencer a mesma empresa do recebimento");
        }
        return conta;
    }

    @Override
    public Recebimento buscarEntidade(UUID id) {
        UUID empresaId = empresaAtualOuNull();
        if (empresaId == null) return super.buscarEntidade(id);
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Recebimento nao encontrado"));
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

    private Specification<Recebimento> and(Specification<Recebimento> base, Specification<Recebimento> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
