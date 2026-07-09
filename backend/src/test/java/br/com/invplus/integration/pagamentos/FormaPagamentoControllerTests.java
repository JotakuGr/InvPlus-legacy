package br.com.invplus.integration.pagamentos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * Testes de integracao dos endpoints de FormaPagamento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FormaPagamentoControllerTests {

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

    @Test
    void deveCriarListarBuscarAtualizarEExcluirFormaPagamento() throws Exception {
        String token = login().get("accessToken").asString();
        String codigo = "PIX-" + codigoCurto();

        String response = mockMvc.perform(post("/api/v1/formas-pagamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"%s","nome":"Pix teste","tipo":"PIX","permiteTroco":false,"permiteParcelamento":false}
                                """.formatted(codigo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.tipo").value("PIX"))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/formas-pagamento/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/formas-pagamento?codigo=" + codigo + "&tipo=PIX&page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value(codigo));

        mockMvc.perform(put("/api/v1/formas-pagamento/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"%s","nome":"Pix atualizado","tipo":"PIX","permiteTroco":false,"permiteParcelamento":false}
                                """.formatted(codigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pix atualizado"));

        mockMvc.perform(delete("/api/v1/formas-pagamento/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveBloquearCodigoDuplicadoERegraDeTroco() throws Exception {
        String token = login().get("accessToken").asString();
        String codigo = "DUP-" + codigoCurto();

        criar(token, codigo);

        mockMvc.perform(post("/api/v1/formas-pagamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"%s","nome":"Duplicada","tipo":"PIX","permiteTroco":false,"permiteParcelamento":false}
                                """.formatted(codigo)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/formas-pagamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"TRC-%s","nome":"Pix troco","tipo":"PIX","permiteTroco":true,"permiteParcelamento":false}
                                """.formatted(codigoCurto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/formas-pagamento"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Forma Pagamento",
                "sem-forma-pagamento-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("PEDIDO_VENDA:*")
                .build());

        mockMvc.perform(get("/api/v1/formas-pagamento")
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

    private void criar(String token, String codigo) throws Exception {
        mockMvc.perform(post("/api/v1/formas-pagamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"%s","nome":"Pix teste","tipo":"PIX","permiteTroco":false,"permiteParcelamento":false}
                                """.formatted(codigo)))
                .andExpect(status().isCreated());
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
