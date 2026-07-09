package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integracao dos endpoints de ParcelaPagar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParcelaPagarControllerTests {

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
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Test
    void deveCriarListarBuscarAtualizarBaixarECancelarParcela() throws Exception {
        String token = login().get("accessToken").asString();
        ContaPagar conta = prepararConta("900.00");

        String response = mockMvc.perform(post("/api/v1/parcelas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadParcela(conta.getId(), 1, "300.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroParcela").value(1))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/parcelas-pagar/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/parcelas-pagar?contaPagarId=" + conta.getId() + "&status=ABERTA&page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numeroConta").value(conta.getNumero()));

        mockMvc.perform(put("/api/v1/parcelas-pagar/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadParcela(conta.getId(), 1, "350.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(350.00));

        mockMvc.perform(post("/api/v1/parcelas-pagar/" + id + "/baixar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valorPago\":100.00,\"juros\":0,\"multa\":0,\"desconto\":0,\"dataPagamento\":\"2026-07-10\",\"observacao\":\"PIX\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARCIALMENTE_PAGA"))
                .andExpect(jsonPath("$.valorAberto").value(250.00));

        String segundaParcela = mockMvc.perform(post("/api/v1/parcelas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadParcela(conta.getId(), 2, "100.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String segundaParcelaId = objectMapper.readTree(segundaParcela).get("id").asString();

        mockMvc.perform(post("/api/v1/parcelas-pagar/" + segundaParcelaId + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void deveGerarParcelasPelaCondicaoEBaixarTotal() throws Exception {
        String token = login().get("accessToken").asString();
        ContaPagar conta = prepararConta("900.00");
        CondicaoPagamento condicao = prepararCondicao();

        String response = mockMvc.perform(post("/api/v1/contas-pagar/" + conta.getId() + "/gerar-parcelas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condicaoPagamentoId\":\"%s\",\"dataBase\":\"2026-07-10\"}".formatted(condicao.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].valor").value(300.00))
                .andExpect(jsonPath("$[1].dataVencimento").value("2026-08-10"))
                .andReturn().getResponse().getContentAsString();
        JsonNode parcelas = objectMapper.readTree(response);
        String primeiraParcelaId = parcelas.get(0).get("id").asString();

        mockMvc.perform(post("/api/v1/parcelas-pagar/" + primeiraParcelaId + "/baixar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valorPago\":300.00,\"juros\":0,\"multa\":0,\"desconto\":0,\"dataPagamento\":\"2026-07-10\",\"observacao\":\"PIX\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGA"))
                .andExpect(jsonPath("$.valorAberto").value(0.00));

        mockMvc.perform(post("/api/v1/contas-pagar/" + conta.getId() + "/gerar-parcelas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condicaoPagamentoId\":\"%s\",\"dataBase\":\"2026-07-10\"}".formatted(condicao.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBloquearDuplicidadeValorInvalidoEBaixaAcimaDoSaldo() throws Exception {
        String token = login().get("accessToken").asString();
        ContaPagar conta = prepararConta("900.00");

        mockMvc.perform(post("/api/v1/parcelas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadParcela(conta.getId(), 1, "300.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/v1/parcelas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadParcela(conta.getId(), 1, "300.00")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/parcelas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadParcela(conta.getId(), 2, "0.00")))
                .andExpect(status().isBadRequest());

        String parcela = mockMvc.perform(post("/api/v1/parcelas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadParcela(conta.getId(), 2, "300.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String parcelaId = objectMapper.readTree(parcela).get("id").asString();

        mockMvc.perform(post("/api/v1/parcelas-pagar/" + parcelaId + "/baixar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valorPago\":301.00,\"juros\":0,\"multa\":0,\"desconto\":0,\"dataPagamento\":\"2026-07-10\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/parcelas-pagar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Parcela Pagar",
                "sem-parcela-pagar-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("CONTA_RECEBER:VISUALIZAR")
                .build());

        mockMvc.perform(get("/api/v1/parcelas-pagar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@invplus.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private ContaPagar prepararConta(String valor) {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Empresa empresa = new Empresa("Empresa PR " + sufixo, "Empresa PR " + sufixo,
                documentoNumerico("12", "000199"), null, null, null);
        empresa = empresaRepository.saveAndFlush(empresa);

        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor PR " + sufixo, null,
                documentoNumerico("98", "000188"), null, null, null, null, null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoa(pessoa);
        fornecedor.atualizar(null, 0, null);
        fornecedor = fornecedorRepository.saveAndFlush(fornecedor);

        ContaPagar conta = new ContaPagar();
        conta.abrir(empresa, null, fornecedor, null, "CPP-" + sufixo.toUpperCase(), "Conta parcelas",
                LocalDate.of(2026, 7, 8), new BigDecimal(valor), null);
        return contaPagarRepository.saveAndFlush(conta);
    }

    private CondicaoPagamento prepararCondicao() {
        String codigo = "CPR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CondicaoPagamento condicao = new CondicaoPagamento();
        condicao.atualizar(codigo, "Condicao " + codigo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                RegraVencimento.PROXIMO_DIA_UTIL, true);
        condicao.substituirParcelas(List.of(parcelaCondicao(condicao, 1, 0, "33.3333"),
                parcelaCondicao(condicao, 2, 30, "33.3333"), parcelaCondicao(condicao, 3, 60, "33.3334")));
        return condicaoPagamentoRepository.saveAndFlush(condicao);
    }

    private CondicaoPagamentoParcela parcelaCondicao(CondicaoPagamento condicao, int numero, int dias, String percentual) {
        CondicaoPagamentoParcela parcela = new CondicaoPagamentoParcela();
        parcela.atualizar(condicao, numero, dias, new BigDecimal(percentual));
        return parcela;
    }

    private String payloadParcela(UUID contaId, int numero, String valor) {
        return """
                {"contaPagarId":"%s","numeroParcela":%d,"dataVencimento":"2026-07-10","valor":%s,"observacao":"Teste"}
                """.formatted(contaId, numero, valor);
    }

    private String documentoNumerico(String prefixo, String sufixo) {
        String base = prefixo + Math.abs(UUID.randomUUID().hashCode()) + sufixo + "00000000000000";
        return base.substring(0, 14);
    }
}
