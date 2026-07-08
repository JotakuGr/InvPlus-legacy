package br.com.invplus.integration.vendas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integracao dos endpoints de PedidoVendaItem.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoVendaItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Cobre criar, listar, atualizar, leitura completa, excluir e recalcular o total do pedido.
     */
    @Test
    void deveExecutarCrudCompletoComRecalculoDoPedido() throws Exception {
        String token = login().get("accessToken").asString();
        Fixture fixture = criarFixture(token);

        String criado = mockMvc.perform(post("/api/v1/pedidos-venda-itens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadItem(fixture, "2.000", "1.00", "0.50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(fixture.produtoId()))
                .andExpect(jsonPath("$.precoUtilizado").value(19.9000))
                .andExpect(jsonPath("$.subtotal").value(39.80))
                .andExpect(jsonPath("$.valorTotal").value(39.30))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/pedidos-venda/" + fixture.pedidoId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(39.30));

        mockMvc.perform(get("/api/v1/pedidos-venda-itens?pedidoVendaId=" + fixture.pedidoId()
                        + "&produtoId=" + fixture.produtoId() + "&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/pedidos-venda-itens/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadItem(fixture, "1.000", "0.00", "0.10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(20.00));

        mockMvc.perform(get("/api/v1/pedidos-venda/" + fixture.pedidoId() + "/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].id").value(id))
                .andExpect(jsonPath("$.pedido.valorTotal").value(20.00));

        mockMvc.perform(delete("/api/v1/pedidos-venda-itens/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pedidos-venda/" + fixture.pedidoId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(0));

        String listagem = mockMvc.perform(get("/api/v1/pedidos-venda-itens?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(listagem).doesNotContain(id);
    }

    @Test
    void deveRetornar404ParaReferenciasInexistentes() throws Exception {
        String token = login().get("accessToken").asString();
        Fixture fixture = criarFixture(token);

        mockMvc.perform(post("/api/v1/pedidos-venda-itens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadItem(fixture.comProduto(UUID.randomUUID().toString()), "1.000", "0", "0")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto nao encontrado"));

        mockMvc.perform(post("/api/v1/pedidos-venda-itens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadItem(fixture.comPreco(UUID.randomUUID().toString()), "1.000", "0", "0")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ProdutoPreco nao encontrado"));

        mockMvc.perform(post("/api/v1/pedidos-venda-itens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadItem(fixture.comUnidade(UUID.randomUUID().toString()), "1.000", "0", "0")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ProdutoUnidade nao encontrada"));
    }

    @Test
    void deveBloquearItemEmPedidoCancelado() throws Exception {
        String token = login().get("accessToken").asString();
        Fixture fixture = criarFixture(token);
        mockMvc.perform(post("/api/v1/pedidos-venda/" + fixture.pedidoId() + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/pedidos-venda-itens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadItem(fixture, "1.000", "0", "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pedido cancelado ou faturado nao pode ser alterado"));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos-venda-itens"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Pedido Venda Item",
                "sem-pedido-venda-item-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/pedidos-venda-itens")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
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
        String cnpj = cnpjUnico();
        String response = mockMvc.perform(post("/api/v1/empresas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"razaoSocial":"Empresa Venda Item %s","nomeFantasia":"Empresa Venda Item","cnpj":"%s","inscricaoEstadual":"ISENTO","regimeTributario":"NORMAL","crt":3,"telefone":"71999990000","email":"venda-item-%s@teste.com"}
                                """.formatted(cnpj, cnpj, cnpj)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarCliente(String token) throws Exception {
        String cnpj = cnpjUnico();
        String pessoa = mockMvc.perform(post("/api/v1/pessoas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoPessoa":"JURIDICA","nome":"Cliente Venda Item %s","nomeFantasia":"Cliente Venda Item","cpfCnpj":"%s","rgIe":"ISENTO","telefone":"7133334444","email":"cliente-venda-item-%s@teste.com"}
                                """.formatted(cnpj, cnpj, cnpj)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode pessoaNode = objectMapper.readTree(pessoa);
        String response = mockMvc.perform(post("/api/v1/clientes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" 
                                {"pessoaId":"%s","limiteCredito":5000.00,"observacao":"Cliente venda item"}
                                """.formatted(pessoaNode.get("id").asString())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarPedidoVenda(String token, String empresaId, String clienteId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pedidos-venda").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresaId":"%s","clienteId":"%s","numero":"PVITEM-%s","dataEmissao":"2026-07-07","dataEntregaPrevista":"2026-07-10","desconto":0,"acrescimo":0,"observacao":"Pedido venda item"}
                                """.formatted(empresaId, clienteId, codigoCurto())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarCategoria(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/categorias").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Categoria Venda Item %s","descricao":"Categoria"}
                                """.formatted(codigoCurto())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarUnidadeMedida(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/unidades-medida").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sigla":"U%s","descricao":"Unidade venda item","casasDecimais":0}
                                """.formatted(codigoCurto().substring(0, 5))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarProduto(String token, String categoriaId, String unidadeId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":"%s","unidadeMedidaId":"%s","codigo":"PVI-%s","nome":"Produto Venda Item","tipoProduto":"PRODUTO","naturezaProduto":"AMBOS","statusProduto":"ATIVO","precoCusto":0,"precoVenda":0,"estoqueMinimo":0,"controlaEstoque":true,"permiteCompra":true,"permiteVenda":true}
                                """.formatted(categoriaId, unidadeId, codigoCurto())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarProdutoPreco(String token, String produtoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-precos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":"%s","tipoPreco":"VENDA","preco":19.9000,"vigenciaInicio":"2026-07-01","principal":true,"observacao":"Preco venda"}
                                """.formatted(produtoId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarProdutoUnidade(String token, String produtoId, String unidadeId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-unidades").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":"%s","unidadeMedidaId":"%s","fatorConversao":1.000000,"unidadePadrao":true,"permiteCompra":true,"permiteVenda":true,"permiteEstoque":true,"permiteFracionamento":false,"observacao":"Unidade venda"}
                                """.formatted(produtoId, unidadeId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String payloadItem(Fixture fixture, String quantidade, String desconto, String acrescimo) {
        return """
                {"pedidoVendaId":"%s","produtoId":"%s","produtoPrecoId":"%s","produtoUnidadeId":"%s","quantidade":%s,"desconto":%s,"acrescimo":%s,"observacao":"Item venda"}
                """.formatted(fixture.pedidoId(), fixture.produtoId(), fixture.produtoPrecoId(), fixture.produtoUnidadeId(),
                quantidade, desconto, acrescimo);
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "76" + "%012d".formatted(parte);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private record Fixture(String pedidoId, String produtoId, String produtoPrecoId, String produtoUnidadeId) {
        Fixture comProduto(String produtoId) {
            return new Fixture(pedidoId, produtoId, produtoPrecoId, produtoUnidadeId);
        }

        Fixture comPreco(String produtoPrecoId) {
            return new Fixture(pedidoId, produtoId, produtoPrecoId, produtoUnidadeId);
        }

        Fixture comUnidade(String produtoUnidadeId) {
            return new Fixture(pedidoId, produtoId, produtoPrecoId, produtoUnidadeId);
        }
    }
}
