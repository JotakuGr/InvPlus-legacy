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
 * Fluxo real Cadastros/Produtos/Estoque ate reserva, cancelamento e movimentacoes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaEstoqueBusinessFlowIntegrationTest {

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
    void deveReservarCancelarEBaixarNoFluxoCompleto() throws Exception {
        Empresa empresa = empresaTestFactory.criarAtiva();
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("ESTOQUE:*"));
        JsonNode deposito = criarDeposito(token);
        JsonNode localizacao = criarLocalizacao(token, deposito.get("id").asString());
        JsonNode categoria = criarCategoria(token);
        JsonNode unidade = criarUnidade(token);
        JsonNode produto = criarProduto(token, categoria.get("id").asString(), unidade.get("id").asString());
        JsonNode estoque = criarEstoque(token, produto.get("id").asString(),
                deposito.get("id").asString(), localizacao.get("id").asString());

        JsonNode reservaCancelada = criarReserva(token, estoque.get("id").asString(), "20.000");
        mockMvc.perform(get("/api/v1/estoques/" + estoque.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(100.000))
                .andExpect(jsonPath("$.quantidadeReservada").value(20.000))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(80.000));

        mockMvc.perform(post("/api/v1/reservas-estoque/" + reservaCancelada.get("id").asString() + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mockMvc.perform(get("/api/v1/estoques/" + estoque.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(100.000))
                .andExpect(jsonPath("$.quantidadeReservada").value(0.000))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(100.000));

        JsonNode reservaBaixada = criarReserva(token, estoque.get("id").asString(), "20.000");
        mockMvc.perform(post("/api/v1/reservas-estoque/" + reservaBaixada.get("id").asString() + "/baixar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BAIXADA"));

        mockMvc.perform(get("/api/v1/estoques/" + estoque.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(80.000))
                .andExpect(jsonPath("$.quantidadeReservada").value(0.000))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(80.000));

        mockMvc.perform(get("/api/v1/reservas-estoque?estoqueId=" + estoque.get("id").asString()
                        + "&status=BAIXADA&origem=VENDA&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(reservaBaixada.get("id").asString()));

        mockMvc.perform(get("/api/v1/movimentacoes-estoque?estoqueId=" + estoque.get("id").asString()
                        + "&origem=VENDA&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4));
    }

    private JsonNode criarDeposito(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "DEP-%s",
                                  "nome": "Deposito Reserva",
                                  "descricao": "Deposito para reserva",
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
                                  "principal": false,
                                  "bloqueada": false,
                                  "observacao": "Fluxo reserva"
                                }
                                """.formatted(depositoId, codigoCurto())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarCategoria(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Categoria Reserva %s",
                                  "descricao": "Categoria fluxo reserva",
                                  "ordem": 0
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarUnidade(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/unidades-medida")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sigla": "R%s",
                                  "descricao": "Unidade fluxo reserva",
                                  "casasDecimais": 0
                                }
                                """.formatted(codigoCurto().substring(0, 6))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarProduto(String token, String categoriaId, String unidadeId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": "%s",
                                  "unidadeMedidaId": "%s",
                                  "codigo": "PROD-%s",
                                  "nome": "Produto Reserva Fluxo",
                                  "descricao": "Produto para reserva",
                                  "tipoProduto": "PRODUTO",
                                  "naturezaProduto": "AMBOS",
                                  "statusProduto": "ATIVO",
                                  "precoCusto": 0,
                                  "precoVenda": 0,
                                  "estoqueMinimo": 0,
                                  "controlaEstoque": true,
                                  "permiteCompra": true,
                                  "permiteVenda": true
                                }
                                """.formatted(categoriaId, unidadeId, codigoCurto())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarEstoque(String token, String produtoId, String depositoId, String localizacaoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/estoques")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": "%s",
                                  "depositoId": "%s",
                                  "localizacaoId": "%s",
                                  "quantidade": 100.000,
                                  "quantidadeReservada": 0.000,
                                  "quantidadeAvariada": 0.000,
                                  "quantidadeTransito": 0.000,
                                  "estoqueMinimo": 2.000,
                                  "estoqueMaximo": 150.000
                                }
                                """.formatted(produtoId, depositoId, localizacaoId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarReserva(String token, String estoqueId, String quantidade) throws Exception {
        String response = mockMvc.perform(post("/api/v1/reservas-estoque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estoqueId": "%s",
                                  "quantidade": %s,
                                  "origem": "VENDA",
                                  "origemId": "%s",
                                  "observacao": "Reserva fluxo"
                                }
                                """.formatted(estoqueId, quantidade, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
