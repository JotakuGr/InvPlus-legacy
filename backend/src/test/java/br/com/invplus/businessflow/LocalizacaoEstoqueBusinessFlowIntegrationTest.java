package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.support.security.EmpresaTestFactory;
import br.com.invplus.support.security.JwtTestFactory;
import br.com.invplus.support.security.UsuarioEmpresaTestFactory;
import br.com.invplus.support.security.UsuarioTestFactory;
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
 * Fluxo real Empresa -> Deposito -> LocalizacaoEstoque.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocalizacaoEstoqueBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaTestFactory empresaTestFactory;

    @Autowired
    private UsuarioTestFactory usuarioTestFactory;

    @Autowired
    private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;

    @Autowired
    private JwtTestFactory jwtTestFactory;

    @Test
    void deveCriarLocalizacaoParaDeposito() throws Exception {
        Empresa empresa = empresaTestFactory.criarAtiva();
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("ESTOQUE:*"));
        JsonNode deposito = criarDeposito(token);
        JsonNode localizacao = criarLocalizacao(token, deposito.get("id").asString());

        mockMvc.perform(get("/api/v1/localizacoes-estoque/" + localizacao.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.depositoId").value(deposito.get("id").asString()))
                .andExpect(jsonPath("$.codigo").value(localizacao.get("codigo").asString()))
                .andExpect(jsonPath("$.principal").value(true));

        mockMvc.perform(get("/api/v1/localizacoes-estoque?depositoId=" + deposito.get("id").asString()
                        + "&rua=Rua&corredor=03&principal=true&bloqueada=false&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(localizacao.get("id").asString()));
    }

    private JsonNode criarDeposito(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "DEP-%s",
                                  "nome": "Deposito Localizacao",
                                  "descricao": "Deposito para localizacoes",
                                  "tipoDeposito": "CENTRAL",
                                  "principal": false,
                                  "permiteVenda": true,
                                  "permiteCompra": true,
                                  "permiteTransferencia": true,
                                  "controlaEstoque": true,
                                  "bloqueado": false
                                }
                                """.formatted(codigoCurto())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarLocalizacao(String token, String depositoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/localizacoes-estoque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "depositoId": "%s",
                                  "codigo": "LOC-%s",
                                  "descricao": "Rua A corredor 03",
                                  "rua": "Rua A",
                                  "corredor": "03",
                                  "prateleira": "02",
                                  "nivel": "04",
                                  "posicao": "15",
                                  "principal": true,
                                  "bloqueada": false,
                                  "observacao": "Fluxo integrado"
                                }
                                """.formatted(depositoId, codigoCurto())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String codigoCurto() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }
}
