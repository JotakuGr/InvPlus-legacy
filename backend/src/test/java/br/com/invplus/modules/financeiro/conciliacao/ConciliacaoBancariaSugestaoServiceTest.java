package br.com.invplus.modules.financeiro.conciliacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.conciliacao.dto.CriteriosSugestaoResponse;
import br.com.invplus.modules.financeiro.conciliacao.dto.SugestaoConciliacaoResponse;
import br.com.invplus.modules.financeiro.conciliacao.service.ConciliacaoBancariaSugestaoService;
import br.com.invplus.modules.financeiro.conciliacao.service.PontuacaoConciliacaoService;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.NivelConfiancaConciliacao;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.shared.domain.exception.ConflictException;

@ExtendWith(MockitoExtension.class)
class ConciliacaoBancariaSugestaoServiceTest {

    @Mock private ExtratoBancarioLancamentoRepository lancamentoRepository;
    @Mock private MovimentoFinanceiroRepository movimentoRepository;
    @Mock private PontuacaoConciliacaoService pontuacaoService;
    private ConciliacaoBancariaSugestaoService service;
    private ExtratoBancarioLancamento lancamento;

    @BeforeEach
    @SuppressWarnings("unused")
    void setup() {
        service = new ConciliacaoBancariaSugestaoService(
                lancamentoRepository, movimentoRepository, pontuacaoService);
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        ContaBancaria conta = new ContaBancaria();
        conta.setId(UUID.randomUUID());
        conta.setEmpresa(empresa);
        ExtratoBancario extrato = new ExtratoBancario();
        extrato.setContaBancaria(conta);
        extrato.setStatus(StatusExtratoBancario.IMPORTADO);
        lancamento = new ExtratoBancarioLancamento();
        lancamento.setId(UUID.randomUUID());
        lancamento.setExtratoBancario(extrato);
        lancamento.setDataLancamento(LocalDate.of(2026, 7, 10));
        lancamento.setTipo(TipoLancamentoBancario.CREDITO);
        lancamento.setValor(new BigDecimal("500.00"));
        lancamento.setDescricao("Mercado Central");
        lancamento.setConciliado(false);
    }

    @Test
    void deveOrdenarCandidatosPorPontuacaoERespeitarLimite() {
        MovimentoFinanceiro a = movimento();
        MovimentoFinanceiro b = movimento();
        when(lancamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(lancamento.getId()))
                .thenReturn(Optional.of(lancamento));
        when(movimentoRepository.buscarCandidatosConciliacao(
                lancamento.getExtratoBancario().getContaBancaria().getId(),
                lancamento.getExtratoBancario().getContaBancaria().getEmpresa().getId(),
                TipoMovimentoFinanceiro.ENTRADA, lancamento.getValor(),
                LocalDate.of(2026, 7, 7), LocalDate.of(2026, 7, 13)))
                .thenReturn(List.of(a, b));
        when(pontuacaoService.calcular(lancamento, a)).thenReturn(resposta(a, 82));
        when(pontuacaoService.calcular(lancamento, b)).thenReturn(resposta(b, 95));

        var resultado = service.buscarSugestoes(lancamento.getId(), 1);
        assertThat(resultado.sugestoes()).extracting(sugestao -> sugestao.pontuacao())
                .containsExactly(95);
    }

    @Test
    void deveBloquearLancamentoConciliado() {
        lancamento.setConciliado(true);
        when(lancamentoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(lancamento.getId()))
                .thenReturn(Optional.of(lancamento));
        assertThatThrownBy(() -> service.buscarSugestoes(lancamento.getId(), 10))
                .isInstanceOf(ConflictException.class);
    }

    private MovimentoFinanceiro movimento() {
        MovimentoFinanceiro movimento = new MovimentoFinanceiro();
        movimento.setId(UUID.randomUUID());
        return movimento;
    }

    private SugestaoConciliacaoResponse resposta(MovimentoFinanceiro movimento, int pontos) {
        return new SugestaoConciliacaoResponse(movimento.getId(), "Movimento", LocalDate.of(2026, 7, 10),
                TipoMovimentoFinanceiro.ENTRADA, new BigDecimal("500.00"), UUID.randomUUID(), "Conta",
                pontos, pontos >= 90 ? NivelConfiancaConciliacao.ALTA : NivelConfiancaConciliacao.MEDIA,
                new CriteriosSugestaoResponse(true, true, true, 0, false, BigDecimal.ONE));
    }
}
