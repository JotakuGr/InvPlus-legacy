package br.com.invplus.modules.financeiro.conciliacao.service;

import br.com.invplus.modules.financeiro.conciliacao.dto.ResumoSugestoesConciliacaoResponse;
import br.com.invplus.modules.financeiro.conciliacao.dto.SugestaoConciliacaoResponse;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.enums.NivelConfiancaConciliacao;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calcula candidatos atuais; nenhuma sugestao e persistida ou reservada. */
@Service
@RequiredArgsConstructor
public class ConciliacaoBancariaSugestaoService {

    private final ExtratoBancarioLancamentoRepository lancamentoRepository;
    private final MovimentoFinanceiroRepository movimentoRepository;
    private final PontuacaoConciliacaoService pontuacaoService;

    @Transactional(readOnly = true)
    public ResumoSugestoesConciliacaoResponse buscarSugestoes(UUID lancamentoId, int limite) {
        ExtratoBancarioLancamento lancamento = lancamentoRepository
                .findByIdAndAtivoTrueAndDeletedAtIsNull(lancamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Lancamento bancario nao encontrado"));
        if (Boolean.TRUE.equals(lancamento.getConciliado())) {
            throw new ConflictException("Lancamento bancario ja esta conciliado");
        }
        if (lancamento.getExtratoBancario().getStatus() == StatusExtratoBancario.CANCELADO) {
            throw new InvalidOperationException("Extrato bancario cancelado nao possui sugestoes");
        }

        var conta = lancamento.getExtratoBancario().getContaBancaria();
        LocalDate inicio = lancamento.getDataLancamento().minusDays(3);
        LocalDate fim = lancamento.getDataLancamento().plusDays(3);
        TipoMovimentoFinanceiro tipo = lancamento.getTipo() == TipoLancamentoBancario.CREDITO
                ? TipoMovimentoFinanceiro.ENTRADA : TipoMovimentoFinanceiro.SAIDA;

        var sugestoes = movimentoRepository.buscarCandidatosConciliacao(
                        conta.getId(), conta.getEmpresa().getId(), tipo, lancamento.getValor(), inicio, fim)
                .stream()
                .map(movimento -> pontuacaoService.calcular(lancamento, movimento))
                .filter(item -> item.nivelConfianca() != NivelConfiancaConciliacao.BAIXA)
                .sorted(Comparator.comparingInt(SugestaoConciliacaoResponse::pontuacao).reversed()
                        .thenComparingLong(item -> item.criterios().diferencaDias())
                        .thenComparing(SugestaoConciliacaoResponse::dataMovimento)
                        .thenComparing(SugestaoConciliacaoResponse::movimentoFinanceiroId))
                .limit(limite)
                .toList();

        return new ResumoSugestoesConciliacaoResponse(
                lancamento.getId(), lancamento.getDescricao(), lancamento.getDataLancamento(),
                lancamento.getTipo(), lancamento.getValor(), sugestoes.size(), sugestoes);
    }
}
