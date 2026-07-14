package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.financeiro.dto.request.BaixaParcelaReceberRequest;
import br.com.invplus.modules.financeiro.dto.request.GerarParcelasReceberRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaReceberRequest;
import br.com.invplus.modules.financeiro.dto.response.ParcelaReceberResponse;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.enums.StatusParcelaReceber;
import br.com.invplus.modules.financeiro.mapper.ParcelaReceberMapper;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaReceberRepository;
import br.com.invplus.modules.financeiro.specification.ParcelaReceberSpecification;
import br.com.invplus.modules.financeiro.validator.ParcelaReceberValidator;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra criacao, geracao, baixa e cancelamento das parcelas a receber.
 */
@Service
public class ParcelaReceberService extends BaseCrudService<ParcelaReceber, ParcelaReceberRequest, ParcelaReceberResponse> {

    private final ParcelaReceberRepository repository;
    private final ContaReceberRepository contaReceberRepository;
    private final CondicaoPagamentoRepository condicaoPagamentoRepository;
    private final GeradorParcelasService geradorParcelasService;
    private final ParcelaReceberMapper mapper;
    private final ParcelaReceberValidator validator;

    public ParcelaReceberService(
            ParcelaReceberRepository repository,
            ContaReceberRepository contaReceberRepository,
            CondicaoPagamentoRepository condicaoPagamentoRepository,
            GeradorParcelasService geradorParcelasService,
            ParcelaReceberMapper mapper,
            ParcelaReceberValidator validator
    ) {
        super(repository, mapper, "Parcela a receber");
        this.repository = repository;
        this.contaReceberRepository = contaReceberRepository;
        this.condicaoPagamentoRepository = condicaoPagamentoRepository;
        this.geradorParcelasService = geradorParcelasService;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public Page<ParcelaReceberResponse> listarComFiltros(UUID contaReceberId, StatusParcelaReceber status,
            LocalDate dataVencimentoDe, LocalDate dataVencimentoAte, LocalDate dataPagamentoDe,
            LocalDate dataPagamentoAte, Boolean ativo, Pageable pageable) {
        Specification<ParcelaReceber> specification = ParcelaReceberSpecification.naoDeletado();
        specification = and(specification, ParcelaReceberSpecification.ativo(ativo));
        specification = and(specification, ParcelaReceberSpecification.contaIgual(contaReceberId));
        specification = and(specification, ParcelaReceberSpecification.statusIgual(status));
        specification = and(specification, ParcelaReceberSpecification.vencimentoDe(dataVencimentoDe));
        specification = and(specification, ParcelaReceberSpecification.vencimentoAte(dataVencimentoAte));
        specification = and(specification, ParcelaReceberSpecification.pagamentoDe(dataPagamentoDe));
        specification = and(specification, ParcelaReceberSpecification.pagamentoAte(dataPagamentoAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ParcelaReceberResponse criar(ParcelaReceberRequest request) {
        ContaReceber conta = buscarConta(request.contaReceberId());
        validator.validarCriacao(conta, request);
        ParcelaReceber parcela = novaParcela(conta, request.numeroParcela(), request.dataVencimento(),
                request.valor(), request.observacao());
        return mapper.toResponse(repository.save(parcela));
    }

    @Override
    @Transactional
    public ParcelaReceberResponse atualizar(UUID id, ParcelaReceberRequest request) {
        ParcelaReceber parcela = buscarEntidade(id);
        ContaReceber conta = buscarConta(request.contaReceberId());
        validator.validarAtualizacao(id, parcela, conta, request);
        parcela.atualizar(conta, request.numeroParcela(), request.dataVencimento(), request.valor(), request.observacao());
        return mapper.toResponse(parcela);
    }

    @Transactional
    public List<ParcelaReceberResponse> gerarParcelas(UUID contaReceberId, GerarParcelasReceberRequest request) {
        ContaReceber conta = buscarConta(contaReceberId);
        validator.validarContaEditavel(conta);
        if (repository.existsByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(contaReceberId)) {
            throw new InvalidOperationException("Conta a receber ja possui parcelas ativas");
        }

        CondicaoPagamento condicao = condicaoPagamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(request.condicaoPagamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Condicao de pagamento nao encontrada"));
        List<ParcelaGerada> geradas = ajustarTotal(geradorParcelasService.gerar(request.dataBase(), conta.getValorTotal(), condicao),
                conta.getValorTotal());

        List<ParcelaReceber> parcelas = new ArrayList<>();
        for (ParcelaGerada gerada : geradas) {
            ParcelaReceber parcela = novaParcela(conta, gerada.numeroParcela(), gerada.vencimento(), gerada.valor(), null);
            parcelas.add(parcela);
        }
        return repository.saveAll(parcelas).stream().map(mapper::toResponse).toList();
    }

    /**
     * Gera parcelas para fluxos internos, reutilizando a mesma regra exposta pela API.
     */
    @Transactional
    public List<ParcelaReceberResponse> gerarPorContaReceber(UUID contaReceberId, UUID condicaoPagamentoId, LocalDate dataBase) {
        return gerarParcelas(contaReceberId, new GerarParcelasReceberRequest(condicaoPagamentoId, dataBase));
    }

    @Transactional
    public ParcelaReceberResponse baixar(UUID id, BaixaParcelaReceberRequest request) {
        ParcelaReceber parcela = buscarEntidade(id);
        validator.validarBaixa(parcela, request);
        parcela.baixar(request.valorPago(), request.juros(), request.multa(), request.desconto(),
                request.dataPagamento(), request.observacao());
        atualizarConta(parcela.getContaReceber());
        return mapper.toResponse(parcela);
    }

    @Transactional
    public ParcelaReceberResponse cancelar(UUID id) {
        ParcelaReceber parcela = buscarEntidade(id);
        validator.validarCancelamento(parcela);
        parcela.cancelar();
        atualizarConta(parcela.getContaReceber());
        return mapper.toResponse(parcela);
    }

    @Transactional
    public ParcelaReceberResponse estornarRecebimento(UUID id, BigDecimal valorPago, BigDecimal juros,
            BigDecimal multa, BigDecimal desconto, String observacao) {
        ParcelaReceber parcela = buscarEntidade(id);
        parcela.estornar(valorPago, juros, multa, desconto, observacao);
        atualizarConta(parcela.getContaReceber());
        return mapper.toResponse(parcela);
    }

    private ParcelaReceber novaParcela(ContaReceber conta, Integer numero, LocalDate vencimento,
            BigDecimal valor, String observacao) {
        ParcelaReceber parcela = new ParcelaReceber();
        parcela.abrir(conta, numero, vencimento, valor, observacao);
        return parcela;
    }

    private ContaReceber buscarConta(UUID id) {
        return contaReceberRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a receber nao encontrada"));
    }

    private List<ParcelaGerada> ajustarTotal(List<ParcelaGerada> parcelas, BigDecimal valorTotal) {
        BigDecimal soma = parcelas.stream()
                .map(parcela -> Objects.requireNonNull(parcela.valor(), "Valor da parcela nao pode ser nulo"))
                .reduce(BigDecimal.ZERO, (total, valor) -> total.add(valor));
        BigDecimal diferenca = valorTotal.subtract(soma);
        if (diferenca.signum() == 0 || parcelas.isEmpty()) {
            return parcelas;
        }
        List<ParcelaGerada> ajustadas = new ArrayList<>(parcelas);
        ParcelaGerada ultima = ajustadas.get(ajustadas.size() - 1);
        ajustadas.set(ajustadas.size() - 1,
                new ParcelaGerada(ultima.numeroParcela(), ultima.vencimento(), ultima.valor().add(diferenca)));
        BigDecimal somaAjustada = ajustadas.stream()
                .map(parcela -> Objects.requireNonNull(parcela.valor(), "Valor da parcela nao pode ser nulo"))
                .reduce(BigDecimal.ZERO, (total, valor) -> total.add(valor));
        if (somaAjustada.compareTo(valorTotal) != 0) {
            throw new InvalidOperationException("Soma das parcelas nao fecha o valor total da conta");
        }
        return ajustadas;
    }

    private void atualizarConta(ContaReceber conta) {
        List<ParcelaReceber> parcelas = repository.findByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(conta.getId());
        BigDecimal valorPago = parcelas.stream()
                .filter(parcela -> parcela.getStatus() != StatusParcelaReceber.CANCELADA)
                .map(parcela -> Objects.requireNonNull(
                        parcela.getValorPago(), "Valor recebido da parcela nao pode ser nulo"))
                .reduce(BigDecimal.ZERO, (total, valor) -> total.add(valor));
        BigDecimal valorAberto = conta.getValorTotal().subtract(valorPago);
        StatusContaReceber status = definirStatusConta(parcelas, valorAberto);
        conta.atualizarRecebimento(valorPago, valorAberto, status);
    }

    private StatusContaReceber definirStatusConta(List<ParcelaReceber> parcelas, BigDecimal valorAberto) {
        if (valorAberto.signum() == 0 && !parcelas.isEmpty()) {
            return StatusContaReceber.PAGA;
        }
        boolean algumaParcial = parcelas.stream()
                .anyMatch(parcela -> parcela.getStatus() == StatusParcelaReceber.PARCIALMENTE_PAGA
                        || parcela.getStatus() == StatusParcelaReceber.PAGA);
        return algumaParcial ? StatusContaReceber.PARCIALMENTE_PAGA : StatusContaReceber.ABERTA;
    }

    private Specification<ParcelaReceber> and(Specification<ParcelaReceber> base, Specification<ParcelaReceber> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
