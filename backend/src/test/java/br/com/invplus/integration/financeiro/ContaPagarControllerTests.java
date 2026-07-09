package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
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
 * Testes de integracao dos endpoints de ContaPagar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContaPagarControllerTests {

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
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Test
    void deveCriarListarBuscarAtualizarECancelarContaPagar() throws Exception {
        String token = login().get("accessToken").asString();
        Dados dados = prepararDados();
        String numero = "CP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String response = mockMvc.perform(post("/api/v1/contas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, numero, "100.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(numero))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.valorAberto").value(100.00))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/contas-pagar/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/contas-pagar?numero=" + numero + "&status=ABERTA&page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numero").value(numero));

        mockMvc.perform(put("/api/v1/contas-pagar/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, numero, "150.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(150.00))
                .andExpect(jsonPath("$.valorAberto").value(150.00));

        mockMvc.perform(post("/api/v1/contas-pagar/" + id + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void deveBloquearDuplicidadeEValorInvalido() throws Exception {
        String token = login().get("accessToken").asString();
        Dados dados = prepararDados();
        String numero = "DUP-CP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        mockMvc.perform(post("/api/v1/contas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, numero, "100.00")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/contas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, numero, "100.00")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/contas-pagar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, "INV-" + UUID.randomUUID().toString().substring(0, 8), "0.00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/contas-pagar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Conta Pagar",
                "sem-conta-pagar-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("PEDIDO_VENDA:*")
                .build());

        mockMvc.perform(get("/api/v1/contas-pagar")
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

    private Dados prepararDados() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Empresa empresa = new Empresa("Empresa CP " + sufixo, "Empresa CP " + sufixo,
                documento("12", "99"), null, null, null);
        empresa = empresaRepository.saveAndFlush(empresa);

        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor CP " + sufixo, null,
                documento("98", "88"), null, null, null, null, null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoa(pessoa);
        fornecedor.atualizar(null, 0, null);
        fornecedor = fornecedorRepository.saveAndFlush(fornecedor);
        return new Dados(empresa.getId().toString(), fornecedor.getId().toString());
    }

    private String documento(String prefixo, String sufixo) {
        String meio = String.format("%010d", Math.abs((long) UUID.randomUUID().hashCode()) % 10_000_000_000L);
        return prefixo + meio + sufixo;
    }

    private String payload(Dados dados, String numero, String valor) {
        return """
                {"empresaId":"%s","fornecedorId":"%s","numero":"%s","descricao":"Conta %s","dataEmissao":"2026-07-08","valorTotal":%s,"observacao":"Teste"}
                """.formatted(dados.empresaId(), dados.fornecedorId(), numero, numero, valor);
    }

    private record Dados(String empresaId, String fornecedorId) {
    }
}
