package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.vendas.entity.PedidoVendaItem;
import br.com.invplus.modules.vendas.repository.PedidoVendaItemRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Valida o fluxo comercial inicial com item: cliente, pedido, produto, preco e unidade.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoVendaItemBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoVendaItemRepository pedidoVendaItemRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void deveCriarItemRecalcularPedidoEConsultarPedidoCompletoComItens() throws Exception {
        String token = login().get("accessToken").asString();
        Fixture fixture = criarFixture(token);

        JsonNode item = criarItem(token, fixture, "3.000", "2.00", "1.00");

        mockMvc.perform(get("/api/v1/pedidos-venda/" + fixture.pedidoId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(58.70));

        mockMvc.perform(get("/api/v1/pedidos-venda/" + fixture.pedidoId() + "/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.id").value(fixture.pedidoId()))
                .andExpect(jsonPath("$.itens[0].id").value(item.get("id").asString()))
                .andExpect(jsonPath("$.itens[0].produtoId").value(fixture.produtoId()))
                .andExpect(jsonPath("$.itens[0].precoUtilizado").value(19.9000))
                .andExpect(jsonPath("$.itens[0].subtotal").value(59.70))
                .andExpect(jsonPath("$.itens[0].valorTotal").value(58.70));
    }

    @Test
    void deveBloquearAtualizacaoConcorrenteDePedidoVendaItemPorOptimisticLock() throws Exception {
        String token = login().get("accessToken").asString();
        Fixture fixture = criarFixture(token);
        JsonNode item = criarItem(token, fixture, "1.000", "0", "0");
        UUID itemId = UUID.fromString(item.get("id").asString());
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        PedidoVendaItem usuarioA = tx.execute(status -> pedidoVendaItemRepository.findById(itemId).orElseThrow());
        PedidoVendaItem usuarioB = tx.execute(status -> pedidoVendaItemRepository.findById(itemId).orElseThrow());

        usuarioA.setObservacao("Alteracao concorrente A");
        tx.executeWithoutResult(status -> pedidoVendaItemRepository.saveAndFlush(usuarioA));

        usuarioB.setObservacao("Alteracao concorrente B");
        assertThatThrownBy(() -> tx.executeWithoutResult(
                status -> pedidoVendaItemRepository.saveAndFlush(usuarioB)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
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

    private Fixture criarFixture(String token) throws Exception {
        JsonNode empresa = criarEmpresa(token);
        JsonNode cliente = criarCliente(token);
        JsonNode pedido = criarPedidoVenda(token, empresa.get("id").asString(), cliente.get("id").asString());
        JsonNode categoria = criarCategoria(token);
        JsonNode unidadeMedida = criarUnidadeMedida(token);
        JsonNode produto = criarProduto(token, categoria.get("id").asString(), unidadeMedida.get("id").asString());
        JsonNode preco = criarProdutoPreco(token, produto.get("id").asString());
        JsonNode produtoUnidade = criarProdutoUnidade(token, produto.get("id").asString(), unidadeMedida.get("id").asString());
        return new Fixture(
                pedido.get("id").asString(),
                produto.get("id").asString(),
                preco.get("id").asString(),
                produtoUnidade.get("id").asString()
        );
    }

    private JsonNode criarItem(String token, Fixture fixture, String quantidade, String desconto, String acrescimo) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pedidos-venda-itens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pedidoVendaId": "%s",
                                  "produtoId": "%s",
                                  "produtoPrecoId": "%s",
                                  "produtoUnidadeId": "%s",
                                  "quantidade": %s,
                                  "desconto": %s,
                                  "acrescimo": %s,
                                  "observacao": "Item do fluxo comercial"
                                }
                                """.formatted(fixture.pedidoId(), fixture.produtoId(), fixture.produtoPrecoId(),
                                fixture.produtoUnidadeId(), quantidade, desconto, acrescimo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarEmpresa(String token) throws Exception {
        String cnpj = cnpjUnico("75");
        String response = mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"razaoSocial":"Empresa Fluxo Item %s","nomeFantasia":"Empresa Fluxo Item","cnpj":"%s","inscricaoEstadual":"ISENTO","regimeTributario":"NORMAL","crt":3,"telefone":"71999990000","email":"empresa-fluxo-item-%s@teste.com"}
                                """.formatted(cnpj, cnpj, cnpj)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarCliente(String token) throws Exception {
        String cnpj = cnpjUnico("74");
        String pessoa = mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoPessoa":"JURIDICA","nome":"Cliente Fluxo Item %s","nomeFantasia":"Cliente Fluxo Item","cpfCnpj":"%s","rgIe":"ISENTO","telefone":"7133334444","email":"cliente-fluxo-item-%s@teste.com"}
                                """.formatted(cnpj, cnpj, cnpj)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode pessoaNode = objectMapper.readTree(pessoa);
        String response = mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pessoaId":"%s","limiteCredito":5000.00,"observacao":"Cliente do fluxo de item"}
                                """.formatted(pessoaNode.get("id").asString())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarPedidoVenda(String token, String empresaId, String clienteId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresaId":"%s","clienteId":"%s","numero":"PVIF-%s","dataEmissao":"2026-07-07","dataEntregaPrevista":"2026-07-10","desconto":0,"acrescimo":0,"observacao":"Pedido fluxo item"}
                                """.formatted(empresaId, clienteId, codigoCurto())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarCategoria(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Categoria Fluxo Item %s","descricao":"Categoria"}
                                """.formatted(codigoCurto())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarUnidadeMedida(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/unidades-medida")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sigla":"F%s","descricao":"Unidade fluxo item","casasDecimais":0}
                                """.formatted(codigoCurto().substring(0, 5))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarProduto(String token, String categoriaId, String unidadeId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":"%s","unidadeMedidaId":"%s","codigo":"PVIF-%s","nome":"Produto Fluxo Item","tipoProduto":"PRODUTO","naturezaProduto":"AMBOS","statusProduto":"ATIVO","precoCusto":0,"precoVenda":0,"estoqueMinimo":0,"controlaEstoque":true,"permiteCompra":true,"permiteVenda":true}
                                """.formatted(categoriaId, unidadeId, codigoCurto())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarProdutoPreco(String token, String produtoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":"%s","tipoPreco":"VENDA","preco":19.9000,"vigenciaInicio":"2026-07-01","principal":true,"observacao":"Preco venda"}
                                """.formatted(produtoId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarProdutoUnidade(String token, String produtoId, String unidadeId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-unidades")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":"%s","unidadeMedidaId":"%s","fatorConversao":1.000000,"unidadePadrao":true,"permiteCompra":true,"permiteVenda":true,"permiteEstoque":true,"permiteFracionamento":false,"observacao":"Unidade venda"}
                                """.formatted(produtoId, unidadeId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String cnpjUnico(String prefixo) {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return prefixo + "%012d".formatted(parte);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private record Fixture(String pedidoId, String produtoId, String produtoPrecoId, String produtoUnidadeId) {
    }
}
