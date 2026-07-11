package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.conciliacao.dto.*;
import br.com.invplus.modules.financeiro.conciliacao.service.*;
import br.com.invplus.modules.financeiro.dto.request.*;
import br.com.invplus.modules.financeiro.enums.*;
import br.com.invplus.modules.financeiro.service.*;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CentralConciliacaoBancariaBusinessFlowIntegrationTest {
    @Autowired EmpresaRepository empresaRepository;
    @Autowired FormaPagamentoRepository formaRepository;
    @Autowired ContaBancariaService contaService;
    @Autowired ExtratoBancarioService extratoService;
    @Autowired ExtratoBancarioLancamentoService lancamentoService;
    @Autowired MovimentoFinanceiroService movimentoService;
    @Autowired ConciliacaoBancariaPainelService painelService;
    @Autowired ConciliacaoBancariaLoteService loteService;

    @Test
    void deveConsultarCentralEConfirmarDoisParesEmLote() {
        String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Empresa empresa = empresaRepository.saveAndFlush(new Empresa("Central " + sufixo,
                "Central " + sufixo, documento(sufixo), null, null, null));
        FormaPagamento forma = new FormaPagamento();
        forma.atualizar("CE" + sufixo, "PIX Central", TipoFormaPagamento.PIX, false, false);
        forma = formaRepository.saveAndFlush(forma);
        var conta = contaService.criar(new ContaBancariaRequest(empresa.getId(), null, "001", "Banco Central",
                sufixo, null, "9" + sufixo, null, TipoContaBancaria.CORRENTE, "Central", null, null,
                BigDecimal.ZERO, LocalDate.of(2026, 7, 1), true));
        var extrato = extratoService.importar(new ExtratoBancarioRequest(conta.id(), LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31), BigDecimal.ZERO, new BigDecimal("800.00"),
                OrigemExtratoBancario.OFX, "central-" + sufixo + ".ofx", hash(sufixo), null));
        var l1 = lancamentoService.criar(lancamento(extrato.id(), "500.00", "Mercado Central", "A" + sufixo));
        var l2 = lancamentoService.criar(lancamento(extrato.id(), "300.00", "Loja Norte", "B" + sufixo));
        var m1 = movimentoService.criarManual(movimento(empresa.getId(), forma.getId(), conta.id(),
                "500.00", "Mercado Central"));
        var m2 = movimentoService.criarManual(movimento(empresa.getId(), forma.getId(), conta.id(),
                "300.00", "Loja Norte"));

        var antes = painelService.buscarPainel(empresa.getId(), conta.id(), null, null);
        assertThat(antes.indicadores().quantidadeLancamentosPendentes()).isEqualTo(2);
        var pendencias = painelService.buscarPendencias(empresa.getId(), extrato.id(), conta.id(),
                null, null, null, null, true, null, PageRequest.of(0, 20));
        assertThat(pendencias).hasSize(2).allMatch(item -> item.melhorSugestao() != null);

        var lote = loteService.confirmar(empresa.getId(), new ConciliacaoBancariaLoteRequest(List.of(
                new ConciliacaoBancariaLoteItemRequest(l1.id(), m1.id(), "Central"),
                new ConciliacaoBancariaLoteItemRequest(l2.id(), m2.id(), "Central"))));
        assertThat(lote.quantidadeConciliada()).isEqualTo(2);
        assertThat(lote.quantidadeRejeitada()).isZero();
        var depois = painelService.buscarPainel(empresa.getId(), conta.id(), null, null);
        assertThat(depois.indicadores().quantidadeLancamentosPendentes()).isZero();
        assertThat(depois.indicadores().percentualConciliado()).isEqualByComparingTo("100.00");
    }

    private ExtratoBancarioLancamentoRequest lancamento(UUID extratoId, String valor,
            String descricao, String identificador) {
        return new ExtratoBancarioLancamentoRequest(extratoId, LocalDate.of(2026, 7, 10), descricao, null,
                TipoLancamentoBancario.CREDITO, new BigDecimal(valor), null, identificador,
                CategoriaLancamentoBancario.PIX, null);
    }

    private MovimentoFinanceiroRequest movimento(UUID empresaId, UUID formaId, UUID contaId,
            String valor, String descricao) {
        return new MovimentoFinanceiroRequest(empresaId, null, formaId, null, null, null, null, contaId,
                TipoMovimentoFinanceiro.ENTRADA, OrigemMovimentoFinanceiro.AJUSTE_MANUAL, null,
                new BigDecimal(valor), LocalDate.of(2026, 7, 10), descricao, null);
    }

    private String hash(String valor) {
        return (valor.replaceAll("[^A-Fa-f0-9]", "c")
                + "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc").substring(0, 64);
    }

    private String documento(String valor) {
        String numeros = valor.replaceAll("\\D", "");
        return ("76543210000199" + numeros).substring(numeros.length());
    }
}
