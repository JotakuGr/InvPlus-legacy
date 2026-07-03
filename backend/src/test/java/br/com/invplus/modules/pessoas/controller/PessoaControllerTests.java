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
 * Testes de integracao dos endpoints de Pessoa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PessoaControllerTests {

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
     * Cobre criar pessoa fisica, buscar, listar, atualizar e excluir logicamente.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String cpf = cpfUnico();

        String criado = mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadFisica(cpf, "Pessoa Controller", "pessoa-controller@teste.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoPessoa").value("FISICA"))
                .andExpect(jsonPath("$.nome").value("Pessoa Controller"))
                .andExpect(jsonPath("$.cpfCnpj").value(cpf))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/pessoas/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/pessoas?page=0&size=20&sort=nome")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/api/v1/pessoas/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadFisica(cpf, "Pessoa Atualizada", "pessoa-atualizada@teste.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pessoa Atualizada"))
                .andExpect(jsonPath("$.email").value("pessoa-atualizada@teste.com"));

        mockMvc.perform(delete("/api/v1/pessoas/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pessoas/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/pessoas?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Garante criacao de pessoa juridica.
     */
    @Test
    void deveCriarPessoaJuridicaComSucesso() throws Exception {
        String token = login().get("accessToken").asString();
        String cnpj = cnpjUnico();

        mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJuridica(cnpj, "Empresa Controller LTDA", "empresa-controller@teste.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoPessoa").value("JURIDICA"))
                .andExpect(jsonPath("$.cpfCnpj").value(cnpj));
    }

    /**
     * Bloqueia CPF/CNPJ duplicado entre pessoas ativas.
     */
    @Test
    void naoDeveCriarCpfCnpjDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        String cpf = cpfUnico();

        criarPessoaFisica(token, cpf);

        mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadFisica(cpf, "Pessoa Duplicada", "duplicada@teste.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe pessoa ativa com este CPF/CNPJ"));
    }

    /**
     * Pessoa fisica com documento de CNPJ deve retornar 400.
     */
    @Test
    void deveRetornar400ParaCpfInvalidoDePessoaFisica() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadFisica(cnpjUnico(), "Pessoa CPF Invalido", "cpf-invalido@teste.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF invalido para pessoa fisica"));
    }

    /**
     * Pessoa juridica com documento de CPF deve retornar 400.
     */
    @Test
    void deveRetornar400ParaCnpjInvalidoDePessoaJuridica() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJuridica(cpfUnico(), "Empresa CNPJ Invalido", "cnpj-invalido@teste.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CNPJ invalido para pessoa juridica"));
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/pessoas"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de Pessoa deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Pessoa",
                "sem-pessoa-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/pessoas")
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

    private void criarPessoaFisica(String token, String cpf) throws Exception {
        mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadFisica(cpf, "Pessoa Base", "base" + cpf + "@teste.com")))
                .andExpect(status().isCreated());
    }

    private String payloadFisica(String cpf, String nome, String email) {
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
                  "observacao": "Pessoa fisica de teste"
                }
                """.formatted(nome, cpf, email);
    }

    private String payloadJuridica(String cnpj, String nome, String email) {
        return """
                {
                  "tipoPessoa": "JURIDICA",
                  "nome": "%s",
                  "nomeFantasia": "Fantasia Teste",
                  "cpfCnpj": "%s",
                  "rgIe": "ISENTO",
                  "telefone": "7133334444",
                  "celular": "71999998888",
                  "email": "%s",
                  "observacao": "Pessoa juridica de teste"
                }
                """.formatted(nome, cnpj, email);
    }

    private String cpfUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000L);
        return "12" + "%09d".formatted(parte);
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "88" + "%012d".formatted(parte);
    }
}
