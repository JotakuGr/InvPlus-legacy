package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.financeiro.dto.request.GerarParcelasPagarRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarBaixaRequest;
import br.com.invplus.modules.financeiro.dto.request.ParcelaPagarRequest;
import br.com.invplus.modules.financeiro.dto.response.ParcelaPagarResponse;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.enums.StatusParcelaPagar;
import br.com.invplus.modules.financeiro.mapper.ParcelaPagarMapper;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaPagarRepository;
import br.com.invplus.modules.financeiro.specification.ParcelaPagarSpecification;
import br.com.invplus.modules.financeiro.validator.ParcelaPagarValidator;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra criacao, geracao, baixa e cancelamento das parcelas a pagar.
 */
@Service
public class ParcelaPagarService extends BaseCrudService<ParcelaPagar, ParcelaPagarRequest, ParcelaPagarResponse> {

    private final ParcelaPagarRepository repository;
    private final ContaPagarRepository contaPagarRepository;
    private final CondicaoPagamentoRepository condicaoPagamentoRepository;
    private final GeradorParcelasService geradorParcelasService;
    private final ParcelaPagarMapper mapper;
    private final ParcelaPagarValidator validator;

    public ParcelaPagarService(
            ParcelaPagarRepository repository,
            ContaPagarRepository contaPagarRepository,
            CondicaoPagamentoRepository condicaoPagamentoRepository,
            GeradorParcelasService geradorParcelasService,
            ParcelaPagarMapper mapper,
            ParcelaPagarValidator validator
    ) {
        super(repository, mapper, "Parcela a pagar");
        this.repository = repository;
        this.contaPagarRepository = contaPagarRepository;
        this.condicaoPagamentoRepository = condicaoPagamentoRepository;
        this.geradorParcelasService = geradorParcelasService;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public Page<ParcelaPagarResponse> listarComFiltros(UUID contaPagarId, StatusParcelaPagar status,
            LocalDate dataVencimentoDe, LocalDate dataVencimentoAte, LocalDate dataPagamentoDe,
            LocalDate dataPagamentoAte, Boolean ativo, Pageable pageable) {
        Specification<ParcelaPagar> specification = ParcelaPagarSpecification.naoDeletado();
        specification = and(specification, ParcelaPagarSpecification.ativo(ativo));
        specification = and(specification, ParcelaPagarSpecification.contaIgual(contaPagarId));
        specification = and(specification, ParcelaPagarSpecification.statusIgual(status));
        specification = and(specification, ParcelaPagarSpecification.vencimentoDe(dataVencimentoDe));
        specification = and(specification, ParcelaPagarSpecification.vencimentoAte(dataVencimentoAte));
        specification = and(specification, ParcelaPagarSpecification.pagamentoDe(dataPagamentoDe));
        specification = and(specification, ParcelaPagarSpecification.pagamentoAte(dataPagamentoAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ParcelaPagarResponse criar(ParcelaPagarRequest request) {
        ContaPagar conta = buscarConta(request.contaPagarId());
        validator.validarCriacao(conta, request);
        ParcelaPagar parcela = novaParcela(conta, request.numeroParcela(), request.dataVencimento(),
                request.valor(), request.observacao());
        return mapper.toResponse(repository.save(parcela));
    }

    @Override
    @Transactional
    public ParcelaPagarResponse atualizar(UUID id, ParcelaPagarRequest request) {
        ParcelaPagar parcela = buscarEntidade(id);
        ContaPagar conta = buscarConta(request.contaPagarId());
        validator.validarAtualizacao(id, parcela, conta, request);
        parcela.atualizar(conta, request.numeroParcela(), request.dataVencimento(), request.valor(),
                request.observacao());
        return mapper.toResponse(parcela);
    }

    /**
     * Gera todas as parcelas definidas pela condicao de pagamento.
     */
    @Transactional
    public List<ParcelaPagarResponse> gerarParcelas(UUID contaPagarId, GerarParcelasPagarRequest request) {
        ContaPagar conta = buscarConta(contaPagarId);
        validator.validarContaEditavel(conta);
        if (repository.existsByContaPagarIdAndAtivoTrueAndDeletedAtIsNull(contaPagarId)) {
            throw new InvalidOperationException("Conta a pagar ja possui parcelas ativas");
        }

        CondicaoPagamento condicao = condicaoPagamentoRepository
                .findByIdAndAtivoTrueAndDeletedAtIsNull(request.condicaoPagamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Condicao de pagamento nao encontrada"));
        List<ParcelaGerada> geradas = ajustarTotal(
                geradorParcelasService.gerar(request.dataBase(), conta.getValorTotal(), condicao),
                conta.getValorTotal());

        List<ParcelaPagar> parcelas = new ArrayList<>();
        for (ParcelaGerada gerada : geradas) {
            parcelas.add(novaParcela(conta, gerada.numeroParcela(), gerada.vencimento(), gerada.valor(), null));
        }
        return repository.saveAll(parcelas).stream().map(mapper::toResponse).toList();
    }

    /**
     * Registra baixa e consolida os totais da conta na mesma transacao.
     */
    @Transactional
    public ParcelaPagarResponse baixar(UUID id, ParcelaPagarBaixaRequest request) {
        ParcelaPagar parcela = buscarEntidade(id);
        validator.validarBaixa(parcela, request);
        parcela.baixar(request.valorPago(), request.juros(), request.multa(), request.desconto(),
                request.dataPagamento(), request.observacao());
        atualizarConta(parcela.getContaPagar());
        return mapper.toResponse(parcela);
    }

    /**
     * Cancela uma parcela e recalcula a situacao consolidada da conta.
     */
    @Transactional
    public ParcelaPagarResponse cancelar(UUID id) {
        ParcelaPagar parcela = buscarEntidade(id);
        validator.validarCancelamento(parcela);
        parcela.cancelar();
        atualizarConta(parcela.getContaPagar());
        return mapper.toResponse(parcela);
    }

    /**
     * Estorna um pagamento e reconsolida a conta a pagar.
     */
    @Transactional
    public ParcelaPagarResponse estornarPagamento(UUID id, BigDecimal valorPago, BigDecimal juros,
            BigDecimal multa, BigDecimal desconto, String observacao) {
        ParcelaPagar parcela = buscarEntidade(id);
        parcela.estornar(valorPago, juros, multa, desconto, observacao);
        atualizarConta(parcela.getContaPagar());
        return mapper.toResponse(parcela);
    }

    private ParcelaPagar novaParcela(ContaPagar conta, Integer numero, LocalDate vencimento,
            BigDecimal valor, String observacao) {
        ParcelaPagar parcela = new ParcelaPagar();
        parcela.abrir(conta, numero, vencimento, valor, observacao);
        return parcela;
    }

    private ContaPagar buscarConta(UUID id) {
        return contaPagarRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a pagar nao encontrada"));
    }

    private List<ParcelaGerada> ajustarTotal(List<ParcelaGerada> parcelas, BigDecimal valorTotal) {
        BigDecimal soma = parcelas.stream().map(ParcelaGerada::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal diferenca = valorTotal.subtract(soma);
        if (diferenca.signum() == 0 || parcelas.isEmpty()) {
            return parcelas;
        }
        List<ParcelaGerada> ajustadas = new ArrayList<>(parcelas);
        ParcelaGerada ultima = ajustadas.get(ajustadas.size() - 1);
        ajustadas.set(ajustadas.size() - 1,
                new ParcelaGerada(ultima.numeroParcela(), ultima.vencimento(), ultima.valor().add(diferenca)));
        BigDecimal somaAjustada = ajustadas.stream().map(ParcelaGerada::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (somaAjustada.compareTo(valorTotal) != 0) {
            throw new InvalidOperationException("Soma das parcelas nao fecha o valor total da conta");
        }
        return ajustadas;
    }

    private void atualizarConta(ContaPagar conta) {
        List<ParcelaPagar> parcelas = repository.findByContaPagarIdAndAtivoTrueAndDeletedAtIsNull(conta.getId());
        BigDecimal valorPago = parcelas.stream()
                .filter(parcela -> parcela.getStatus() != StatusParcelaPagar.CANCELADA)
                .map(ParcelaPagar::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorAberto = conta.getValorTotal().subtract(valorPago);
        conta.atualizarPagamento(valorPago, valorAberto, definirStatusConta(parcelas, valorAberto));
    }

    private StatusContaPagar definirStatusConta(List<ParcelaPagar> parcelas, BigDecimal valorAberto) {
        if (valorAberto.signum() == 0 && !parcelas.isEmpty()) {
            return StatusContaPagar.PAGA;
        }
        boolean algumaBaixa = parcelas.stream()
                .anyMatch(parcela -> parcela.getStatus() == StatusParcelaPagar.PARCIALMENTE_PAGA
                        || parcela.getStatus() == StatusParcelaPagar.PAGA);
        return algumaBaixa ? StatusContaPagar.PARCIALMENTE_PAGA : StatusContaPagar.ABERTA;
    }

    private Specification<ParcelaPagar> and(
            Specification<ParcelaPagar> base,
            Specification<ParcelaPagar> filtro
    ) {
        return filtro == null ? base : base.and(filtro);
    }
}
