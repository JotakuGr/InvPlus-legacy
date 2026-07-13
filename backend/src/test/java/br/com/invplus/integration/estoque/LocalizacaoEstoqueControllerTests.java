package br.com.invplus.integration.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.security.entity.Usuario;
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
 * Testes de integracao dos endpoints de LocalizacaoEstoque.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocalizacaoEstoqueControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private DepositoRepository depositoRepository;

    @Autowired
    private UsuarioTestFactory usuarioTestFactory;

    @Autowired
    private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;

    @Autowired
    private JwtTestFactory jwtTestFactory;

    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        Deposito deposito = criarDeposito();
        String token = token(deposito.getEmpresa(), java.util.Set.of("ESTOQUE:*"));

        String criado = mockMvc.perform(post("/api/v1/localizacoes-estoque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(deposito.getId(), "A-01", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.depositoId").value(deposito.getId().toString()))
                .andExpect(jsonPath("$.codigo").value("A-01"))
                .andExpect(jsonPath("$.principal").value(true))
                .andExpect(jsonPath("$.bloqueada").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/localizacoes-estoque/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/localizacoes-estoque?depositoId=" + deposito.getId()
                        + "&codigo=A&descricao=Rua&rua=Rua&corredor=03&prateleira=02&principal=true&bloqueada=false&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/localizacoes-estoque/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(deposito.getId(), "A-01", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(true));

        mockMvc.perform(delete("/api/v1/localizacoes-estoque/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/localizacoes-estoque/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/localizacoes-estoque?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(listagem).doesNotContain(id);
    }

    @Test
    void deveRetornar404ParaDepositoInexistente() throws Exception {
        Empresa empresa = criarEmpresa();
        String token = token(empresa, java.util.Set.of("ESTOQUE:*"));

        mockMvc.perform(post("/api/v1/localizacoes-estoque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID(), "A-404", false)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Deposito nao encontrado"));
    }

    @Test
    void naoDeveCriarCodigoDuplicadoPorDeposito() throws Exception {
        Deposito deposito = criarDeposito();
        String token = token(deposito.getEmpresa(), java.util.Set.of("ESTOQUE:*"));
        criarLocalizacao(token, deposito.getId(), "A-DUP", false);

        mockMvc.perform(post("/api/v1/localizacoes-estoque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(deposito.getId(), "A-DUP", false)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe localizacao ativa com este codigo para o deposito"));
    }

    @Test
    void naoDeveCriarSegundaPrincipalPorDeposito() throws Exception {
        Deposito deposito = criarDeposito();
        String token = token(deposito.getEmpresa(), java.util.Set.of("ESTOQUE:*"));
        criarLocalizacao(token, deposito.getId(), "A-PRI-1", true);

        mockMvc.perform(post("/api/v1/localizacoes-estoque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(deposito.getId(), "A-PRI-2", true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Deposito ja possui localizacao principal ativa"));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes-estoque")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        String token = token(criarEmpresa(), java.util.Set.of("SEGURANCA:*"));

        mockMvc.perform(get("/api/v1/localizacoes-estoque").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveIsolarLocalizacaoPorEmpresaDoToken() throws Exception {
        Deposito depositoA = criarDeposito();
        Deposito depositoB = criarDeposito();
        String tokenA = token(depositoA.getEmpresa(), java.util.Set.of("ESTOQUE:*"));
        String tokenB = token(depositoB.getEmpresa(), java.util.Set.of("ESTOQUE:*"));
        String localizacaoId = criarLocalizacao(tokenA, depositoA.getId(), "A-ISO", false);

        mockMvc.perform(get("/api/v1/localizacoes-estoque/" + localizacaoId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/localizacoes-estoque?page=0&size=50")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(listagem).doesNotContain(localizacaoId);
    }

    private String criarLocalizacao(String token, UUID depositoId, String codigo, Boolean principal) throws Exception {
        String response = mockMvc.perform(post("/api/v1/localizacoes-estoque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(depositoId, codigo, principal)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(UUID depositoId, String codigo, Boolean principal) {
        return """
                {
                  "depositoId": "%s",
                  "codigo": "%s",
                  "descricao": "Rua A prateleira 02",
                  "rua": "Rua A",
                  "corredor": "03",
                  "prateleira": "02",
                  "nivel": "04",
                  "posicao": "15",
                  "principal": %s,
                  "bloqueada": false,
                  "observacao": "Localizacao de teste"
                }
                """.formatted(depositoId, codigo, principal);
    }

    private Deposito criarDeposito() {
        Empresa empresa = criarEmpresa();

        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-" + codigoCurto(), "Deposito Localizacao", "Teste",
                TipoDeposito.CENTRAL, false, true, true, true, true, false);
        return depositoRepository.saveAndFlush(deposito);
    }

    private Empresa criarEmpresa() {
        Empresa empresa = new Empresa();
        empresa.atualizar("Empresa Localizacao " + UUID.randomUUID(), "Empresa Localizacao", cnpjUnico(),
                "ISENTO", null, "NORMAL", 3, "71999990000", "empresa-" + UUID.randomUUID() + "@teste.com");
        return empresaRepository.saveAndFlush(empresa);
    }

    private String token(Empresa empresa, java.util.Set<String> permissoes) {
        Usuario usuario = permissoes.contains("ESTOQUE:*")
                ? usuarioTestFactory.criarAtivoComPerfilAdmin()
                : usuarioTestFactory.criarAtivoSemPerfil();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        return jwtTestFactory.accessToken(usuario.getId(), usuario.getEmail(), empresa.getId(), null, permissoes);
    }

    private String cnpjUnico() {
        return "15" + "%012d".formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L));
    }

    private String codigoCurto() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }
}
