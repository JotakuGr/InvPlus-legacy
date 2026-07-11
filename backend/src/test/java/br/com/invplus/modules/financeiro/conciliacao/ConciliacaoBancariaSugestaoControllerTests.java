package br.com.invplus.modules.financeiro.conciliacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.financeiro.conciliacao.dto.ResumoSugestoesConciliacaoResponse;
import br.com.invplus.modules.financeiro.conciliacao.service.ConciliacaoBancariaSugestaoService;
import br.com.invplus.modules.financeiro.controller.ConciliacaoBancariaController;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.service.ConciliacaoBancariaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConciliacaoBancariaSugestaoControllerTests {

    @Mock private ConciliacaoBancariaService conciliacaoService;
    @Mock private ConciliacaoBancariaSugestaoService sugestaoService;

    @Test
    void deveDelegarConsultaEDevolverResumo() {
        UUID id = UUID.randomUUID();
        var resumo = new ResumoSugestoesConciliacaoResponse(id, "Credito", LocalDate.now(),
                TipoLancamentoBancario.CREDITO, new BigDecimal("10.00"), 0, List.of());
        when(sugestaoService.buscarSugestoes(id, 10)).thenReturn(resumo);
        var response = new ConciliacaoBancariaController(conciliacaoService, sugestaoService)
                .buscarSugestoes(id, 10);
        assertThat(response.getBody()).isEqualTo(resumo);
    }
}
