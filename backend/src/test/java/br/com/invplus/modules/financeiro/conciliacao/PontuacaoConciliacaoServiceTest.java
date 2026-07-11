package br.com.invplus.modules.financeiro.conciliacao;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.modules.financeiro.conciliacao.service.PontuacaoConciliacaoService;
import br.com.invplus.modules.financeiro.conciliacao.util.NormalizadorTextoFinanceiro;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.NivelConfiancaConciliacao;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PontuacaoConciliacaoServiceTest {

    private final PontuacaoConciliacaoService service =
            new PontuacaoConciliacaoService(new NormalizadorTextoFinanceiro());

    @Test
    void deveCalcularCorrespondenciaCompletaComConfiancaAlta() {
        UUID origem = UUID.randomUUID();
        var resposta = service.calcular(lancamento(LocalDate.of(2026, 7, 10), origem.toString(),
                "PIX Mercado Central"), movimento(LocalDate.of(2026, 7, 10), origem,
                "Recebimento Mercado Central"));
        assertThat(resposta.pontuacao()).isEqualTo(100);
        assertThat(resposta.nivelConfianca()).isEqualTo(NivelConfiancaConciliacao.ALTA);
        assertThat(resposta.criterios().documentoIgual()).isTrue();
    }

    @Test
    void devePontuarDatasEIgnorarDocumentosVazios() {
        var base = LocalDate.of(2026, 7, 10);
        assertThat(service.calcular(lancamento(base, null, "Mercado Central"),
                movimento(base.plusDays(1), null, "Mercado Central")).pontuacao()).isEqualTo(88);
        assertThat(service.calcular(lancamento(base, null, "Mercado Central"),
                movimento(base.plusDays(2), null, "Mercado Central")).pontuacao()).isEqualTo(85);
        assertThat(service.calcular(lancamento(base, null, "Mercado Central"),
                movimento(base.plusDays(3), null, "Outro favorecido")).pontuacao()).isEqualTo(77);
    }

    private ExtratoBancarioLancamento lancamento(LocalDate data, String documento, String descricao) {
        var entity = new ExtratoBancarioLancamento();
        entity.setDataLancamento(data);
        entity.setDocumento(documento);
        entity.setDescricao(descricao);
        entity.setTipo(TipoLancamentoBancario.CREDITO);
        entity.setValor(new BigDecimal("500.00"));
        return entity;
    }

    private MovimentoFinanceiro movimento(LocalDate data, UUID origem, String descricao) {
        var conta = new ContaBancaria();
        conta.setId(UUID.randomUUID());
        conta.setBancoNome("Banco Teste");
        conta.setAgencia("1234");
        conta.setNumeroConta("9999");
        var entity = new MovimentoFinanceiro();
        entity.setId(UUID.randomUUID());
        entity.setContaBancaria(conta);
        entity.setDataMovimento(data);
        entity.setOrigemId(origem);
        entity.setDescricao(descricao);
        entity.setTipoMovimento(TipoMovimentoFinanceiro.ENTRADA);
        entity.setValor(new BigDecimal("500.00"));
        return entity;
    }
}
