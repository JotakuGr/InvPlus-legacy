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
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.support.security.EmpresaTestFactory;
import br.com.invplus.support.security.JwtTestFactory;
import br.com.invplus.support.security.UsuarioEmpresaTestFactory;
import br.com.invplus.support.security.UsuarioTestFactory;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
    private EmpresaTestFactory empresaTestFactory;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private UsuarioTestFactory usuarioTestFactory;

    @Autowired
    private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;

    @Autowired
    private JwtTestFactory jwtTestFactory;

    /**
     * Cobre criar, buscar, listar com filtros, atualizar, excluir e ocultar registro excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        ContextoTeste contexto = criarContexto();
        String token = contexto.token();
        Empresa empresa = contexto.empresa();
        Filial filial = criarFilial(empresa);

        String criado = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(filial.getId(), "DEP-CTRL", true)))
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

        mockMvc.perform(get("/api/v1/depositos?filialId=" + filial.getId()
                        + "&codigo=DEP&nome=Central&tipoDeposito=CENTRAL&principal=true&controlaEstoque=true&bloqueado=false&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/depositos/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(filial.getId(), "DEP-CTRL", true)))
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

    /** Token operacional sem empresa deve ser rejeitado antes do controller. */
    @Test
    void deveRetornar401ParaTokenSemEmpresa() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        String token = jwtTestFactory.accessTokenSemEmpresa(
                usuario.getId(), usuario.getEmail(), java.util.Set.of("DEPOSITO:*"));

        mockMvc.perform(get("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Filial inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaFilialInexistente() throws Exception {
        ContextoTeste contexto = criarContexto();
        String token = contexto.token();

        mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID(), "DEP-FIL", false)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Filial nao encontrada"));
    }

    /**
     * Codigo deve ser unico por empresa.
     */
    @Test
    void naoDeveCriarCodigoDuplicadoPorEmpresa() throws Exception {
        ContextoTeste contexto = criarContexto();
        String token = contexto.token();
        criarDeposito(token, null, "DEP-DUP", false);

        mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null, "DEP-DUP", false)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe deposito ativo com este codigo para a empresa"));
    }

    /**
     * Empresa deve ter apenas um deposito principal ativo.
     */
    @Test
    void naoDeveCriarSegundoPrincipalPorEmpresa() throws Exception {
        ContextoTeste contexto = criarContexto();
        String token = contexto.token();
        criarDeposito(token, null, "DEP-PRI-1", true);

        mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null, "DEP-PRI-2", true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Empresa ja possui deposito principal ativo"));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/depositos")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoSemPerfil();
        Empresa empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("SEGURANCA:*"));

        mockMvc.perform(get("/api/v1/depositos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveIsolarDepositosPorEmpresaDoToken() throws Exception {
        ContextoTeste contextoA = criarContexto();
        ContextoTeste contextoB = criarContexto();
        Filial filialA = criarFilial(contextoA.empresa());
        String depositoId = criarDeposito(contextoA.token(), filialA.getId(), "DEP-ISO", false);

        mockMvc.perform(get("/api/v1/depositos/" + depositoId)
                        .header("Authorization", "Bearer " + contextoB.token()))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/depositos")
                        .header("Authorization", "Bearer " + contextoB.token()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(listagem).doesNotContain(depositoId);
    }

    private String criarDeposito(String token, UUID filialId, String codigo, Boolean principal) throws Exception {
        String response = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(filialId, codigo, principal)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(UUID filialId, String codigo, Boolean principal) {
        return """
                {
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
                """.formatted(filialId == null ? "null" : "\"" + filialId + "\"", codigo, principal);
    }

    private ContextoTeste criarContexto() {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("DEPOSITO:*"));
        return new ContextoTeste(token, empresa);
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

    private record ContextoTeste(String token, Empresa empresa) {
    }
}
