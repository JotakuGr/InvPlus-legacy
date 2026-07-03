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
 * Testes de integracao dos endpoints de Categoria.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoriaControllerTests {

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
        String nomePai = nomeUnico("Categoria Pai");
        String nomeFilha = nomeUnico("Categoria Filha");
        String paiId = criarCategoria(token, nomePai, null);

        String criado = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Categoria com pai",
                                  "categoriaPaiId": "%s"
                                }
                                """.formatted(nomeFilha, paiId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nomeFilha))
                .andExpect(jsonPath("$.categoriaPaiId").value(paiId))
                .andExpect(jsonPath("$.categoriaPaiNome").value(nomePai))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/categorias/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/categorias?page=0&size=20&sort=nome")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/api/v1/categorias/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s Atualizada",
                                  "descricao": "Categoria atualizada",
                                  "categoriaPaiId": "%s"
                                }
                                """.formatted(nomeFilha, paiId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nomeFilha + " Atualizada"))
                .andExpect(jsonPath("$.categoriaPaiId").value(paiId));

        mockMvc.perform(delete("/api/v1/categorias/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/categorias/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/categorias?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Garante unicidade de nome entre categorias ativas.
     */
    @Test
    void naoDeveCriarNomeDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        String nome = nomeUnico("Categoria Duplicada");

        criarCategoria(token, nome, null);

        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Duplicada"
                                }
                                """.formatted(nome)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe categoria ativa com este nome"));
    }

    /**
     * Bloqueia auto-relacao na hierarquia.
     */
    @Test
    void naoDevePermitirCategoriaPaiDelaMesma() throws Exception {
        String token = login().get("accessToken").asString();
        String nome = nomeUnico("Categoria Auto Pai");
        String id = criarCategoria(token, nome, null);

        mockMvc.perform(put("/api/v1/categorias/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "categoriaPaiId": "%s"
                                }
                                """.formatted(nome, id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Categoria nao pode ser pai dela mesma"));
    }

    /**
     * Bloqueia ciclos entre categorias pai e filhas.
     */
    @Test
    void naoDevePermitirCicloHierarquico() throws Exception {
        String token = login().get("accessToken").asString();
        String paiNome = nomeUnico("Categoria Ciclo Pai");
        String filhaNome = nomeUnico("Categoria Ciclo Filha");
        String paiId = criarCategoria(token, paiNome, null);
        String filhaId = criarCategoria(token, filhaNome, paiId);

        mockMvc.perform(put("/api/v1/categorias/" + paiId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "categoriaPaiId": "%s"
                                }
                                """.formatted(paiNome, filhaId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Categoria nao pode gerar ciclo hierarquico"));
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de Categoria deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Categoria",
                "sem-categoria-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/categorias")
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

    private String criarCategoria(String token, String nome, String categoriaPaiId) throws Exception {
        String campoPai = categoriaPaiId == null ? "" : "\"categoriaPaiId\": \"" + categoriaPaiId + "\",";

        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  %s
                                  "nome": "%s",
                                  "descricao": "Categoria %s"
                                }
                                """.formatted(campoPai, nome, nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private String nomeUnico(String prefixo) {
        return prefixo + " " + Long.toString(Math.abs(System.nanoTime()), 36);
    }
}
