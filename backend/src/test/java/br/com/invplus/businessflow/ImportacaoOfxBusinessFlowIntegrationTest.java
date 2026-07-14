package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.importacao.service.ImportacaoExtratoService;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.service.ConciliacaoBancariaService;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.modules.financeiro.service.MovimentoFinanceiroService;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.security.service.AuthenticatedUserDetails;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

/** Valida OFX -> extrato -> lancamento -> movimento -> conciliacao. */
@SpringBootTest
@ActiveProfiles("test")
class ImportacaoOfxBusinessFlowIntegrationTest {

    @Autowired private ImportacaoExtratoService importacaoService;
    @Autowired private ConciliacaoBancariaService conciliacaoService;
    @Autowired private MovimentoFinanceiroService movimentoService;
    @Autowired private ContaBancariaService contaBancariaService;
    @Autowired private ExtratoBancarioLancamentoRepository lancamentoRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private FormaPagamentoRepository formaPagamentoRepository;

    @AfterEach
    @SuppressWarnings("unused")
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveImportarOfxEConciliarLancamentoComMovimento() throws Exception {
        String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Empresa empresa = empresaRepository.saveAndFlush(new Empresa("Empresa OFX Flow " + sufixo,
                "Empresa OFX Flow " + sufixo, documento(sufixo), null, null, null));
        UUID contaId = contaBancariaService.criar(new ContaBancariaRequest(empresa.getId(), null, "001",
                "Banco OFX Flow", "1234", null, "99999", "9", TipoContaBancaria.CORRENTE,
                "Empresa OFX Flow", null, null, BigDecimal.ZERO, LocalDate.of(2026, 7, 1), true)).id();
        ativarContexto(empresa.getId());
        FormaPagamento forma = new FormaPagamento();
        forma.atualizar("OF" + sufixo, "Pix OFX " + sufixo, TipoFormaPagamento.PIX, false, false);
        forma = formaPagamentoRepository.saveAndFlush(forma);

        var importacao = importacaoService.importarOfx(arquivo(sufixo), contaId, "Fluxo completo");
        var lancamento = lancamentoRepository.findByExtratoBancarioIdAndAtivoTrueAndDeletedAtIsNull(
                importacao.extratoBancarioId()).getFirst();
        var movimento = movimentoService.criarManual(new MovimentoFinanceiroRequest(empresa.getId(), null,
                forma.getId(), null, null, TipoMovimentoFinanceiro.ENTRADA,
                OrigemMovimentoFinanceiro.AJUSTE_MANUAL, null, new BigDecimal("150.00"),
                LocalDate.of(2026, 7, 10), "Recebimento OFX", null));
        var conciliacao = conciliacaoService.criar(new ConciliacaoBancariaRequest(lancamento.getId(),
                movimento.id(), TipoConciliacaoBancaria.MANUAL, "Conciliado apos importacao"));

        assertThat(importacao.quantidadeImportada()).isEqualTo(1);
        assertThat(lancamento.getConciliado()).isFalse();
        assertThat(conciliacao.status()).isEqualTo(StatusConciliacaoBancaria.CONCILIADA);
        assertThat(lancamentoRepository.findById(lancamento.getId()).orElseThrow().getConciliado()).isTrue();
    }

    private MockMultipartFile arquivo(String nonce) throws Exception {
        byte[] bytes;
        try (var stream = getClass().getResourceAsStream("/ofx/extrato-valido.ofx")) {
            bytes = stream.readAllBytes();
        }
        String conteudo = new String(bytes, StandardCharsets.UTF_8) + "\n<!-- " + nonce + " -->";
        return new MockMultipartFile("arquivo", "extrato-valido.ofx", "application/x-ofx",
                conteudo.getBytes(StandardCharsets.UTF_8));
    }

    private void ativarContexto(UUID empresaId) {
        var principal = new AuthenticatedUserDetails(
                UUID.randomUUID(), "teste@invplus.com", "", List.of(), true, empresaId, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("66777888000199" + numeros).substring(numeros.length());
    }
}
