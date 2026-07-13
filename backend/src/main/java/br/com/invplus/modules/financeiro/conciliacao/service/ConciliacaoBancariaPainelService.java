package br.com.invplus.modules.financeiro.conciliacao.service;

import br.com.invplus.modules.financeiro.conciliacao.dto.*;
import br.com.invplus.modules.financeiro.conciliacao.projection.ExtratoPendenteProjection;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.enums.*;
import br.com.invplus.modules.financeiro.repository.*;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.invplus.security.context.EmpresaContext;

/** Compoe a visao operacional da conciliacao sem persistir estado derivado. */
@Service
public class ConciliacaoBancariaPainelService {

    private final ExtratoBancarioRepository extratoRepository;
    private final ExtratoBancarioLancamentoRepository lancamentoRepository;
    private final MovimentoFinanceiroRepository movimentoRepository;
    private final ContaBancariaRepository contaRepository;
    private final PontuacaoConciliacaoService pontuacaoService;
    private final EmpresaContext empresaContext;

    public ConciliacaoBancariaPainelService(ExtratoBancarioRepository extratoRepository,
            ExtratoBancarioLancamentoRepository lancamentoRepository,
            MovimentoFinanceiroRepository movimentoRepository, ContaBancariaRepository contaRepository,
            PontuacaoConciliacaoService pontuacaoService, EmpresaContext empresaContext) {
        this.extratoRepository = extratoRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.movimentoRepository = movimentoRepository;
        this.contaRepository = contaRepository;
        this.pontuacaoService = pontuacaoService;
        this.empresaContext = empresaContext;
    }

