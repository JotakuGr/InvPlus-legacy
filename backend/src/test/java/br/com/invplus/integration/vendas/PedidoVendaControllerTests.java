package br.com.invplus.integration.vendas;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.support.security.EmpresaTestFactory;
import br.com.invplus.support.security.JwtTestFactory;
import br.com.invplus.support.security.UsuarioEmpresaTestFactory;
import br.com.invplus.support.security.UsuarioTestFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integracao dos endpoints de PedidoVenda.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoVendaControllerTests {

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

    /**
     * Cobre CRUD do cabecalho, aprovacoes, cancelamento, filtros e leitura completa.
     */
    @Test
    void deveExecutarFluxoCompletoDePedidoVenda() throws Exception {
        ContextoTeste contexto = criarContexto();
        String token = contexto.token();
        JsonNode cliente = criarCliente(token);
        JsonNode vendedor = criarFuncionario(token);
        String numero = "PV-" + codigoCurto();

        String criado = mockMvc.perform(post("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null, cliente.get("id").asString(),
                                vendedor.get("id").asString(), numero, "Pedido inicial")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.valorTotal").value(0))
                .andExpect(jsonPath("$.clienteNome").value(cliente.get("nome").asString()))
                .andExpect(jsonPath("$.vendedorNome").value(vendedor.get("nome").asString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/pedidos-venda/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/pedidos-venda?clienteId=" + cliente.get("id").asString()
                        + "&vendedorId=" + vendedor.get("id").asString()
                        + "&numero=" + numero
                        + "&status=ABERTO&dataEmissaoDe=2026-07-01&dataEmissaoAte=2026-07-31&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/pedidos-venda/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null, cliente.get("id").asString(),
                                null, numero, "Pedido atualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendedorId").doesNotExist())
                .andExpect(jsonPath("$.observacao").value("Pedido atualizado"));

        mockMvc.perform(post("/api/v1/pedidos-venda/" + id + "/aprovar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pedido de venda precisa possuir itens para ser aprovado"));

        mockMvc.perform(get("/api/v1/pedidos-venda/" + id + "/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.id").value(id))
                .andExpect(jsonPath("$.pedido.status").value("ABERTO"))
                .andExpect(jsonPath("$.reservas.length()").value(0));

        mockMvc.perform(post("/api/v1/pedidos-venda/" + id + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.canceladoEm").exists());

        mockMvc.perform(put("/api/v1/pedidos-venda/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null, cliente.get("id").asString(),
                                null, numero, "Tentativa bloqueada")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pedido cancelado ou faturado nao pode ser alterado"));

        mockMvc.perform(delete("/api/v1/pedidos-venda/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pedidos-venda/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    /**
     * Garante unicidade de numero por empresa entre pedidos ativos.
     */
    @Test
    void naoDeveCriarNumeroDuplicadoNaMesmaEmpresa() throws Exception {
        String token = criarContexto().token();
        JsonNode cliente = criarCliente(token);
        String numero = "PV-DUP-" + codigoCurto();
        criarPedido(token, cliente.get("id").asString(), numero);

        mockMvc.perform(post("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null, cliente.get("id").asString(),
                                null, numero, "Duplicado")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe pedido de venda ativo com este numero para a empresa"));
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos-venda"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401ComAccessTokenSemEmpresa() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        String token = jwtTestFactory.accessTokenSemEmpresa(
                usuario.getId(), usuario.getEmail(), java.util.Set.of("PEDIDO_VENDA:*"));

        mockMvc.perform(get("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar404AoBuscarPedidoDeOutraEmpresa() throws Exception {
        ContextoTeste contextoA = criarContexto();
        ContextoTeste contextoB = criarContexto();
        JsonNode cliente = criarCliente(contextoB.token());
        JsonNode pedidoB = criarPedido(
                contextoB.token(), cliente.get("id").asString(), "PV-ISO-" + codigoCurto());

        mockMvc.perform(get("/api/v1/pedidos-venda/" + pedidoB.get("id").asString())
                        .header("Authorization", "Bearer " + contextoA.token()))
                .andExpect(status().isNotFound());
    }

    /**
     * Usuario autenticado sem permissao de PedidoVenda deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Empresa empresa = empresaTestFactory.criarAtiva();
        Usuario usuario = usuarioTestFactory.criarAtivoSemPerfil();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of());

        mockMvc.perform(get("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode criarCliente(String token) throws Exception {
        JsonNode pessoa = criarPessoa(token, "Cliente Vendas", "cliente-vendas", "JURIDICA", cnpjUnico());
        String response = mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pessoaId": "%s", "limiteCredito": 5000.00, "observacao": "Cliente de vendas"}
                                """.formatted(pessoa.get("id").asString())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarFuncionario(String token) throws Exception {
        JsonNode pessoa = criarPessoa(token, "Vendedor Vendas", "vendedor-vendas", "FISICA", cpfUnico());
        String response = mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": "%s",
                                  "cargo": "Vendedor",
                                  "salario": 3000.00,
                                  "dataAdmissao": "2026-01-10",
                                  "observacao": "Vendedor do fluxo"
                                }
                                """.formatted(pessoa.get("id").asString())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarPessoa(String token, String nome, String emailPrefixo, String tipoPessoa, String cpfCnpj) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoPessoa": "%s",
                                  "nome": "%s %s",
                                  "nomeFantasia": "%s",
                                  "cpfCnpj": "%s",
                                  "rgIe": "ISENTO",
                                  "telefone": "7133334444",
                                  "email": "%s-%s@teste.com"
                                }
                                """.formatted(tipoPessoa, nome, codigoCurto(), nome, cpfCnpj, emailPrefixo, cpfCnpj)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarPedido(String token, String clienteId, String numero) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null, clienteId, null, numero, "Pedido base")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String payload(String filialId, String clienteId, String vendedorId, String numero, String observacao) {
        return """
                {
                  "filialId": %s,
                  "clienteId": "%s",
                  "vendedorId": %s,
                  "numero": "%s",
                  "dataEmissao": "2026-07-07",
                  "dataEntregaPrevista": "2026-07-10",
                  "desconto": 0,
                  "acrescimo": 0,
                  "observacao": "%s"
                }
                """.formatted(
                filialId == null ? "null" : "\"" + filialId + "\"",
                clienteId,
                vendedorId == null ? "null" : "\"" + vendedorId + "\"",
                numero,
                observacao
        );
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "79" + "%012d".formatted(parte);
    }

    private String cpfUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000L);
        return "9" + "%010d".formatted(parte);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private ContextoTeste criarContexto() {
        Empresa empresa = empresaTestFactory.criarAtiva();
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("PEDIDO_VENDA:*"));
        return new ContextoTeste(token);
    }

    private record ContextoTeste(String token) {
    }
}
