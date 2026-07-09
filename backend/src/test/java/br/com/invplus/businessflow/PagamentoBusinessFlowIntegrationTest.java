package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaPagarRepository;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Fluxo financeiro: ContaPagar -> ParcelaPagar -> FormaPagamento -> Pagamento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PagamentoBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private ContaPagarRepository contaPagarRepository;
    @Autowired
    private ParcelaPagarRepository parcelaPagarRepository;
    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;
    @Autowired
    private MovimentoFinanceiroRepository movimentoFinanceiroRepository;

    @Test
    void devePagarParcelaAtualizarContaEPermitirEstorno() throws Exception {
        String token = login().get("accessToken").asString();
        ContaPagar conta = prepararConta();
        ParcelaPagar parcela = prepararParcela(conta);
        FormaPagamento formaPagamento = prepararFormaPagamento();

        String response = mockMvc.perform(post("/api/v1/pagamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(parcela.getId(), formaPagamento.getId(), "600.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andReturn().getResponse().getContentAsString();
        String pagamentoId = objectMapper.readTree(response).get("id").asString();
        org.assertj.core.api.Assertions.assertThat(movimentoFinanceiroRepository
                .findByOrigemAndOrigemIdAndAtivoTrueAndDeletedAtIsNull(
                        OrigemMovimentoFinanceiro.PAGAMENTO, UUID.fromString(pagamentoId))
                .orElseThrow().getStatus()).isEqualTo(StatusMovimentoFinanceiro.ATIVO);

        mockMvc.perform(get("/api/v1/parcelas-pagar/" + parcela.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGA"))
                .andExpect(jsonPath("$.valorAberto").value(0.00));

        mockMvc.perform(get("/api/v1/contas-pagar/" + conta.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGA"))
                .andExpect(jsonPath("$.valorPago").value(600.00));

        mockMvc.perform(post("/api/v1/pagamentos/" + pagamentoId + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
        org.assertj.core.api.Assertions.assertThat(movimentoFinanceiroRepository
                .findByOrigemAndOrigemIdAndAtivoTrueAndDeletedAtIsNull(
                        OrigemMovimentoFinanceiro.PAGAMENTO, UUID.fromString(pagamentoId))
                .orElseThrow().getStatus()).isEqualTo(StatusMovimentoFinanceiro.CANCELADO);

        mockMvc.perform(get("/api/v1/contas-pagar/" + conta.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.valorPago").value(0.00))
                .andExpect(jsonPath("$.valorAberto").value(600.00));
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@invplus.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private ContaPagar prepararConta() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Empresa empresa = new Empresa("Empresa BF REC " + sufixo, "Empresa BF REC " + sufixo,
                documentoNumerico("12", "000199"), null, null, null);
        empresa = empresaRepository.saveAndFlush(empresa);

        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor BF REC " + sufixo, null,
                documentoNumerico("98", "000188"), null, null, null, null, null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoa(pessoa);
        fornecedor.atualizar(null, 0, null);
        fornecedor = fornecedorRepository.saveAndFlush(fornecedor);

        ContaPagar conta = new ContaPagar();
        conta.abrir(empresa, null, fornecedor, null, "CRBFREC-" + sufixo.toUpperCase(), "Conta fluxo pagamento",
                LocalDate.of(2026, 7, 8), new BigDecimal("600.00"), null);
        return contaPagarRepository.saveAndFlush(conta);
    }

    private ParcelaPagar prepararParcela(ContaPagar conta) {
        ParcelaPagar parcela = new ParcelaPagar();
        parcela.abrir(conta, 1, LocalDate.of(2026, 7, 10), new BigDecimal("600.00"), null);
        return parcelaPagarRepository.saveAndFlush(parcela);
    }

    private FormaPagamento prepararFormaPagamento() {
        String codigo = "BFREC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        FormaPagamento formaPagamento = new FormaPagamento();
        formaPagamento.atualizar(codigo, "Pix " + codigo, TipoFormaPagamento.PIX, false, false);
        return formaPagamentoRepository.saveAndFlush(formaPagamento);
    }

    private String payload(UUID parcelaId, UUID formaPagamentoId, String valor) {
        return """
                {"parcelaPagarId":"%s","formaPagamentoId":"%s","valorPago":%s,"juros":0,"multa":0,"desconto":0,"dataPagamento":"2026-07-10","observacao":"PIX"}
                """.formatted(parcelaId, formaPagamentoId, valor);
    }

    private String documentoNumerico(String prefixo, String sufixo) {
        String base = prefixo + Math.abs(UUID.randomUUID().hashCode()) + sufixo + "00000000000000";
        return base.substring(0, 14);
    }
}
