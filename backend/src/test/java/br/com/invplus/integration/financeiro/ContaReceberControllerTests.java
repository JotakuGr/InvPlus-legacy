package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.service.JwtService;
import br.com.invplus.support.JwtContextoTestSupport;
import br.com.invplus.support.security.UsuarioTestFactory;
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
 * Testes de integracao dos endpoints de ContaReceber.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContaReceberControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioTestFactory usuarioTestFactory;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Test
    void deveCriarListarBuscarAtualizarECancelarContaReceber() throws Exception {
        Dados dados = prepararDados();
        String token = tokenAdmin(dados.empresa());
        String numero = "CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String response = mockMvc.perform(post("/api/v1/contas-receber")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, numero, "100.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(numero))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.valorAberto").value(100.00))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/contas-receber/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/contas-receber?numero=" + numero + "&status=ABERTA&page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numero").value(numero));

        mockMvc.perform(put("/api/v1/contas-receber/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, numero, "150.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(150.00))
                .andExpect(jsonPath("$.valorAberto").value(150.00));

        mockMvc.perform(post("/api/v1/contas-receber/" + id + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void deveBloquearDuplicidadeEValorInvalido() throws Exception {
        Dados dados = prepararDados();
        String token = tokenAdmin(dados.empresa());
        String numero = "DUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        mockMvc.perform(post("/api/v1/contas-receber")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, numero, "100.00")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/contas-receber")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, numero, "100.00")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/contas-receber")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(dados, "INV-" + UUID.randomUUID().toString().substring(0, 8), "0.00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/contas-receber"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Conta Receber",
                "sem-conta-receber-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);
        Empresa empresa = prepararDados().empresa();
        String token = JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, usuario, empresa);

        mockMvc.perform(get("/api/v1/contas-receber")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private Dados prepararDados() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        String documentoEmpresa = "12" + Math.abs(UUID.randomUUID().hashCode()) + "000199";
        documentoEmpresa = documentoEmpresa.substring(0, Math.min(14, documentoEmpresa.length()));
        String documentoPessoa = "98" + Math.abs(UUID.randomUUID().hashCode()) + "000188";
        documentoPessoa = documentoPessoa.substring(0, Math.min(14, documentoPessoa.length()));

        Empresa empresa = new Empresa("Empresa CR " + sufixo, "Empresa CR " + sufixo, documentoEmpresa, null, null, null);
        empresa = empresaRepository.saveAndFlush(empresa);

        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente CR " + sufixo, null, documentoPessoa, null, null, null, null, null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.atualizar(java.math.BigDecimal.ZERO, null, null, null);
        cliente = clienteRepository.saveAndFlush(cliente);
        return new Dados(empresa, cliente.getId().toString());
    }

    private String payload(Dados dados, String numero, String valor) {
        return """
                {"clienteId":"%s","numero":"%s","descricao":"Conta %s","dataEmissao":"2026-07-08","valorTotal":%s,"observacao":"Teste"}
                """.formatted(dados.clienteId(), numero, numero, valor);
    }

    private String tokenAdmin(Empresa empresa) {
        Usuario admin = usuarioTestFactory.criarAtivoComPerfilAdmin();
        return JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, admin, empresa);
    }

    private record Dados(Empresa empresa, String clienteId) {
    }
}
