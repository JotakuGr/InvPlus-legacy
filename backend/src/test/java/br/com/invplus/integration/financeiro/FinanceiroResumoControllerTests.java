package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import br.com.invplus.support.security.EmpresaTestFactory;
import br.com.invplus.support.security.JwtTestFactory;
import br.com.invplus.support.security.UsuarioEmpresaTestFactory;
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
 * Testes do contrato REST e das permissoes dos relatorios financeiros.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinanceiroResumoControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private EmpresaTestFactory empresaTestFactory;
    @Autowired
    private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;
    @Autowired
    private JwtTestFactory jwtTestFactory;

    @Test
    void deveConsultarFluxoCaixaEDashboard() throws Exception {
        String token = login().get("accessToken").asString();
        String query = "?dataInicio=2026-07-01&dataFim=2026-07-31";

        mockMvc.perform(get("/api/v1/financeiro/relatorios/fluxo-caixa" + query)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEntradas").isNumber())
                .andExpect(jsonPath("$.totalSaidas").isNumber())
                .andExpect(jsonPath("$.saldo").isNumber());

        mockMvc.perform(get("/api/v1/financeiro/relatorios/dashboard" + query)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contasReceberAberto").isNumber())
                .andExpect(jsonPath("$.contasPagarAberto").isNumber())
                .andExpect(jsonPath("$.contasReceberVencidas").isNumber());

        mockMvc.perform(get("/api/v1/financeiro/relatorios/dre" + query)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receitaBruta").isNumber())
                .andExpect(jsonPath("$.despesasOperacionais").isNumber())
                .andExpect(jsonPath("$.resultadoOperacional").isNumber())
                .andExpect(jsonPath("$.receitas").isArray())
                .andExpect(jsonPath("$.despesas").isArray());
    }

    @Test
    void deveRetornar400ParaPeriodoInvalido() throws Exception {
        String token = login().get("accessToken").asString();
        mockMvc.perform(get("/api/v1/financeiro/relatorios/fluxo-caixa")
                        .queryParam("dataInicio", "2026-07-31")
                        .queryParam("dataFim", "2026-07-01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/financeiro/relatorios/dashboard")
                        .queryParam("dataInicio", "2026-07-01")
                        .queryParam("dataFim", "2026-07-31"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/financeiro/relatorios/dre")
                        .queryParam("dataInicio", "2026-07-01")
                        .queryParam("dataFim", "2026-07-31"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Relatorio", "sem-relatorio-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuario = usuarioRepository.saveAndFlush(usuario);
        var empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null,
                java.util.Set.of("PAGAMENTO:VISUALIZAR"));

        mockMvc.perform(get("/api/v1/financeiro/relatorios/dashboard")
                        .queryParam("dataInicio", "2026-07-01")
                        .queryParam("dataFim", "2026-07-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/financeiro/relatorios/dre")
                        .queryParam("dataInicio", "2026-07-01")
                        .queryParam("dataFim", "2026-07-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@invplus.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }
}
