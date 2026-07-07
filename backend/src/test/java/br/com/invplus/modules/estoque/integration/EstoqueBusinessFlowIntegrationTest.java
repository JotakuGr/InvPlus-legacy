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
 * Fluxo real Cadastros/Produtos/Estoque ate a criacao do saldo atual.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstoqueBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveCriarSaldoDeEstoqueNoFluxoCompleto() throws Exception {
        String token = login().get("accessToken").asString();
        JsonNode empresa = criarEmpresa(token);
        JsonNode deposito = criarDeposito(token, empresa.get("id").asString());
        JsonNode localizacao = criarLocalizacao(token, deposito.get("id").asString());
        JsonNode categoria = criarCategoria(token);
        JsonNode unidade = criarUnidade(token);
        JsonNode produto = criarProduto(token, categoria.get("id").asString(), unidade.get("id").asString());
        JsonNode estoque = criarEstoque(token, produto.get("id").asString(), empresa.get("id").asString(),
                deposito.get("id").asString(), localizacao.get("id").asString());

        mockMvc.perform(get("/api/v1/estoques/" + estoque.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(produto.get("id").asString()))
                .andExpect(jsonPath("$.empresaId").value(empresa.get("id").asString()))
                .andExpect(jsonPath("$.depositoId").value(deposito.get("id").asString()))
                .andExpect(jsonPath("$.localizacaoId").value(localizacao.get("id").asString()))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(7.000));

        mockMvc.perform(get("/api/v1/estoques/saldo?produtoId=" + produto.get("id").asString()
                        + "&empresaId=" + empresa.get("id").asString()
                        + "&depositoId=" + deposito.get("id").asString()
                        + "&localizacaoId=" + localizacao.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(estoque.get("id").asString()));

        mockMvc.perform(get("/api/v1/estoques?produtoId=" + produto.get("id").asString()
                        + "&empresaId=" + empresa.get("id").asString()
                        + "&depositoId=" + deposito.get("id").asString()
                        + "&localizacaoId=" + localizacao.get("id").asString()
                        + "&comSaldo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(estoque.get("id").asString()));
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
                                  "email": "empresa-estoque-%s@teste.com"
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
                                  "nome": "Deposito Estoque",
                                  "descricao": "Deposito para saldo",
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
                                  "principal": false,
                                  "bloqueada": false,
                                  "observacao": "Fluxo estoque"
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
                                  "nome": "Categoria Estoque %s",
                                  "descricao": "Categoria fluxo estoque",
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
                                  "sigla": "U%s",
                                  "descricao": "Unidade fluxo estoque",
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
                                  "nome": "Produto Estoque Fluxo",
                                  "descricao": "Produto para saldo",
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

    private JsonNode criarEstoque(String token, String produtoId, String empresaId, String depositoId, String localizacaoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/estoques")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": "%s",
                                  "empresaId": "%s",
                                  "depositoId": "%s",
                                  "localizacaoId": "%s",
                                  "quantidade": 10.000,
                                  "quantidadeReservada": 3.000,
                                  "quantidadeAvariada": 0.000,
                                  "quantidadeTransito": 0.000,
                                  "estoqueMinimo": 2.000,
                                  "estoqueMaximo": 50.000
                                }
                                """.formatted(produtoId, empresaId, depositoId, localizacaoId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String cnpjUnico() {
        return "18" + "%012d".formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L));
    }

    private String codigoCurto() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }
}
