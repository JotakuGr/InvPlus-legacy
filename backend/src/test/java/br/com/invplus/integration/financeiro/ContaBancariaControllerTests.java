package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import java.math.BigDecimal;
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
 * Testes REST do cadastro de contas bancarias.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContaBancariaControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Test
    void deveCriarFiltrarAtualizarEExcluirContaBancaria() throws Exception {
        String token = login().get("accessToken").asString();
        Empresa empresa = prepararEmpresa();
        String sufixo = sufixo();
        String id = criar(token, empresa.getId(), "001", "1234" + sufixo, "98765", true);

        mockMvc.perform(get("/api/v1/contas-bancarias")
                        .queryParam("empresaId", empresa.getId().toString())
                        .queryParam("bancoCodigo", "001")
                        .queryParam("tipoConta", "CORRENTE")
                        .queryParam("principal", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].saldoAtual").value(100.00));

        mockMvc.perform(put("/api/v1/contas-bancarias/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), "237", "2222" + sufixo, "12345", false, BigDecimal.ZERO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bancoCodigo").value("237"))
                .andExpect(jsonPath("$.principal").value(false));

        mockMvc.perform(delete("/api/v1/contas-bancarias/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/contas-bancarias/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBloquearDuplicidadeSegundaPrincipalEDocumentoInvalido() throws Exception {
        String token = login().get("accessToken").asString();
        Empresa empresa = prepararEmpresa();
        String sufixo = sufixo();
        criar(token, empresa.getId(), "001", "3333" + sufixo, "98765", true);

        mockMvc.perform(post("/api/v1/contas-bancarias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), "001", "3333" + sufixo, "98765", false, BigDecimal.ZERO)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/contas-bancarias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), "104", "4444" + sufixo, "22222", true, BigDecimal.ZERO)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/contas-bancarias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadComDocumento(empresa.getId(), "341", "5555" + sufixo, "33333", "12345")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/contas-bancarias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Conta Bancaria",
                "sem-conta-bancaria-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("PAGAMENTO:VISUALIZAR")
                .build());

        mockMvc.perform(get("/api/v1/contas-bancarias")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String criar(String token, UUID empresaId, String bancoCodigo,
            String agencia, String numeroConta, Boolean principal) throws Exception {
        String response = mockMvc.perform(post("/api/v1/contas-bancarias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresaId, bancoCodigo, agencia, numeroConta, principal, new BigDecimal("100.00"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(UUID empresaId, String bancoCodigo, String agencia,
            String numeroConta, Boolean principal, BigDecimal saldoInicial) {
        return """
                {
                  "empresaId": "%s",
                  "bancoCodigo": "%s",
                  "bancoNome": "Banco Teste",
                  "agencia": "%s",
                  "agenciaDigito": "1",
                  "numeroConta": "%s",
                  "contaDigito": "0",
                  "tipoConta": "CORRENTE",
                  "titular": "Titular Teste",
                  "documentoTitular": "12345678901",
                  "pixChave": "pix@teste.com",
                  "saldoInicial": %s,
                  "dataSaldoInicial": "2026-07-09",
                  "principal": %s
                }
                """.formatted(empresaId, bancoCodigo, agencia, numeroConta,
                saldoInicial.toPlainString(), principal);
    }

    private String payloadComDocumento(UUID empresaId, String bancoCodigo,
            String agencia, String numeroConta, String documento) {
        return """
                {
                  "empresaId": "%s",
                  "bancoCodigo": "%s",
                  "bancoNome": "Banco Teste",
                  "agencia": "%s",
                  "numeroConta": "%s",
                  "tipoConta": "CORRENTE",
                  "titular": "Titular Teste",
                  "documentoTitular": "%s",
                  "principal": false
                }
                """.formatted(empresaId, bancoCodigo, agencia, numeroConta, documento);
    }

    private Empresa prepararEmpresa() {
        String sufixo = sufixo();
        return empresaRepository.saveAndFlush(new Empresa("Empresa CB " + sufixo,
                "Empresa CB " + sufixo, documento(sufixo), null, null, null));
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@invplus.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("65432100000000" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
