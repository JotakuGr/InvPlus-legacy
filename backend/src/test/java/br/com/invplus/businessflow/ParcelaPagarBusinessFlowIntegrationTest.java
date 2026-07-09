package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamentoParcela;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
 * Fluxo financeiro: ContaPagar -> CondicaoPagamento -> ParcelaPagar -> baixa total.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParcelaPagarBusinessFlowIntegrationTest {

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
    private CondicaoPagamentoRepository condicaoPagamentoRepository;

    @Test
    void deveGerarParcelasBaixarTudoEFecharContaPagar() throws Exception {
        String token = login().get("accessToken").asString();
        ContaPagar conta = prepararConta();
        CondicaoPagamento condicao = prepararCondicao();

        String response = mockMvc.perform(post("/api/v1/contas-pagar/" + conta.getId() + "/gerar-parcelas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condicaoPagamentoId\":\"%s\",\"dataBase\":\"2026-07-10\"}".formatted(condicao.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        JsonNode parcelas = objectMapper.readTree(response);

        for (int i = 0; i < parcelas.size(); i++) {
            String parcelaId = parcelas.get(i).get("id").asString();
            mockMvc.perform(post("/api/v1/parcelas-pagar/" + parcelaId + "/baixar")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"valorPago\":300.00,\"juros\":0,\"multa\":0,\"desconto\":0,\"dataPagamento\":\"2026-07-10\",\"observacao\":\"PIX\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAGA"));
        }

        mockMvc.perform(get("/api/v1/contas-pagar/" + conta.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGA"))
                .andExpect(jsonPath("$.valorPago").value(600.00))
                .andExpect(jsonPath("$.valorAberto").value(0.00));
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
        Empresa empresa = new Empresa("Empresa BF PR " + sufixo, "Empresa BF PR " + sufixo,
                documentoNumerico("12", "000199"), null, null, null);
        empresa = empresaRepository.saveAndFlush(empresa);

        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor BF PR " + sufixo, null,
                documentoNumerico("98", "000188"), null, null, null, null, null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoa(pessoa);
        fornecedor.atualizar(null, 0, null);
        fornecedor = fornecedorRepository.saveAndFlush(fornecedor);

        ContaPagar conta = new ContaPagar();
        conta.abrir(empresa, null, fornecedor, null, "CPBF-" + sufixo.toUpperCase(), "Conta a pagar do fluxo financeiro",
                LocalDate.of(2026, 7, 8), new BigDecimal("600.00"), null);
        return contaPagarRepository.saveAndFlush(conta);
    }

    private CondicaoPagamento prepararCondicao() {
        String codigo = "BFPR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CondicaoPagamento condicao = new CondicaoPagamento();
        condicao.atualizar(codigo, "Condicao " + codigo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                RegraVencimento.MANTER_DATA, true);
        condicao.substituirParcelas(List.of(parcelaCondicao(condicao, 1, 0), parcelaCondicao(condicao, 2, 30)));
        return condicaoPagamentoRepository.saveAndFlush(condicao);
    }

    private CondicaoPagamentoParcela parcelaCondicao(CondicaoPagamento condicao, int numero, int dias) {
        CondicaoPagamentoParcela parcela = new CondicaoPagamentoParcela();
        parcela.atualizar(condicao, numero, dias, new BigDecimal("50.00"));
        return parcela;
    }

    private String documentoNumerico(String prefixo, String sufixo) {
        String base = prefixo + Math.abs(UUID.randomUUID().hashCode()) + sufixo + "00000000000000";
        return base.substring(0, 14);
    }
}
