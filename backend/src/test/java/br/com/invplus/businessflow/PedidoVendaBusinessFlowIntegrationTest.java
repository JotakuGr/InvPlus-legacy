package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Prova o fluxo inicial Empresa -> Cliente -> PedidoVenda com historico e concorrencia.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoVendaBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoVendaRepository pedidoVendaRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EmpresaTestFactory empresaTestFactory;

    @Autowired
    private UsuarioTestFactory usuarioTestFactory;

    @Autowired
    private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;

    @Autowired
    private JwtTestFactory jwtTestFactory;

    @Test
    void deveCriarAprovarCancelarEConsultarPedidoVendaCompleto() throws Exception {
        String token = criarContexto();
        JsonNode cliente = criarCliente(token);
        JsonNode pedido = criarPedidoVenda(token, cliente.get("id").asString());

        mockMvc.perform(post("/api/v1/pedidos-venda/" + pedido.get("id").asString() + "/aprovar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pedido de venda precisa possuir itens para ser aprovado"));

        mockMvc.perform(get("/api/v1/pedidos-venda/" + pedido.get("id").asString() + "/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.id").value(pedido.get("id").asString()))
                .andExpect(jsonPath("$.pedido.status").value("ABERTO"))
                .andExpect(jsonPath("$.reservas.length()").value(0))
                .andExpect(jsonPath("$.historico[0].statusNovo").value("ABERTO"));

        mockMvc.perform(post("/api/v1/pedidos-venda/" + pedido.get("id").asString() + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void deveBloquearAtualizacaoConcorrenteDePedidoVendaPorOptimisticLock() throws Exception {
        String token = criarContexto();
        JsonNode cliente = criarCliente(token);
        JsonNode pedido = criarPedidoVenda(token, cliente.get("id").asString());
        UUID pedidoId = UUID.fromString(pedido.get("id").asString());
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        PedidoVenda usuarioA = tx.execute(status -> pedidoVendaRepository.findById(pedidoId).orElseThrow());
        PedidoVenda usuarioB = tx.execute(status -> pedidoVendaRepository.findById(pedidoId).orElseThrow());

        usuarioA.setObservacao("Alteracao concorrente A");
        tx.executeWithoutResult(status -> pedidoVendaRepository.saveAndFlush(usuarioA));

        usuarioB.setObservacao("Alteracao concorrente B");
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> pedidoVendaRepository.saveAndFlush(usuarioB)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    private JsonNode criarCliente(String token) throws Exception {
        String cnpj = cnpjUnico();
        String pessoa = mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoPessoa": "JURIDICA",
                                  "nome": "Cliente Fluxo Venda %s",
                                  "nomeFantasia": "Cliente Fluxo Venda",
                                  "cpfCnpj": "%s",
                                  "rgIe": "ISENTO",
                                  "telefone": "7133334444",
                                  "email": "cliente-fluxo-venda-%s@teste.com"
                                }
                                """.formatted(cnpj, cnpj, cnpj)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode pessoaNode = objectMapper.readTree(pessoa);

        String response = mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pessoaId": "%s", "limiteCredito": 5000.00, "observacao": "Cliente do fluxo de vendas"}
                                """.formatted(pessoaNode.get("id").asString())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarPedidoVenda(String token, String clienteId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pedidos-venda")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "%s",
                                  "numero": "PV-FLUXO-%s",
                                  "dataEmissao": "2026-07-07",
                                  "dataEntregaPrevista": "2026-07-10",
                                  "desconto": 0,
                                  "acrescimo": 0,
                                  "observacao": "Pedido do fluxo de vendas"
                                }
                                """.formatted(clienteId, codigoCurto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "77" + "%012d".formatted(parte);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String criarContexto() {
        Empresa empresa = empresaTestFactory.criarAtiva();
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        return jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("PEDIDO_VENDA:*"));
    }
}
