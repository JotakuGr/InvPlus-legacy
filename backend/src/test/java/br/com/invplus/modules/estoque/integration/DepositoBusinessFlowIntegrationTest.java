package br.com.invplus.modules.estoque.integration;

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
 * Fluxo real entre Cadastros e Estoque para validar Deposito.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DepositoBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Cria Empresa, Filial e Deposito e valida filtros principais do estoque.
     */
    @Test
    void deveCriarDepositoAVinculadoAEmpresaEFilial() throws Exception {
        String token = login().get("accessToken").asString();
        JsonNode empresa = criarEmpresa(token);
        JsonNode filial = criarFilial(token, empresa.get("id").asString());
        JsonNode deposito = criarDeposito(token, empresa.get("id").asString(), filial.get("id").asString());

        mockMvc.perform(get("/api/v1/depositos/" + deposito.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaId").value(empresa.get("id").asString()))
                .andExpect(jsonPath("$.filialId").value(filial.get("id").asString()))
                .andExpect(jsonPath("$.principal").value(true))
                .andExpect(jsonPath("$.controlaEstoque").value(true))
                .andExpect(jsonPath("$.bloqueado").value(false));

        mockMvc.perform(get("/api/v1/depositos?empresaId=" + empresa.get("id").asString()
                        + "&filialId=" + filial.get("id").asString()
                        + "&tipoDeposito=LOJA&principal=true&controlaEstoque=true&bloqueado=false&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(deposito.get("id").asString()));
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
                                  "razaoSocial": "Empresa Estoque Fluxo %s",
                                  "nomeFantasia": "Empresa Estoque",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "inscricaoMunicipal": "123",
                                  "regimeTributario": "NORMAL",
                                  "crt": 3,
                                  "telefone": "71999990000",
                                  "email": "empresa-fluxo-%s@teste.com"
                                }
                                """.formatted(UUID.randomUUID(), cnpjUnico(), UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarFilial(String token, String empresaId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/cadastros/filiais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empresaId": "%s",
                                  "codigo": "FIL-%s",
                                  "nome": "Filial Estoque",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "telefone": "71999991111",
                                  "email": "filial-fluxo-%s@teste.com",
                                  "matriz": false
                                }
                                """.formatted(empresaId, codigoCurto(), cnpjUnico(), UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarDeposito(String token, String empresaId, String filialId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empresaId": "%s",
                                  "filialId": "%s",
                                  "codigo": "DEP-%s",
                                  "nome": "Loja principal",
                                  "descricao": "Deposito da loja principal",
                                  "tipoDeposito": "LOJA",
                                  "principal": true,
                                  "permiteVenda": true,
                                  "permiteCompra": true,
                                  "permiteTransferencia": true,
                                  "controlaEstoque": true,
                                  "bloqueado": false
                                }
                                """.formatted(empresaId, filialId, codigoCurto())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String cnpjUnico() {
        return "14" + "%012d".formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L));
    }

    private String codigoCurto() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }
}
