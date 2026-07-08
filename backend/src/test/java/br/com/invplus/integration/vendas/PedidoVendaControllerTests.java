package br.com.invplus.integration.vendas;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Cobre CRUD do cabecalho, aprovacoes, cancelamento, filtros e leitura completa.
     */
    @Test
    void deveExecutarFluxoCompletoDePedidoVenda() throws Exception {
        String token = login().get("accessToken").asString();
        JsonNode empresa = criarEmpresa(token);
        JsonNode cliente = criarCliente(token);
        JsonNode vendedor = criarFuncionario(token);
        String numero = "PV-" + codigoCurto();

        String criado = mockMvc.perform(post("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.get("id").asString(), null, cliente.get("id").asString(),
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

        mockMvc.perform(get("/api/v1/pedidos-venda?empresaId=" + empresa.get("id").asString()
                        + "&clienteId=" + cliente.get("id").asString()
                        + "&vendedorId=" + vendedor.get("id").asString()
                        + "&numero=" + numero
                        + "&status=ABERTO&dataEmissaoDe=2026-07-01&dataEmissaoAte=2026-07-31&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/pedidos-venda/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.get("id").asString(), null, cliente.get("id").asString(),
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
                        .content(payload(empresa.get("id").asString(), null, cliente.get("id").asString(),
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
        String token = login().get("accessToken").asString();
        JsonNode empresa = criarEmpresa(token);
        JsonNode cliente = criarCliente(token);
        String numero = "PV-DUP-" + codigoCurto();
        criarPedido(token, empresa.get("id").asString(), cliente.get("id").asString(), numero);

        mockMvc.perform(post("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.get("id").asString(), null, cliente.get("id").asString(),
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

    /**
     * Usuario autenticado sem permissao de PedidoVenda deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Pedido Venda",
                "sem-pedido-venda-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
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
        String response = mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "razaoSocial": "Empresa Vendas %s",
                                  "nomeFantasia": "Empresa Vendas",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "regimeTributario": "NORMAL",
                                  "crt": 3,
                                  "telefone": "71999990000",
                                  "email": "vendas-%s@teste.com"
                                }
                                """.formatted(cnpj, cnpj, cnpj)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
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

    private JsonNode criarPedido(String token, String empresaId, String clienteId, String numero) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresaId, null, clienteId, null, numero, "Pedido base")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String payload(String empresaId, String filialId, String clienteId, String vendedorId, String numero, String observacao) {
        return """
                {
                  "empresaId": "%s",
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
                empresaId,
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
}
