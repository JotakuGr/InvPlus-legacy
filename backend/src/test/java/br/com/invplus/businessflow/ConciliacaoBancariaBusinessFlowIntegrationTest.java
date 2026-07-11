package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaCancelamentoRequest;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioLancamentoRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioRepository;
import br.com.invplus.modules.financeiro.service.ConciliacaoBancariaService;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioLancamentoService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioService;
import br.com.invplus.modules.financeiro.service.MovimentoFinanceiroService;
import br.com.invplus.modules.financeiro.conciliacao.service.ConciliacaoBancariaSugestaoService;
import br.com.invplus.shared.domain.exception.ConflictException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Valida o ciclo MovimentoFinanceiro <-> Conciliacao <-> Lancamento bancario. */
@SpringBootTest
@ActiveProfiles("test")
class SugestaoConciliacaoBusinessFlowIntegrationTest {

    @Autowired private ConciliacaoBancariaService conciliacaoService;
    @Autowired private ContaBancariaService contaBancariaService;
    @Autowired private ExtratoBancarioService extratoService;
    @Autowired private ExtratoBancarioLancamentoService lancamentoService;
    @Autowired private MovimentoFinanceiroService movimentoService;
    @Autowired private ConciliacaoBancariaSugestaoService sugestaoService;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private FormaPagamentoRepository formaPagamentoRepository;
    @Autowired private ExtratoBancarioRepository extratoRepository;
    @Autowired private ExtratoBancarioLancamentoRepository lancamentoRepository;

    @Test
    void deveConciliarAtualizarExtratoECancelarPreservandoHistorico() {
        String sufixo = sufixo();
        Empresa empresa = empresaRepository.saveAndFlush(new Empresa("Empresa Fluxo Conc " + sufixo,
                "Empresa Fluxo Conc " + sufixo, documento(sufixo), null, null, null));
        FormaPagamento forma = new FormaPagamento();
        forma.atualizar("CF" + sufixo, "Forma Fluxo Conc " + sufixo,
                TipoFormaPagamento.PIX, false, false);
        forma = formaPagamentoRepository.saveAndFlush(forma);

        var conta = contaBancariaService.criar(new ContaBancariaRequest(empresa.getId(), null,
                "001", "Banco Fluxo", "2" + sufixo, null, "8" + sufixo, null,
                TipoContaBancaria.CORRENTE, "Empresa Fluxo", null, null,
                BigDecimal.ZERO, LocalDate.of(2026, 7, 10), true));
        var extrato = extratoService.importar(new ExtratoBancarioRequest(conta.id(),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), BigDecimal.ZERO,
                new BigDecimal("200.00"), OrigemExtratoBancario.OFX,
                "fluxo.ofx", hash(sufixo), null));
        var lancamento = lancamentoService.criar(new ExtratoBancarioLancamentoRequest(extrato.id(),
                LocalDate.of(2026, 7, 10), "Credito do recebimento", null,
                TipoLancamentoBancario.CREDITO, new BigDecimal("200.00"), null,
                "FLOW-" + sufixo, CategoriaLancamentoBancario.PIX, null));
        var movimento = movimentoService.criarManual(new MovimentoFinanceiroRequest(empresa.getId(), null,
                forma.getId(), null, null, null, null, conta.id(), TipoMovimentoFinanceiro.ENTRADA,
                OrigemMovimentoFinanceiro.AJUSTE_MANUAL, null, new BigDecimal("200.00"),
                LocalDate.of(2026, 7, 10), "Entrada financeira", null));

        var sugestoes = sugestaoService.buscarSugestoes(lancamento.id(), 10);
        assertThat(sugestoes.sugestoes()).extracting("movimentoFinanceiroId").contains(movimento.id());

        var conciliacao = conciliacaoService.criar(new ConciliacaoBancariaRequest(lancamento.id(),
                movimento.id(), TipoConciliacaoBancaria.MANUAL, "Fluxo integrado"));

        assertThat(conciliacao.status()).isEqualTo(StatusConciliacaoBancaria.CONCILIADA);
        assertThat(lancamentoRepository.findById(lancamento.id()).orElseThrow().getConciliado()).isTrue();
        assertThat(extratoRepository.findById(extrato.id()).orElseThrow().getStatus())
                .isEqualTo(StatusExtratoBancario.CONCILIADO);
        assertThatThrownBy(() -> sugestaoService.buscarSugestoes(lancamento.id(), 10))
                .isInstanceOf(ConflictException.class);

        var cancelada = conciliacaoService.cancelar(conciliacao.id(),
                new ConciliacaoBancariaCancelamentoRequest("Conciliacao selecionada incorretamente"));

        assertThat(cancelada.status()).isEqualTo(StatusConciliacaoBancaria.CANCELADA);
        assertThat(lancamentoRepository.findById(lancamento.id()).orElseThrow().getConciliado()).isFalse();
        assertThat(extratoRepository.findById(extrato.id()).orElseThrow().getStatus())
                .isEqualTo(StatusExtratoBancario.IMPORTADO);
    }

    private String hash(String valor) {
        return (valor.replaceAll("[^A-Fa-f0-9]", "b")
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb").substring(0, 64);
    }

    private String documento(String valor) {
        String numeros = valor.replaceAll("\\D", "");
        return ("98765432000199" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
