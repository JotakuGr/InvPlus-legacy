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
 * Testes de integracao dos endpoints de Fornecedor.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FornecedorControllerTests {

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
     * Cobre criar, buscar, listar, atualizar, excluir e ocultar fornecedor excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Fornecedor Controller", "fornecedor-controller@teste.com");

        String criado = mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "7", "Fornecedor inicial")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pessoaId").value(pessoaId))
                .andExpect(jsonPath("$.nome").value("Fornecedor Controller"))
                .andExpect(jsonPath("$.prazoMedioEntrega").value(7))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/fornecedores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/fornecedores?page=0&size=20&sort=criadoEm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/api/v1/fornecedores/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "15", "Fornecedor atualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prazoMedioEntrega").value(15))
                .andExpect(jsonPath("$.observacao").value("Fornecedor atualizado"));

        mockMvc.perform(delete("/api/v1/fornecedores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/fornecedores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/fornecedores?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Bloqueia fornecedor duplicado para a mesma pessoa.
     */
    @Test
    void naoDeveCriarFornecedorDuplicadoParaMesmaPessoa() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Fornecedor Duplicado", "fornecedor-duplicado@teste.com");

        criarFornecedor(token, pessoaId);

        mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "10", "Duplicado")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Pessoa ja possui fornecedor ativo"));
    }

    /**
     * Pessoa inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaPessoaInexistente() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID().toString(), "10", "Sem pessoa")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pessoa nao encontrada"));
    }

    /**
     * Prazo medio negativo deve retornar 400.
     */
    @Test
    void deveRetornar400ParaPrazoMedioNegativo() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Fornecedor Prazo", "fornecedor-prazo@teste.com");

        mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "-1", "Prazo negativo")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/fornecedores"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de Fornecedor deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Fornecedor",
                "sem-fornecedor-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/fornecedores")
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

    private void criarFornecedor(String token, String pessoaId) throws Exception {
        mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, "10", "Base")))
                .andExpect(status().isCreated());
    }

    private String payload(String pessoaId, String prazoMedioEntrega, String observacao) {
        return """
                {
                  "pessoaId": "%s",
                  "condicaoPagamentoId": "%s",
                  "prazoMedioEntrega": %s,
                  "observacao": "%s"
                }
                """.formatted(pessoaId, UUID.randomUUID(), prazoMedioEntrega, observacao);
    }

    private String payloadPessoa(String nome, String email) {
        String cnpj = cnpjUnico();
        return """
                {
                  "tipoPessoa": "JURIDICA",
                  "nome": "%s",
                  "nomeFantasia": "Fornecedor Teste",
                  "cpfCnpj": "%s",
                  "rgIe": "1234567",
                  "telefone": "7133334444",
                  "celular": "71999998888",
                  "email": "%s",
                  "observacao": "Pessoa para fornecedor"
                }
                """.formatted(nome, cnpj, email);
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "13" + "%012d".formatted(parte);
    }
}
