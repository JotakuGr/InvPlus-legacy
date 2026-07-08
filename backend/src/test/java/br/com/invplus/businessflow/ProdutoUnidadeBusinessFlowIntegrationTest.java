package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * Testa o fluxo real Produto -> ProdutoUnidade.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoUnidadeBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Prova que unidade alternativa conversa corretamente com Produto e UnidadeMedida.
     */
    @Test
    void deveExecutarFluxoProdutoUnidade() throws Exception {
        String token = login().get("accessToken").asString();
        String unidadeBaseId = criarUnidade(token, "N");
        ProdutoCriado produto = criarProduto(token, unidadeBaseId);
        String produtoUnidadeId = criarProdutoUnidade(token, produto.id(), unidadeBaseId);

        mockMvc.perform(get("/api/v1/produtos-unidades/" + produtoUnidadeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(produto.id()))
                .andExpect(jsonPath("$.produtoCodigo").value(produto.codigo()))
                .andExpect(jsonPath("$.unidadeMedidaId").value(unidadeBaseId))
                .andExpect(jsonPath("$.unidadePadrao").value(true));

        mockMvc.perform(get("/api/v1/produtos-unidades?produtoId=" + produto.id()
                        + "&unidadeMedidaId=" + unidadeBaseId
                        + "&unidadePadrao=true&permiteCompra=true&permiteVenda=true&permiteEstoque=true&permiteFracionamento=true&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(produtoUnidadeId));

        mockMvc.perform(get("/api/v1/produtos/" + produto.id() + "/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produto.id").value(produto.id()))
                .andExpect(jsonPath("$.unidadePadrao.id").value(produtoUnidadeId));
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@invplus.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String criarUnidade(String token, String prefixo) throws Exception {
        String sigla = prefixo + Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase().substring(0, 5);
        String response = mockMvc.perform(post("/api/v1/unidades-medida").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sigla\":\"%s\",\"descricao\":\"Unidade %s\",\"casasDecimais\":0}".formatted(sigla, sigla)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private ProdutoCriado criarProduto(String token, String unidadeId) throws Exception {
        String categoriaId = criarCategoria(token);
        String marcaId = criarMarca(token);
        String codigo = "PROD-FLUXO-UNID-" + Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
        String response = mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadProduto(categoriaId, marcaId, unidadeId, codigo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new ProdutoCriado(objectMapper.readTree(response).get("id").asString(), codigo);
    }

    private String criarCategoria(String token) throws Exception {
        String nome = "Categoria Fluxo Unidade " + Long.toString(Math.abs(System.nanoTime()), 36);
        String response = mockMvc.perform(post("/api/v1/categorias").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"descricao\":\"Categoria fluxo unidade\"}".formatted(nome)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String nome = "Marca Fluxo Unidade " + Long.toString(Math.abs(System.nanoTime()), 36);
        String response = mockMvc.perform(post("/api/v1/marcas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"descricao\":\"Marca fluxo unidade\"}".formatted(nome)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarProdutoUnidade(String token, String produtoId, String unidadeId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-unidades")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": "%s",
                                  "unidadeMedidaId": "%s",
                                  "fatorConversao": 1.000000,
                                  "unidadePadrao": true,
                                  "permiteCompra": true,
                                  "permiteVenda": true,
                                  "permiteEstoque": true,
                                  "permiteFracionamento": true,
                                  "observacao": "Unidade padrao do fluxo"
                                }
                                """.formatted(produtoId, unidadeId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payloadProduto(String categoriaId, String marcaId, String unidadeId, String codigo) {
        String barras = "794" + "%010d".formatted(Math.abs(System.nanoTime() % 10_000_000_000L));
        return """
                {
                  "categoriaId":"%s","marcaId":"%s","unidadeMedidaId":"%s","codigo":"%s","sku":"SKU-%s",
                  "codigoInterno":"INT-%s","codigoBarras":"%s","nome":"Produto Fluxo Unidade","descricao":"Produto fluxo unidade",
                  "tipoProduto":"PRODUTO","naturezaProduto":"AMBOS","statusProduto":"ATIVO",
                  "precoCusto":10.0000,"precoVenda":20.0000,"pesoLiquido":1.000,"pesoBruto":1.200,
                  "alturaCm":10.000,"larguraCm":20.000,"comprimentoCm":30.000,"estoqueMinimo":5.000,
                  "controlaLote":false,"controlaValidade":false,"permiteFracionamento":true,
                  "controlaEstoque":true,"permiteCompra":true,"permiteVenda":true
                }
                """.formatted(categoriaId, marcaId, unidadeId, codigo, codigo, codigo, barras);
    }

    private record ProdutoCriado(String id, String codigo) {
    }
}