    @Transactional(readOnly = true)
    public PainelConciliacaoBancariaResponse buscarPainel(UUID contaId,
            LocalDate inicio, LocalDate fim) {
        UUID empresaId = empresaContext.getEmpresaId();
        validarPeriodo(inicio, fim);
        validarConta(empresaId, contaId);
        var extratos = extratoRepository.indicadoresPainel(empresaId, contaId, inicio, fim);
        var lancamentos = lancamentoRepository.indicadoresPainel(empresaId, contaId, inicio, fim);
        long total = lancamentos == null ? 0 : lancamentos.getQuantidadeLancamentos();
        long conciliados = lancamentos == null ? 0 : lancamentos.getQuantidadeLancamentosConciliados();
        var indicadores = new IndicadoresConciliacaoResponse(
                extratos == null ? 0 : extratos.getQuantidadeExtratos(),
                extratos == null ? 0 : extratos.getQuantidadeExtratosImportados(),
                extratos == null ? 0 : extratos.getQuantidadeExtratosProcessando(),
                extratos == null ? 0 : extratos.getQuantidadeExtratosConciliados(),
                total, lancamentos == null ? 0 : lancamentos.getQuantidadeLancamentosPendentes(), conciliados,
                valor(lancamentos == null ? null : lancamentos.getValorTotalCreditosPendentes()),
                valor(lancamentos == null ? null : lancamentos.getValorTotalDebitosPendentes()),
                calcularPercentual(conciliados, total));
        Pageable recentes = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "importadoEm"));
        List<ExtratoPendenteResponse> resumos = extratoRepository
                .buscarResumoExtratos(empresaId, contaId, null, inicio, fim, false, recentes)
                .stream().map(this::mapearExtrato).toList();
        return new PainelConciliacaoBancariaResponse(indicadores, resumos);
    }

    @Transactional(readOnly = true)
    public Page<ExtratoPendenteResponse> buscarExtratosPendentes(UUID contaId,
            StatusExtratoBancario status, LocalDate inicio, LocalDate fim, Pageable pageable) {
        UUID empresaId = empresaContext.getEmpresaId();
        validarPeriodo(inicio, fim);
        validarConta(empresaId, contaId);
        boolean explicito = status != null;
        return extratoRepository.buscarResumoExtratos(empresaId, contaId, status, inicio, fim,
                explicito, limitar(pageable)).map(this::mapearExtrato);
    }
    @Transactional(readOnly = true)
    public Page<LancamentoPendenteResponse> buscarPendencias(UUID extratoId,
            UUID contaId, TipoLancamentoBancario tipo, CategoriaLancamentoBancario categoria,
            LocalDate inicio, LocalDate fim, Boolean possuiSugestao,
            NivelConfiancaConciliacao nivel, Pageable pageable) {
        UUID empresaId = empresaContext.getEmpresaId();
        validarPeriodo(inicio, fim);
        validarConta(empresaId, contaId);
        validarExtrato(empresaId, extratoId);
        Page<ExtratoBancarioLancamento> pagina = lancamentoRepository.buscarPendencias(
                empresaId, extratoId, contaId, tipo, categoria, inicio, fim, limitar(pageable));
        Map<ChaveCandidatos, List<br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro>> cache = new HashMap<>();
        List<LancamentoPendenteResponse> respostas = pagina.getContent().stream()
                .map(item -> mapearLancamento(item, cache))
                .filter(item -> possuiSugestao == null
                        || possuiSugestao.equals(item.quantidadeSugestoes() > 0))
                .filter(item -> nivel == null || item.melhorSugestao() != null
                        && item.melhorSugestao().nivelConfianca() == nivel)
                .toList();
        long total = possuiSugestao != null || nivel != null ? respostas.size() : pagina.getTotalElements();
        return new PageImpl<>(respostas, pagina.getPageable(), total);
    }
    private LancamentoPendenteResponse mapearLancamento(ExtratoBancarioLancamento l,
            Map<ChaveCandidatos, List<br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro>> cache) {
        var conta = l.getExtratoBancario().getContaBancaria();
        TipoMovimentoFinanceiro tipo = l.getTipo() == TipoLancamentoBancario.CREDITO
                ? TipoMovimentoFinanceiro.ENTRADA : TipoMovimentoFinanceiro.SAIDA;
        ChaveCandidatos chave = new ChaveCandidatos(conta.getId(), conta.getEmpresa().getId(), tipo,
                l.getValor(), l.getDataLancamento().minusDays(3), l.getDataLancamento().plusDays(3));
        var movimentos = cache.computeIfAbsent(chave, key -> movimentoRepository.buscarCandidatosConciliacao(
                key.contaId(), key.empresaId(), key.tipo(), key.valor(), key.inicio(), key.fim()));
        var sugestoes = movimentos.stream().map(m -> pontuacaoService.calcular(l, m))
                .filter(s -> s.nivelConfianca() != NivelConfiancaConciliacao.BAIXA)
                .sorted(Comparator.comparingInt(SugestaoConciliacaoResponse::pontuacao).reversed()
                        .thenComparingLong(s -> s.criterios().diferencaDias())
                        .thenComparing(SugestaoConciliacaoResponse::movimentoFinanceiroId)).toList();
        MelhorSugestaoResponse melhor = sugestoes.isEmpty() ? null : melhor(sugestoes.getFirst());
        return new LancamentoPendenteResponse(l.getId(), l.getExtratoBancario().getId(), conta.getId(),
                l.getExtratoBancario().descricaoContaBancaria(), l.getDataLancamento(), l.getDescricao(),
                l.getDocumento(), l.getTipo(), l.getCategoriaBancaria(), l.getValor(),
                l.getSaldoAposLancamento(), melhor, sugestoes.size());
    }

    private MelhorSugestaoResponse melhor(SugestaoConciliacaoResponse s) {
        return new MelhorSugestaoResponse(s.movimentoFinanceiroId(), s.movimentoDescricao(),
                s.dataMovimento(), s.valorMovimento(), s.pontuacao(), s.nivelConfianca(), s.criterios());
    }

    private ExtratoPendenteResponse mapearExtrato(ExtratoPendenteProjection p) {
        long total = p.getQuantidadeLancamentos();
        long conciliados = p.getQuantidadeConciliados();
        return new ExtratoPendenteResponse(p.getExtratoBancarioId(), p.getContaBancariaId(),
                p.getBancoNome() + " - Ag. " + p.getAgencia() + " - Conta " + p.getNumeroConta(),
                p.getDataInicio(), p.getDataFim(), p.getStatus(), (int) total, (int) conciliados,
                (int) (total - conciliados), calcularPercentual(conciliados, total),
                valor(p.getValorCreditosPendentes()), valor(p.getValorDebitosPendentes()), p.getImportadoEm());
    }

    private BigDecimal calcularPercentual(long conciliados, long total) {
        if (total == 0) return BigDecimal.ZERO.setScale(2);
        return BigDecimal.valueOf(conciliados).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim))
            throw new InvalidOperationException("Data inicial nao pode ser posterior a data final");
    }

    private void validarConta(UUID empresaId, UUID contaId) {
        if (contaId == null) return;
        contaRepository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(contaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancaria nao encontrada"));
    }

    private void validarExtrato(UUID empresaId, UUID extratoId) {
        if (extratoId == null) return;
        extratoRepository.findByIdAndContaBancariaEmpresaIdAndAtivoTrueAndDeletedAtIsNull(extratoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Extrato bancario nao encontrado"));
    }

    private Pageable limitar(Pageable pageable) {
        int tamanho = Math.min(Math.max(pageable.getPageSize(), 1), 50);
        return PageRequest.of(pageable.getPageNumber(), tamanho, pageable.getSort());
    }

    private BigDecimal valor(BigDecimal valor) { return valor == null ? BigDecimal.ZERO : valor; }

    private record ChaveCandidatos(UUID contaId, UUID empresaId, TipoMovimentoFinanceiro tipo,
            BigDecimal valor, LocalDate inicio, LocalDate fim) {}
}
