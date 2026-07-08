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
 * Testes de integracao dos endpoints de Funcionario.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FuncionarioControllerTests {

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
     * Cobre criar, buscar, listar, atualizar, excluir e ocultar funcionario excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Funcionario Controller", "funcionario-controller@teste.com");
        String usuarioId = criarUsuario();

        String criado = mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, usuarioId, "Analista", "5000.00", "2026-01-10", null, "Inicial")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pessoaId").value(pessoaId))
                .andExpect(jsonPath("$.usuarioId").value(usuarioId))
                .andExpect(jsonPath("$.nome").value("Funcionario Controller"))
                .andExpect(jsonPath("$.cargo").value("Analista"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/funcionarios/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/funcionarios?page=0&size=20&sort=criadoEm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/api/v1/funcionarios/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, null, "Coordenador", "7000.00", "2026-01-10", null, "Atualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.cargo").value("Coordenador"))
                .andExpect(jsonPath("$.observacao").value("Atualizado"));

        mockMvc.perform(delete("/api/v1/funcionarios/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/funcionarios/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/funcionarios?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Bloqueia funcionario duplicado para a mesma pessoa.
     */
    @Test
    void naoDeveCriarFuncionarioDuplicadoParaMesmaPessoa() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Funcionario Duplicado", "funcionario-duplicado@teste.com");

        criarFuncionario(token, pessoaId, null);

        mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, null, "Analista", "5000.00", "2026-01-10", null, "Duplicado")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Pessoa ja possui funcionario ativo"));
    }

    /**
     * Bloqueia usuario duplicado entre funcionarios ativos.
     */
    @Test
    void naoDeveVincularUsuarioDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        String usuarioId = criarUsuario();
        String pessoaId1 = criarPessoa(token, "Funcionario Usuario 1", "funcionario-usuario-1@teste.com");
        String pessoaId2 = criarPessoa(token, "Funcionario Usuario 2", "funcionario-usuario-2@teste.com");

        criarFuncionario(token, pessoaId1, usuarioId);

        mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId2, usuarioId, "Analista", "5000.00", "2026-01-10", null, "Duplicado")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Usuario ja vinculado a funcionario ativo"));
    }

    /**
     * Pessoa inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaPessoaInexistente() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID().toString(), null, "Analista", "5000.00", "2026-01-10", null, "Sem pessoa")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pessoa nao encontrada"));
    }

    /**
     * Salario negativo deve retornar 400.
     */
    @Test
    void deveRetornar400ParaSalarioNegativo() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Funcionario Salario", "funcionario-salario@teste.com");

        mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, null, "Analista", "-1.00", "2026-01-10", null, "Salario negativo")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Data de demissao anterior a admissao deve retornar 400.
     */
    @Test
    void deveRetornar400ParaDataDemissaoAnterior() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Funcionario Datas", "funcionario-datas@teste.com");

        mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, null, "Analista", "5000.00", "2026-01-10", "2026-01-09", "Datas")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/funcionarios"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de Funcionario deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Funcionario",
                "sem-funcionario-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/funcionarios")
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

    private String criarUsuario() {
        Usuario usuario = new Usuario(
                "Usuario Funcionario",
                "usuario-funcionario-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        return usuarioRepository.saveAndFlush(usuario).getId().toString();
    }

    private void criarFuncionario(String token, String pessoaId, String usuarioId) throws Exception {
        mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, usuarioId, "Analista", "5000.00", "2026-01-10", null, "Base")))
                .andExpect(status().isCreated());
    }

    private String payload(
            String pessoaId,
            String usuarioId,
            String cargo,
            String salario,
            String dataAdmissao,
            String dataDemissao,
            String observacao
    ) {
        String usuario = usuarioId == null ? "null" : "\"" + usuarioId + "\"";
        String demissao = dataDemissao == null ? "null" : "\"" + dataDemissao + "\"";
        return """
                {
                  "pessoaId": "%s",
                  "usuarioId": %s,
                  "cargo": "%s",
                  "salario": %s,
                  "dataAdmissao": "%s",
                  "dataDemissao": %s,
                  "observacao": "%s"
                }
                """.formatted(pessoaId, usuario, cargo, salario, dataAdmissao, demissao, observacao);
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
                  "observacao": "Pessoa para funcionario"
                }
                """.formatted(nome, cpf, email);
    }

    private String cpfUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000L);
        return "14" + "%09d".formatted(parte);
    }
}
