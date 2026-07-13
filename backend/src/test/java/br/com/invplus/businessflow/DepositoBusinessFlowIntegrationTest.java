package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
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

    @Autowired private UsuarioTestFactory usuarioTestFactory;
    @Autowired private EmpresaTestFactory empresaTestFactory;
    @Autowired private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;
    @Autowired private JwtTestFactory jwtTestFactory;

    /**
     * Cria Empresa, Filial e Deposito e valida filtros principais do estoque.
     */
    @Test
    void deveCriarDepositoAVinculadoAEmpresaEFilial() throws Exception {
        ContextoTeste contexto = criarContexto();
        String token = contexto.token();
        JsonNode filial = criarFilial(token);
        JsonNode deposito = criarDeposito(token, filial.get("id").asString());

        mockMvc.perform(get("/api/v1/depositos/" + deposito.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaId").value(contexto.empresaId().toString()))
                .andExpect(jsonPath("$.filialId").value(filial.get("id").asString()))
                .andExpect(jsonPath("$.principal").value(true))
                .andExpect(jsonPath("$.controlaEstoque").value(true))
                .andExpect(jsonPath("$.bloqueado").value(false));

        mockMvc.perform(get("/api/v1/depositos?filialId=" + filial.get("id").asString()
                        + "&tipoDeposito=LOJA&principal=true&controlaEstoque=true&bloqueado=false&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(deposito.get("id").asString()));
    }

    private ContextoTeste criarContexto() {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(usuario.getId(), usuario.getEmail(), empresa.getId(), null,
                java.util.Set.of("CADASTROS:*", "DEPOSITO:*", "ESTOQUE:*"));
        return new ContextoTeste(token, empresa.getId());
    }

    private JsonNode criarFilial(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/cadastros/filiais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "FIL-%s",
                                  "nome": "Filial Estoque",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "telefone": "71999991111",
                                  "email": "filial-fluxo-%s@teste.com",
                                  "matriz": false
                                }
                                """.formatted(codigoCurto(), cnpjUnico(), UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarDeposito(String token, String filialId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
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
                                """.formatted(filialId, codigoCurto())))
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

    private record ContextoTeste(String token, UUID empresaId) {
    }
}
