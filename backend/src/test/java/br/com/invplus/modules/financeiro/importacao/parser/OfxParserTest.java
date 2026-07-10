package br.com.invplus.modules.financeiro.importacao.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Testes unitarios da conversao segura de OFX para o modelo intermediario. */
class OfxParserTest {

    private final OfxParser parser = new OfxParser();

    @Test
    void deveLerContaPeriodoSaldoEIdentificador() throws IOException {
        var extrato = parser.processar(recurso("extrato-valido.ofx"));

        assertThat(extrato.bancoCodigo()).isEqualTo("001");
        assertThat(extrato.agencia()).isEqualTo("1234");
        assertThat(extrato.numeroConta()).isEqualTo("99999-9");
        assertThat(extrato.dataInicio()).hasToString("2026-07-01");
        assertThat(extrato.dataFim()).hasToString("2026-07-31");
        assertThat(extrato.saldoFinal()).isEqualByComparingTo("1150.00");
        assertThat(extrato.lancamentos().getFirst().identificadorBancario()).isEqualTo("FIT-VALIDO-1");
    }

    @Test
    void deveLerCreditoDebitoETarifaPreservandoSinalOriginal() throws IOException {
        var extrato = parser.processar(recurso("extrato-creditos-debitos.ofx"));

        assertThat(extrato.lancamentos()).hasSize(3);
        assertThat(extrato.lancamentos().get(0).valor()).isPositive();
        assertThat(extrato.lancamentos().get(1).valor()).isEqualByComparingTo(new BigDecimal("-120.50"));
        assertThat(extrato.lancamentos().get(1).documento()).isEqualTo("DOC-10");
        assertThat(extrato.lancamentos().get(2).tipoOriginal()).isEqualTo("FEE");
    }

    @Test
    void deveProcessarOfxSgmlComTagsFolhaSemFechamento() {
        String sgml = """
                OFXHEADER:100
                ENCODING:UTF-8
                <OFX><BANKMSGSRSV1><STMTTRNRS><STMTRS>
                <CURDEF>BRL<BANKACCTFROM><BANKID>001<BRANCHID>1234<ACCTID>99999-9</BANKACCTFROM>
                <BANKTRANLIST><DTSTART>20260701<DTEND>20260731
                <STMTTRN><TRNTYPE>CREDIT<DTPOSTED>20260710<TRNAMT>25.00<FITID>SGML-1<MEMO>Credito SGML</STMTTRN>
                </BANKTRANLIST><LEDGERBAL><BALAMT>25.00<DTASOF>20260731</LEDGERBAL>
                </STMTRS></STMTTRNRS></BANKMSGSRSV1></OFX>
                """;
        var extrato = parser.processar(sgml.getBytes(StandardCharsets.UTF_8));
        assertThat(extrato.lancamentos()).hasSize(1);
        assertThat(extrato.lancamentos().getFirst().valor()).isEqualByComparingTo("25.00");
    }

    @Test
    void deveBloquearArquivoVazioOuConteudoInvalido() {
        assertThatThrownBy(() -> parser.processar(new byte[0])).isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> parser.processar("nao-ofx".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(InvalidOperationException.class);
    }

    private byte[] recurso(String nome) throws IOException {
        try (var stream = getClass().getResourceAsStream("/ofx/" + nome)) {
            if (stream == null) {
                throw new IOException("Fixture nao encontrada: " + nome);
            }
            return stream.readAllBytes();
        }
    }
}
