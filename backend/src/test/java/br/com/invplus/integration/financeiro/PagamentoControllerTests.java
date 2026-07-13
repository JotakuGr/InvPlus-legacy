package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.enums.StatusParcelaPagar;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaPagarRepository;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.service.JwtService;
import br.com.invplus.support.JwtContextoTestSupport;
import br.com.invplus.support.security.UsuarioTestFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integracao dos endpoints de Pagamento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PagamentoControllerTests {

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
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Autowired
    private UsuarioTestFactory usuarioTestFactory;

    @Test
    void deveCriarListarBuscarECancelarPagamento() throws Exception {
        ParcelaPagar parcela = prepararParcela("300.00");
        String token = tokenAdmin(parcela.getContaPagar().getEmpresa());
        FormaPagamento formaPagamento = prepararFormaPagamento();

        String response = mockMvc.perform(post("/api/v1/pagamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(parcela.getId(), formaPagamento.getId(), "100.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.valorPago").value(100.00))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/pagamentos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/pagamentos?parcelaPagarId=" + parcela.getId() + "&page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        ParcelaPagar parcial = parcelaPagarRepository.findById(parcela.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(parcial.getStatus()).isEqualTo(StatusParcelaPagar.PARCIALMENTE_PAGA);
        org.assertj.core.api.Assertions.assertThat(parcial.getValorAberto()).isEqualByComparingTo("200.00");

        mockMvc.perform(post("/api/v1/pagamentos/" + id + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        ParcelaPagar estornada = parcelaPagarRepository.findById(parcela.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(estornada.getStatus()).isEqualTo(StatusParcelaPagar.ABERTA);
        org.assertj.core.api.Assertions.assertThat(estornada.getValorAberto()).isEqualByComparingTo("300.00");
    }

    @Test
    void deveBloquearParcelaPagaFormaInexistenteValorInvalidoEAcimaDoSaldo() throws Exception {
        ParcelaPagar parcela = prepararParcela("300.00");
        String token = tokenAdmin(parcela.getContaPagar().getEmpresa());
        FormaPagamento formaPagamento = prepararFormaPagamento();

        mockMvc.perform(post("/api/v1/pagamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(parcela.getId(), formaPagamento.getId(), "0.00")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/pagamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(parcela.getId(), formaPagamento.getId(), "301.00")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/pagamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(parcela.getId(), UUID.randomUUID(), "100.00")))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/pagamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(parcela.getId(), formaPagamento.getId(), "300.00")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/pagamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(parcela.getId(), formaPagamento.getId(), "1.00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/pagamentos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Pagamento",
                "sem-pagamento-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);
        Empresa empresa = prepararConta("1.00").getEmpresa();
        String token = JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, usuario, empresa);

        mockMvc.perform(get("/api/v1/pagamentos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String tokenAdmin(Empresa empresa) {
        Usuario admin = usuarioTestFactory.criarAtivoComPerfilAdmin();
        return JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, admin, empresa);
    }

    private ParcelaPagar prepararParcela(String valor) {
        ContaPagar conta = prepararConta(valor);
        ParcelaPagar parcela = new ParcelaPagar();
        parcela.abrir(conta, 1, LocalDate.of(2026, 7, 10), new BigDecimal(valor), null);
        return parcelaPagarRepository.saveAndFlush(parcela);
    }

    private ContaPagar prepararConta(String valor) {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Empresa empresa = new Empresa("Empresa REC " + sufixo, "Empresa REC " + sufixo,
                documentoNumerico("12", "000199"), null, null, null);
        empresa = empresaRepository.saveAndFlush(empresa);

        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor REC " + sufixo, null,
                documentoNumerico("98", "000188"), null, null, null, null, null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoa(pessoa);
        fornecedor.atualizar(null, 0, null);
        fornecedor = fornecedorRepository.saveAndFlush(fornecedor);

        ContaPagar conta = new ContaPagar();
        conta.abrir(empresa, null, fornecedor, null, "CRR-" + sufixo.toUpperCase(), "Conta pagamento",
                LocalDate.of(2026, 7, 8), new BigDecimal(valor), null);
        return contaPagarRepository.saveAndFlush(conta);
    }

    private FormaPagamento prepararFormaPagamento() {
        String codigo = "PIX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
