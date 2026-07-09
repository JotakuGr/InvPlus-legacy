package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoParcelaRequest;
import br.com.invplus.modules.pagamentos.dto.request.FormaPagamentoRequest;
import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import br.com.invplus.modules.pagamentos.dto.response.CondicaoPagamentoResponse;
import br.com.invplus.modules.pagamentos.dto.response.FormaPagamentoResponse;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.service.CondicaoPagamentoService;
import br.com.invplus.modules.pagamentos.service.FormaPagamentoService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Fluxo integrado inicial do modulo Pagamentos.
 */
@SpringBootTest
@ActiveProfiles("test")
class PagamentosBusinessFlowIntegrationTest {

    @Autowired
    private FormaPagamentoService formaPagamentoService;
    @Autowired
    private CondicaoPagamentoService condicaoPagamentoService;

    @Test
    void deveCriarFormaPagamentoECondicaoPagamentoParaUsoComercial() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        FormaPagamentoResponse forma = formaPagamentoService.criar(new FormaPagamentoRequest(
                "CC-" + sufixo,
                "Cartao credito " + sufixo,
                TipoFormaPagamento.CARTAO_CREDITO,
                false,
                true
        ));

        CondicaoPagamentoResponse condicao = condicaoPagamentoService.criar(new CondicaoPagamentoRequest(
                "10X-" + sufixo,
                "Cartao 10x " + sufixo,
                new BigDecimal("1.5000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                RegraVencimento.PROXIMO_DIA_UTIL,
                true,
                parcelasDezVezes()
        ));

        assertThat(forma.id()).isNotNull();
        assertThat(forma.permiteParcelamento()).isTrue();
        assertThat(condicao.id()).isNotNull();
        assertThat(condicao.parcelas()).hasSize(10);

        assertThat(formaPagamentoService.listarComFiltros("CC-" + sufixo, null, TipoFormaPagamento.CARTAO_CREDITO, true, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
        assertThat(condicaoPagamentoService.listarComFiltros("10X-" + sufixo, null, true, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }

    private List<CondicaoPagamentoParcelaRequest> parcelasDezVezes() {
        return java.util.stream.IntStream.rangeClosed(1, 10)
                .mapToObj(numero -> new CondicaoPagamentoParcelaRequest(
                        numero,
                        numero * 30,
                        new BigDecimal("10.00")
                ))
                .toList();
    }
}
