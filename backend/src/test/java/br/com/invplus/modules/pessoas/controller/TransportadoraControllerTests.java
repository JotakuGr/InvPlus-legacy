package br.com.invplus.modules.pessoas.controller;

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
 * Testes de integracao dos endpoints de Transportadora.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransportadoraControllerTests {

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
     * Cobre criar, buscar, listar, atualizar, excluir e ocultar transportadora excluida.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Transportadora Controller", "transportadora-controller@teste.com");

        String criado = mockMvc.perform(post("/api/v1/transportadoras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "1200.000", "Transportadora inicial")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pessoaId").value(pessoaId))
                .andExpect(jsonPath("$.nome").value("Transportadora Controller"))
                .andExpect(jsonPath("$.capacidadeCargaKg").value(1200.000))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/transportadoras/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/transportadoras?page=0&size=20&sort=criadoEm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/api/v1/transportadoras/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "2500.000", "Transportadora atualizada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidadeCargaKg").value(2500.000))
                .andExpect(jsonPath("$.observacao").value("Transportadora atualizada"));

        mockMvc.perform(delete("/api/v1/transportadoras/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/transportadoras/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/transportadoras?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Bloqueia transportadora duplicada para a mesma pessoa.
     */
    @Test
    void naoDeveCriarTransportadoraDuplicadaParaMesmaPessoa() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Transportadora Duplicada", "transportadora-duplicada@teste.com");

        criarTransportadora(token, pessoaId);

        mockMvc.perform(post("/api/v1/transportadoras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "1000.000", "Duplicada")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Pessoa ja possui transportadora ativa"));
    }

    /**
     * Pessoa inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaPessoaInexistente() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/transportadoras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID().toString(), "1000.000", "Sem pessoa")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pessoa nao encontrada"));
    }

    /**
     * Capacidade negativa deve retornar 400.
     */
    @Test
    void deveRetornar400ParaCapacidadeCargaNegativa() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Transportadora Capacidade", "transportadora-capacidade@teste.com");

        mockMvc.perform(post("/api/v1/transportadoras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "-1.000", "Capacidade negativa")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/transportadoras"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de Transportadora deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Transportadora",
                "sem-transportadora-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/transportadoras")
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

    private String criarPessoa(String token, String nome, String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadPessoa(nome, email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private void criarTransportadora(String token, String pessoaId) throws Exception {
        mockMvc.perform(post("/api/v1/transportadoras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "1000.000", "Base")))
                .andExpect(status().isCreated());
    }

    private String payload(String pessoaId, String capacidadeCargaKg, String observacao) {
        return """
                {
                  "pessoaId": "%s",
                  "antt": "ANTT123",
                  "tipoVeiculoPrincipal": "Caminhao Bau",
                  "capacidadeCargaKg": %s,
                  "observacao": "%s"
                }
                """.formatted(pessoaId, capacidadeCargaKg, observacao);
    }

    private String payloadPessoa(String nome, String email) {
        String cnpj = cnpjUnico();
        return """
                {
                  "tipoPessoa": "JURIDICA",
                  "nome": "%s",
                  "nomeFantasia": "Transportes Teste",
                  "cpfCnpj": "%s",
                  "rgIe": "ISENTO",
                  "telefone": "7133334444",
                  "celular": "71999998888",
                  "email": "%s",
                  "observacao": "Pessoa para transportadora"
                }
                """.formatted(nome, cnpj, email);
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "15" + "%012d".formatted(parte);
    }
}
