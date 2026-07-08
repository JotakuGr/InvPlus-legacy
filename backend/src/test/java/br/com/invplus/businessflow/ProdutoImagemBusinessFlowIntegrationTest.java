package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Testa o fluxo real Produto -> Upload de Arquivo -> ProdutoImagem.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoImagemBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Prova que Arquivo e ProdutoImagem conversam corretamente via endpoints reais.
     */
    @Test
    void deveExecutarFluxoProdutoArquivoImagem() throws Exception {
        String token = login().get("accessToken").asString();
        ReferenciasProduto referencias = criarReferenciasProduto(token);
        ProdutoCriado produto = criarProduto(token, referencias);
        ArquivoCriado arquivo = uploadArquivo(token, produto.id());
        String produtoImagemId = criarProdutoImagem(token, produto.id(), arquivo.id());

        mockMvc.perform(get("/api/v1/produtos/" + produto.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produto.id()));

        mockMvc.perform(get("/api/v1/arquivos/" + arquivo.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(arquivo.id()))
                .andExpect(jsonPath("$.entidade").value("PRODUTO"))
                .andExpect(jsonPath("$.entidadeId").value(produto.id()));

        mockMvc.perform(get("/api/v1/produtos-imagens/" + produtoImagemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(produto.id()))
                .andExpect(jsonPath("$.arquivoId").value(arquivo.id()))
                .andExpect(jsonPath("$.arquivoUrl").value(arquivo.url()))
                .andExpect(jsonPath("$.principal").value(true));

        mockMvc.perform(get("/api/v1/produtos-imagens?produtoId=" + produto.id()
                        + "&arquivoId=" + arquivo.id()
                        + "&principal=true&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(produtoImagemId));
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
        String nome = nomeUnico("Categoria Fluxo Imagem");
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Categoria criada pelo fluxo de imagem"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String nome = nomeUnico("Marca Fluxo Imagem");
        String response = mockMvc.perform(post("/api/v1/marcas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Marca criada pelo fluxo de imagem"
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
        String codigo = codigoUnico("PROD-FLUXO-IMG");
        String sku = codigoUnico("SKU-FLUXO-IMG");
        String barras = codigoBarrasUnico();
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
                                  "nome": "Produto Fluxo Imagem",
                                  "descricao": "Produto criado pelo fluxo de imagem",
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

    private ArquivoCriado uploadArquivo(String token, String produtoId) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "produto-fluxo-imagem.png",
                "image/png",
                ("imagem-fluxo-" + UUID.randomUUID()).getBytes()
        );
        String response = mockMvc.perform(multipart("/api/v1/arquivos/upload")
                        .file(file)
                        .param("entidade", "PRODUTO")
                        .param("entidadeId", produtoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entidade").value("PRODUTO"))
                .andExpect(jsonPath("$.entidadeId").value(produtoId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return new ArquivoCriado(node.get("id").asString(), node.get("url").asString());
    }

    private String criarProdutoImagem(String token, String produtoId, String arquivoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-imagens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": "%s",
                                  "arquivoId": "%s",
                                  "principal": true,
                                  "ordem": 0,
                                  "descricao": "Imagem principal do fluxo"
                                }
                                """.formatted(produtoId, arquivoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.arquivoId").value(arquivoId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String nomeUnico(String prefixo) {
        return prefixo + " " + Long.toString(Math.abs(System.nanoTime()), 36);
    }

    private String codigoUnico(String prefixo) {
        return prefixo + "-" + Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }

    private String codigoBarrasUnico() {
        long parte = Math.abs(System.nanoTime() % 10_000_000_000L);
        return "789" + "%010d".formatted(parte);
    }

    private String siglaUnica() {
        long valor = Math.abs(System.nanoTime());
        return "G" + Long.toString(valor, 36).toUpperCase().substring(0, 5);
    }

    private record ReferenciasProduto(String categoriaId, String marcaId, String unidadeId) {
    }

    private record ProdutoCriado(String id, String codigo) {
    }

    private record ArquivoCriado(String id, String url) {
    }
}
