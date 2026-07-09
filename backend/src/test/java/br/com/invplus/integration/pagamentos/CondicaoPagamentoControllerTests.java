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
 * Testes de integracao dos endpoints de CondicaoPagamento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CondicaoPagamentoControllerTests {

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
    void deveCriarListarBuscarAtualizarEExcluirCondicaoPagamento() throws Exception {
        String token = login().get("accessToken").asString();
        String codigo = "30D-" + codigoCurto();

        String response = mockMvc.perform(post("/api/v1/condicoes-pagamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(codigo, parcelasJson(1, 30))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.parcelas[0].dias").value(30))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/condicoes-pagamento/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/condicoes-pagamento?codigo=" + codigo + "&page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value(codigo));

        mockMvc.perform(put("/api/v1/condicoes-pagamento/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(codigo, """
                                [
                                  {"numeroParcela":1,"dias":0,"percentual":40.00},
                                  {"numeroParcela":2,"dias":30,"percentual":30.00},
                                  {"numeroParcela":3,"dias":60,"percentual":30.00}
                                ]
                                """)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parcelas.length()").value(3));

        mockMvc.perform(post("/api/v1/condicoes-pagamento/" + id + "/simular")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataBase\":\"2026-07-10\",\"valorTotal\":1000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroParcela").value(1))
                .andExpect(jsonPath("$[0].valor").value(400.00));

        mockMvc.perform(delete("/api/v1/condicoes-pagamento/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveBloquearCodigoDuplicadoEPercentualNegativo() throws Exception {
        String token = login().get("accessToken").asString();
        String codigo = "DUP-" + codigoCurto();
        criar(token, codigo);

        mockMvc.perform(post("/api/v1/condicoes-pagamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(codigo, parcelasJson(1, 30))))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/condicoes-pagamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"NEG-%s","nome":"Negativa","jurosPercentual":0,"multaPercentual":0,"descontoPercentual":0,"regraVencimento":"PROXIMO_DIA_UTIL","consideraFeriados":true,"parcelas":[{"numeroParcela":1,"dias":0,"percentual":90.00}]}
                                """.formatted(codigoCurto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/condicoes-pagamento"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Condicao Pagamento",
                "sem-condicao-pagamento-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("PEDIDO_VENDA:*")
                .build());

        mockMvc.perform(get("/api/v1/condicoes-pagamento")
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
        mockMvc.perform(post("/api/v1/condicoes-pagamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(codigo, parcelasJson(1, 30))))
                .andExpect(status().isCreated());
    }

    private String payload(String codigo, String parcelas) {
        return """
                {"codigo":"%s","nome":"Condicao %s","jurosPercentual":0,"multaPercentual":0,"descontoPercentual":0,"regraVencimento":"PROXIMO_DIA_UTIL","consideraFeriados":true,"parcelas":%s}
                """.formatted(codigo, codigo, parcelas);
    }

    private String parcelasJson(int quantidade, int intervalo) {
        if (quantidade == 1) {
            return """
                    [{"numeroParcela":1,"dias":%d,"percentual":100.00}]
                    """.formatted(intervalo);
        }
        return """
                [
                  {"numeroParcela":1,"dias":0,"percentual":40.00},
                  {"numeroParcela":2,"dias":30,"percentual":30.00},
                  {"numeroParcela":3,"dias":60,"percentual":30.00}
                ]
                """;
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
