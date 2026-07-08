package br.com.invplus.integration.pessoas;

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
 * Testes de integracao dos endpoints de Cliente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClienteControllerTests {

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
     * Cobre criar, buscar, listar, atualizar, excluir e ocultar cliente excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Cliente Controller", "cliente-controller@teste.com");

        String criado = mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "1500.00", "Cliente inicial")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pessoaId").value(pessoaId))
                .andExpect(jsonPath("$.nome").value("Cliente Controller"))
                .andExpect(jsonPath("$.limiteCredito").value(1500.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/clientes/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/clientes?page=0&size=20&sort=criadoEm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/api/v1/clientes/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "2500.00", "Cliente atualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limiteCredito").value(2500.00))
                .andExpect(jsonPath("$.observacao").value("Cliente atualizado"));

        mockMvc.perform(delete("/api/v1/clientes/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/clientes/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/clientes?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Bloqueia cliente duplicado para a mesma pessoa.
     */
    @Test
    void naoDeveCriarClienteDuplicadoParaMesmaPessoa() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Cliente Duplicado", "cliente-duplicado@teste.com");

        criarCliente(token, pessoaId);

        mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "1000.00", "Duplicado")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Pessoa ja possui cliente ativo"));
    }

    /**
     * Pessoa inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaPessoaInexistente() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID().toString(), "1000.00", "Sem pessoa")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pessoa nao encontrada"));
    }

    /**
     * Limite negativo deve retornar 400.
     */
    @Test
    void deveRetornar400ParaLimiteCreditoNegativo() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Cliente Limite", "cliente-limite@teste.com");

        mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "-1.00", "Limite negativo")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de Cliente deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Cliente",
                "sem-cliente-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/clientes")
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

    private void criarCliente(String token, String pessoaId) throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "1000.00", "Base")))
                .andExpect(status().isCreated());
    }

    private String payload(String pessoaId, String limiteCredito, String observacao) {
        return """
                {
                  "pessoaId": "%s",
                  "limiteCredito": %s,
                  "condicaoPagamentoId": "%s",
                  "tabelaPrecoId": "%s",
                  "observacao": "%s"
                }
                """.formatted(pessoaId, limiteCredito, UUID.randomUUID(), UUID.randomUUID(), observacao);
    }

    private String payloadPessoa(String nome, String email) {
        String cpf = cpfUnico();
        return """
                {
                  "tipoPessoa": "FISICA",
                  "nome": "%s",
                  "nomeFantasia": null,
                  "cpfCnpj": "%s",
                  "rgIe": "1234567",
                  "telefone": "7133334444",
                  "celular": "71999998888",
                  "email": "%s",
                  "observacao": "Pessoa para cliente"
                }
                """.formatted(nome, cpf, email);
    }

    private String cpfUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000L);
        return "13" + "%09d".formatted(parte);
    }
}
