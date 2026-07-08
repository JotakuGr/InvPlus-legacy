package br.com.invplus.integration.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
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
 * Testes de integracao dos endpoints de Deposito.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DepositoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Cobre criar, buscar, listar com filtros, atualizar, excluir e ocultar registro excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        Empresa empresa = criarEmpresa();
        Filial filial = criarFilial(empresa);

        String criado = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), filial.getId(), "DEP-CTRL", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empresaId").value(empresa.getId().toString()))
                .andExpect(jsonPath("$.filialId").value(filial.getId().toString()))
                .andExpect(jsonPath("$.codigo").value("DEP-CTRL"))
                .andExpect(jsonPath("$.controlaEstoque").value(true))
                .andExpect(jsonPath("$.bloqueado").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/depositos/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/depositos?empresaId=" + empresa.getId()
                        + "&filialId=" + filial.getId()
                        + "&codigo=DEP&nome=Central&tipoDeposito=CENTRAL&principal=true&controlaEstoque=true&bloqueado=false&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/depositos/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), filial.getId(), "DEP-CTRL", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(true));

        mockMvc.perform(delete("/api/v1/depositos/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/depositos/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/depositos?page=0&size=50").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Empresa inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaEmpresaInexistente() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID(), null, "DEP-404", false)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Empresa nao encontrada"));
    }

    /**
     * Filial inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaFilialInexistente() throws Exception {
        String token = login().get("accessToken").asString();
        Empresa empresa = criarEmpresa();

        mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), UUID.randomUUID(), "DEP-FIL", false)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Filial nao encontrada"));
    }

    /**
     * Codigo deve ser unico por empresa.
     */
    @Test
    void naoDeveCriarCodigoDuplicadoPorEmpresa() throws Exception {
        String token = login().get("accessToken").asString();
        Empresa empresa = criarEmpresa();
        criarDeposito(token, empresa.getId(), null, "DEP-DUP", false);

        mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), null, "DEP-DUP", false)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe deposito ativo com este codigo para a empresa"));
    }

    /**
     * Empresa deve ter apenas um deposito principal ativo.
     */
    @Test
    void naoDeveCriarSegundoPrincipalPorEmpresa() throws Exception {
        String token = login().get("accessToken").asString();
        Empresa empresa = criarEmpresa();
        criarDeposito(token, empresa.getId(), null, "DEP-PRI-1", true);

        mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), null, "DEP-PRI-2", true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Empresa ja possui deposito principal ativo"));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/depositos")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Deposito",
                "sem-deposito-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash()).authorities("SEGURANCA:*").build());

        mockMvc.perform(get("/api/v1/depositos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@invplus.com", "senha": "admin123"}
                                """))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String criarDeposito(String token, UUID empresaId, UUID filialId, String codigo, Boolean principal) throws Exception {
        String response = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresaId, filialId, codigo, principal)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(UUID empresaId, UUID filialId, String codigo, Boolean principal) {
        return """
                {
                  "empresaId": "%s",
                  "filialId": %s,
                  "codigo": "%s",
                  "nome": "Deposito Central",
                  "descricao": "Deposito de teste",
                  "tipoDeposito": "CENTRAL",
                  "principal": %s,
                  "permiteVenda": true,
                  "permiteCompra": true,
                  "permiteTransferencia": true,
                  "controlaEstoque": true,
                  "bloqueado": false
                }
                """.formatted(empresaId, filialId == null ? "null" : "\"" + filialId + "\"", codigo, principal);
    }

    private Empresa criarEmpresa() {
        Empresa empresa = new Empresa();
        empresa.atualizar("Empresa Estoque " + UUID.randomUUID(), "Empresa Estoque", cnpjUnico(),
                "ISENTO", null, "NORMAL", 3, "71999990000", "empresa-" + UUID.randomUUID() + "@teste.com");
        return empresaRepository.saveAndFlush(empresa);
    }

    private Filial criarFilial(Empresa empresa) {
        Filial filial = new Filial();
        filial.atualizar(empresa, null, "FIL-" + codigoCurto(), "Filial Estoque", cnpjUnico(),
                "ISENTO", "71999991111", "filial-" + UUID.randomUUID() + "@teste.com", false);
        return filialRepository.saveAndFlush(filial);
    }

    private String cnpjUnico() {
        return "13" + "%012d".formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L));
    }

    private String codigoCurto() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }
}
