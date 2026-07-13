package br.com.invplus.modules.financeiro.conciliacao;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import br.com.invplus.modules.financeiro.conciliacao.projection.IndicadoresExtratoProjection;
import br.com.invplus.modules.financeiro.conciliacao.projection.IndicadoresLancamentoProjection;
import br.com.invplus.modules.financeiro.conciliacao.service.ConciliacaoBancariaPainelService;
import br.com.invplus.modules.financeiro.conciliacao.service.PontuacaoConciliacaoService;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;

@ExtendWith(MockitoExtension.class)
class ConciliacaoBancariaPainelServiceTest {
    @Mock ExtratoBancarioRepository extratoRepository;
    @Mock ExtratoBancarioLancamentoRepository lancamentoRepository;
    @Mock MovimentoFinanceiroRepository movimentoRepository;
    @Mock ContaBancariaRepository contaRepository;
    @Mock PontuacaoConciliacaoService pontuacaoService;
    @Mock IndicadoresExtratoProjection extratos;
    @Mock IndicadoresLancamentoProjection lancamentos;
    ConciliacaoBancariaPainelService service;

    @BeforeEach@SuppressWarnings("unused")
 void setup() {
        service = new ConciliacaoBancariaPainelService(extratoRepository, lancamentoRepository,
                movimentoRepository, contaRepository, pontuacaoService, null);
    }

    @Test void deveCalcularIndicadoresEPercentual() {
        UUID empresaId = UUID.randomUUID();
        when(extratoRepository.indicadoresPainel(eq(empresaId), isNull(), isNull(), isNull()))
                .thenReturn(extratos);
        when(lancamentoRepository.indicadoresPainel(eq(empresaId), isNull(), isNull(), isNull()))
                .thenReturn(lancamentos);
        when(lancamentos.getQuantidadeLancamentos()).thenReturn(120L);
        when(lancamentos.getQuantidadeLancamentosConciliados()).thenReturn(90L);
        when(lancamentos.getQuantidadeLancamentosPendentes()).thenReturn(30L);
        when(lancamentos.getValorTotalCreditosPendentes()).thenReturn(new BigDecimal("500.00"));
        when(lancamentos.getValorTotalDebitosPendentes()).thenReturn(new BigDecimal("200.00"));
        when(extratoRepository.buscarResumoExtratos(eq(empresaId), isNull(), isNull(), isNull(),
                isNull(), eq(false), any())).thenReturn(new PageImpl<>(java.util.List.of()));
        var resposta = service.buscarPainel(empresaId, null, null, null);
        assertThat(resposta.indicadores().percentualConciliado()).isEqualByComparingTo("75.00");
        assertThat(resposta.indicadores().valorTotalCreditosPendentes()).isEqualByComparingTo("500.00");
    }

    @Test void deveRetornarPercentualZeroSemLancamentos() {
        UUID empresaId = UUID.randomUUID();
        when(extratoRepository.indicadoresPainel(any(), any(), any(), any())).thenReturn(extratos);
        when(lancamentoRepository.indicadoresPainel(any(), any(), any(), any())).thenReturn(lancamentos);
        when(extratoRepository.buscarResumoExtratos(any(), any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of()));
        assertThat(service.buscarPainel(empresaId, null, null, null).indicadores().percentualConciliado())
                .isEqualByComparingTo("0.00");
    }
}
