package br.com.invplus.modules.produtos.integration;

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
 * Testa o fluxo real Categoria/Marca/Unidade -> Produto -> ProdutoCodigoBarras.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoCodigoBarrasBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Prova que ProdutoCodigoBarras conversa corretamente com Produto e seus cadastros base.
     */
    @Test
    void deveExecutarFluxoProdutoCodigoBarras() throws Exception {
        String token = login().get("accessToken").asString();
        ReferenciasProduto referencias = criarReferenciasProduto(token);
        ProdutoCriado produto = criarProduto(token, referencias);
        CodigoBarrasCriado codigoBarras = criarCodigoBarras(token, produto.id());

        mockMvc.perform(get("/api/v1/produtos/" + produto.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produto.id()))
                .andExpect(jsonPath("$.codigo").value(produto.codigo()));

        mockMvc.perform(get("/api/v1/produtos-codigos-barras/" + codigoBarras.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(codigoBarras.id()))
                .andExpect(jsonPath("$.produtoId").value(produto.id()))
                .andExpect(jsonPath("$.produtoCodigo").value(produto.codigo()))
                .andExpect(jsonPath("$.codigo").value(codigoBarras.codigo()))
                .andExpect(jsonPath("$.tipoCodigo").value("EAN_UNIDADE"))
                .andExpect(jsonPath("$.principal").value(true));

        mockMvc.perform(get("/api/v1/produtos-codigos-barras?produtoId=" + produto.id()
                        + "&tipoCodigo=EAN_UNIDADE"
                        + "&codigo=" + codigoBarras.codigo()
                        + "&principal=true&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(codigoBarras.id()));
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

    private ReferenciasProduto criarReferenciasProduto(String token) throws Exception {
        return new ReferenciasProduto(criarCategoria(token), criarMarca(token), criarUnidade(token));
    }

    private String criarCategoria(String token) throws Exception {
        String nome = nomeUnico("Categoria Fluxo Codigo");
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Categoria criada pelo fluxo de codigo de barras"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String nome = nomeUnico("Marca Fluxo Codigo");
        String response = mockMvc.perform(post("/api/v1/marcas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Marca criada pelo fluxo de codigo de barras"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarUnidade(String token) throws Exception {
        String sigla = siglaUnica();
        String response = mockMvc.perform(post("/api/v1/unidades-medida")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sigla": "%s",
                                  "descricao": "Unidade %s",
                                  "casasDecimais": 0
                                }
                                """.formatted(sigla, sigla)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private ProdutoCriado criarProduto(String token, ReferenciasProduto referencias) throws Exception {
        String codigo = codigoUnico("PROD-FLUXO-COD");
        String sku = codigoUnico("SKU-FLUXO-COD");
        String barras = codigoBarrasUnico("789");
        String response = mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": "%s",
                                  "marcaId": "%s",
                                  "unidadeMedidaId": "%s",
                                  "codigo": "%s",
                                  "sku": "%s",
                                  "codigoInterno": "INT-%s",
                                  "codigoBarras": "%s",
                                  "nome": "Produto Fluxo Codigo",
                                  "descricao": "Produto criado pelo fluxo de codigo de barras",
                                  "tipoProduto": "PRODUTO",
                                  "naturezaProduto": "AMBOS",
                                  "statusProduto": "ATIVO",
                                  "precoCusto": 10.0000,
                                  "precoVenda": 20.0000,
                                  "pesoLiquido": 1.000,
                                  "pesoBruto": 1.200,
                                  "alturaCm": 10.000,
                                  "larguraCm": 20.000,
                                  "comprimentoCm": 30.000,
                                  "estoqueMinimo": 5.000,
                                  "controlaLote": false,
                                  "controlaValidade": false,
                                  "permiteFracionamento": true,
                                  "controlaEstoque": true,
                                  "permiteCompra": true,
                                  "permiteVenda": true
                                }
                                """.formatted(referencias.categoriaId(), referencias.marcaId(), referencias.unidadeId(),
                                codigo, sku, codigo, barras)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return new ProdutoCriado(node.get("id").asString(), codigo);
    }

    private CodigoBarrasCriado criarCodigoBarras(String token, String produtoId) throws Exception {
        String codigo = codigoBarrasUnico("790");
        String response = mockMvc.perform(post("/api/v1/produtos-codigos-barras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": "%s",
                                  "tipoCodigo": "EAN_UNIDADE",
                                  "codigo": "%s",
                                  "principal": true,
                                  "observacao": "Codigo principal do fluxo"
                                }
                                """.formatted(produtoId, codigo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return new CodigoBarrasCriado(node.get("id").asString(), codigo);
    }

    private String nomeUnico(String prefixo) {
        return prefixo + " " + Long.toString(Math.abs(System.nanoTime()), 36);
    }

    private String codigoUnico(String prefixo) {
        return prefixo + "-" + Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }

    private String codigoBarrasUnico(String prefixo) {
        long parte = Math.abs(System.nanoTime() % 10_000_000_000L);
        return prefixo + "%010d".formatted(parte);
    }

    private String siglaUnica() {
        long valor = Math.abs(System.nanoTime());
        return "C" + Long.toString(valor, 36).toUpperCase().substring(0, 5);
    }

    private record ReferenciasProduto(String categoriaId, String marcaId, String unidadeId) {
    }

    private record ProdutoCriado(String id, String codigo) {
    }

    private record CodigoBarrasCriado(String id, String codigo) {
    }
}
