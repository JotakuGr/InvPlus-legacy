package br.com.invplus.concurrency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioLancamentoRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.repository.ConciliacaoBancariaRepository;
import br.com.invplus.modules.financeiro.service.ConciliacaoBancariaService;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioLancamentoService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioService;
import br.com.invplus.modules.financeiro.service.MovimentoFinanceiroService;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.support.context.AbstractContextoEmpresarialIntegrationTest;

/** Confirma que duas conciliacoes concorrentes nao duplicam o mesmo lancamento. */
@SpringBootTest
@ActiveProfiles("test")
class ConciliacaoBancariaConcurrencyTest extends AbstractContextoEmpresarialIntegrationTest {

    @Autowired private ConciliacaoBancariaService conciliacaoService;
    @Autowired private ConciliacaoBancariaRepository conciliacaoRepository;
    @Autowired private ContaBancariaService contaBancariaService;
    @Autowired private ExtratoBancarioService extratoService;
    @Autowired private ExtratoBancarioLancamentoService lancamentoService;
    @Autowired private MovimentoFinanceiroService movimentoService;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private FormaPagamentoRepository formaPagamentoRepository;

    @Test
    void apenasUmaRequisicaoDeveConciliarOMesmoLancamento() throws Exception {
        Cenario cenario = prepararCenario();
        var request = new ConciliacaoBancariaRequest(cenario.lancamentoId(), cenario.movimentoId(),
                TipoConciliacaoBancaria.MANUAL, "Concorrencia");
        CountDownLatch largada = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var tarefa = (java.util.concurrent.Callable<Boolean>) () -> {
                testEmpresaContext.ativarEmpresa(UUID.randomUUID(), cenario.empresaId());
                largada.await();
                try {
                    conciliacaoService.criar(request);
                    return true;
                } catch (RuntimeException ex) {
                    return false;
                } finally {
                    testEmpresaContext.limpar();
                }
            };
            Future<Boolean> primeira = executor.submit(tarefa);
            Future<Boolean> segunda = executor.submit(tarefa);
            largada.countDown();

            long sucessos = java.util.stream.Stream.of(primeira.get(), segunda.get())
                    .filter(Boolean.TRUE::equals).count();
            assertThat(sucessos).isEqualTo(1);
        }

        assertThat(conciliacaoRepository.countByExtratoBancarioLancamentoIdAndStatus(
                cenario.lancamentoId(), StatusConciliacaoBancaria.CONCILIADA)).isEqualTo(1);
    }

    private Cenario prepararCenario() {
        String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Empresa empresa = empresaRepository.saveAndFlush(new Empresa("Empresa Concorrente " + sufixo,
                "Empresa Concorrente " + sufixo, documento(sufixo), null, null, null));
        ativarEmpresa(empresa.getId());
        FormaPagamento forma = new FormaPagamento();
        forma.atualizar("CC" + sufixo, "Forma Concorrente " + sufixo,
                TipoFormaPagamento.PIX, false, false);
        forma = formaPagamentoRepository.saveAndFlush(forma);
        var conta = contaBancariaService.criar(new ContaBancariaRequest(null,
                "001", "Banco Concorrente", "3" + sufixo, null, "7" + sufixo, null,
                TipoContaBancaria.CORRENTE, "Empresa Concorrente", null, null,
                BigDecimal.ZERO, LocalDate.of(2026, 7, 10), true));
        var extrato = extratoService.importar(new ExtratoBancarioRequest(conta.id(),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), BigDecimal.ZERO,
                new BigDecimal("75.00"), OrigemExtratoBancario.OFX,
                "concorrente.ofx", hash(sufixo), null));
        var lancamento = lancamentoService.criar(new ExtratoBancarioLancamentoRequest(extrato.id(),
                LocalDate.of(2026, 7, 10), "Credito concorrente", null,
                TipoLancamentoBancario.CREDITO, new BigDecimal("75.00"), null,
                "RACE-" + sufixo, CategoriaLancamentoBancario.PIX, null));
        var movimento = movimentoService.criarManual(new MovimentoFinanceiroRequest(null,
                forma.getId(), null, null, null, null, null, TipoMovimentoFinanceiro.ENTRADA,
                OrigemMovimentoFinanceiro.AJUSTE_MANUAL, null, new BigDecimal("75.00"),
                LocalDate.of(2026, 7, 10), "Entrada concorrente", null));
        return new Cenario(empresa.getId(), lancamento.id(), movimento.id());
    }

    private String hash(String valor) {
        return (valor.replaceAll("[^A-Fa-f0-9]", "c")
                + "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc").substring(0, 64);
    }

    private String documento(String valor) {
        String numeros = valor.replaceAll("\\D", "");
        return ("11222333000199" + numeros).substring(numeros.length());
    }

    private record Cenario(UUID empresaId, UUID lancamentoId, UUID movimentoId) {
    }
}
