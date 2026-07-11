package br.com.invplus.modules.financeiro.conciliacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import br.com.invplus.modules.financeiro.conciliacao.controller.ConciliacaoBancariaPainelController;
import br.com.invplus.modules.financeiro.conciliacao.dto.*;
import br.com.invplus.modules.financeiro.conciliacao.service.ConciliacaoBancariaPainelService;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class ConciliacaoBancariaPainelControllerTests {
    @Mock ConciliacaoBancariaPainelService service;

    @Test void deveConsultarAsTresVisoes() {
        UUID empresa = UUID.randomUUID();
        var indicadores = new IndicadoresConciliacaoResponse(0,0,0,0,0,0,0,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO.setScale(2));
        var painel = new PainelConciliacaoBancariaResponse(indicadores, List.of());
        when(service.buscarPainel(empresa, null, null, null)).thenReturn(painel);
        when(service.buscarExtratosPendentes(eq(empresa), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(Page.empty());
        when(service.buscarPendencias(eq(empresa), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), any())).thenReturn(Page.empty());
        var controller = new ConciliacaoBancariaPainelController(service);
        assertThat(controller.painel(empresa, null, null, null).getBody()).isEqualTo(painel);
        assertThat(controller.extratos(empresa, null, null, null, null, Pageable.unpaged()).getBody()).isEmpty();
        assertThat(controller.pendencias(empresa, null, null, null, null, null, null,
                null, null, Pageable.unpaged()).getBody()).isEmpty();
    }
}
