package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void deveCriarLocalizacaoParaDeposito() throws Exception {
        String token = login().get("accessToken").asString();
        JsonNode empresa = criarEmpresa(token);
        JsonNode deposito = criarDeposito(token, empresa.get("id").asString());
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

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@invplus.com", "senha": "admin123"}
                                """))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarEmpresa(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "razaoSocial": "Empresa Localizacao Fluxo %s",
                                  "nomeFantasia": "Empresa Localizacao",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "inscricaoMunicipal": "123",
                                  "regimeTributario": "NORMAL",
                                  "crt": 3,
                                  "telefone": "71999990000",
                                  "email": "empresa-localizacao-%s@teste.com"
                                }
                                """.formatted(UUID.randomUUID(), cnpjUnico(), UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarDeposito(String token, String empresaId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empresaId": "%s",
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
                                """.formatted(empresaId, codigoCurto())))
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

    private String cnpjUnico() {
        return "16" + "%012d".formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L));
    }

    private String codigoCurto() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }
}
