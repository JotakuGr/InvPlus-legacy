package br.com.invplus.modules.cadastros.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
 * Testes de integracao dos endpoints de Empresa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmpresaControllerTests {

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

    /**
     * Cobre criar, buscar, listar paginado, atualizar e excluir logicamente.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String cnpj = cnpjUnico();

        String criado = mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(cnpj, "Empresa Controller LTDA", "empresa-controller@teste.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Controller LTDA"))
                .andExpect(jsonPath("$.cnpj").value(cnpj))
                .andExpect(jsonPath("$.crt").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/empresas/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/empresas?page=0&size=20&sort=razaoSocial")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/api/v1/empresas/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(cnpj, "Empresa Atualizada LTDA", "empresa-atualizada@teste.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Atualizada LTDA"))
                .andExpect(jsonPath("$.email").value("empresa-atualizada@teste.com"));

        mockMvc.perform(delete("/api/v1/empresas/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/empresas/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/empresas?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Garante unicidade de CNPJ entre empresas ativas.
     */
    @Test
    void naoDeveCriarCnpjDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        String cnpj = cnpjUnico();

        criarEmpresa(token, cnpj);

        mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(cnpj, "Empresa Duplicada LTDA", "duplicada@teste.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe empresa ativa com este CNPJ"));
    }

    /**
     * CNPJ invalido deve retornar 400.
     */
    @Test
    void deveRetornar400ParaCnpjInvalido() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("123", "Empresa CNPJ Invalido LTDA", "cnpj-invalido@teste.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CNPJ invalido"));
    }

    /**
     * Email invalido deve retornar 400 pelo Bean Validation.
     */
    @Test
    void deveRetornar400ParaEmailInvalido() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(cnpjUnico(), "Empresa Email Invalido LTDA", "email-invalido")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/empresas"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de Empresa deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Empresa",
                "sem-empresa-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/empresas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@invplus.com",
                                  "senha": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private void criarEmpresa(String token, String cnpj) throws Exception {
        mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(cnpj, "Empresa Base LTDA", "base" + cnpj + "@teste.com")))
                .andExpect(status().isCreated());
    }

    private String payload(String cnpj, String razaoSocial, String email) {
        return """
                {
                  "razaoSocial": "%s",
                  "nomeFantasia": "Empresa Teste",
                  "cnpj": "%s",
                  "inscricaoEstadual": "ISENTO",
                  "inscricaoMunicipal": "12345",
                  "regimeTributario": "SIMPLES_NACIONAL",
                  "crt": 1,
                  "telefone": "71999990000",
                  "email": "%s"
                }
                """.formatted(razaoSocial, cnpj, email);
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "88" + "%012d".formatted(parte);
    }
}
