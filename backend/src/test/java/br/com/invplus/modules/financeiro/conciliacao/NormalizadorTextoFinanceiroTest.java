package br.com.invplus.modules.financeiro.conciliacao;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.modules.financeiro.conciliacao.util.NormalizadorTextoFinanceiro;
import org.junit.jupiter.api.Test;

class NormalizadorTextoFinanceiroTest {

    private final NormalizadorTextoFinanceiro normalizador = new NormalizadorTextoFinanceiro();

    @Test
    void deveNormalizarAcentosEspacosCaracteresEPalavrasIrrelevantes() {
        assertThat(normalizador.normalizar("  Pix recebido - João   Silva 123! "))
                .isEqualTo("JOAO SILVA 123");
    }

    @Test
    void deveTratarNuloEVazio() {
        assertThat(normalizador.normalizar(null)).isEmpty();
        assertThat(normalizador.normalizar("   ")).isEmpty();
    }
}
