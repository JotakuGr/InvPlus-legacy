package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaRepository;
import br.com.invplus.modules.compras.repository.PedidoCompraRepository;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.support.security.EmpresaTestFactory;
import br.com.invplus.support.security.JwtTestFactory;
import br.com.invplus.support.security.UsuarioEmpresaTestFactory;
import br.com.invplus.support.security.UsuarioTestFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
 * Prova o fluxo PedidoCompra -> EntradaMercadoria -> Estoque -> MovimentacaoEstoque.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComprasBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoCompraRepository pedidoCompraRepository;

    @Autowired
    private EntradaMercadoriaRepository entradaMercadoriaRepository;

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
    void deveEfetivarEntradaEMovimentarEstoqueAPartirDoPedidoCompra() throws Exception {
        ContextoTeste contexto = criarContexto();
        String token = contexto.token();
        JsonNode deposito = criarDeposito(token);
        JsonNode localizacao = criarLocalizacao(token, deposito.get("id").asString());
        JsonNode categoria = criarCategoria(token);
        JsonNode unidade = criarUnidade(token);
        JsonNode produto = criarProduto(token, categoria.get("id").asString(), unidade.get("id").asString());
        JsonNode estoque = criarEstoque(token, produto.get("id").asString(),
                deposito.get("id").asString(), localizacao.get("id").asString());
        JsonNode pessoa = criarPessoaJuridica(token);
        JsonNode fornecedor = criarFornecedor(token, pessoa.get("id").asString());

        JsonNode pedido = criarPedidoCompra(token, fornecedor.get("id").asString());
        mockMvc.perform(post("/api/v1/pedidos-compra/" + pedido.get("id").asString() + "/aprovar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        JsonNode pedidoItem = criarPedidoCompraItem(token, pedido.get("id").asString(), produto.get("id").asString());
        mockMvc.perform(get("/api/v1/pedidos-compra/" + pedido.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(125.00));

        JsonNode entrada = criarEntradaMercadoria(token, fornecedor.get("id").asString(),
                pedido.get("id").asString());
        criarEntradaMercadoriaItem(token, entrada.get("id").asString(), pedidoItem.get("id").asString(),
                produto.get("id").asString(), deposito.get("id").asString(), localizacao.get("id").asString());

        mockMvc.perform(post("/api/v1/entradas-mercadoria/" + entrada.get("id").asString() + "/efetivar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EFETIVADA"));

        mockMvc.perform(get("/api/v1/estoques/" + estoque.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(110.000))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(110.000));

        mockMvc.perform(get("/api/v1/movimentacoes-estoque?estoqueId=" + estoque.get("id").asString()
                        + "&origem=COMPRA&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].origem").value("COMPRA"));

        mockMvc.perform(get("/api/v1/pedidos-compra/" + pedido.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEBIDO"));

        mockMvc.perform(get("/api/v1/pedidos-compra/" + pedido.get("id").asString() + "/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].produtoId").value(produto.get("id").asString()))
                .andExpect(jsonPath("$.entradasVinculadas[0].id").value(entrada.get("id").asString()))
                .andExpect(jsonPath("$.historico[0].novoStatus").value("RECEBIDO"));

        mockMvc.perform(get("/api/v1/entradas-mercadoria/" + entrada.get("id").asString() + "/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].produtoId").value(produto.get("id").asString()))
                .andExpect(jsonPath("$.movimentacoesGeradas[0].origem").value("COMPRA"))
                .andExpect(jsonPath("$.historico[0].novoStatus").value("EFETIVADA"));
    }

    @Test
    void deveBloquearAtualizacaoConcorrenteDePedidoCompraPorOptimisticLock() throws Exception {
        String token = criarContexto().token();
        JsonNode pessoa = criarPessoaJuridica(token);
        JsonNode fornecedor = criarFornecedor(token, pessoa.get("id").asString());
        JsonNode pedido = criarPedidoCompra(token, fornecedor.get("id").asString());
        UUID pedidoId = UUID.fromString(pedido.get("id").asString());
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        PedidoCompra usuarioA = tx.execute(status -> pedidoCompraRepository.findById(pedidoId).orElseThrow());
        PedidoCompra usuarioB = tx.execute(status -> pedidoCompraRepository.findById(pedidoId).orElseThrow());

        usuarioA.setObservacao("Alteracao concorrente A");
        tx.executeWithoutResult(status -> pedidoCompraRepository.saveAndFlush(usuarioA));

        usuarioB.setObservacao("Alteracao concorrente B");
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> pedidoCompraRepository.saveAndFlush(usuarioB)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void deveBloquearAtualizacaoConcorrenteDeEntradaMercadoriaPorOptimisticLock() throws Exception {
        String token = criarContexto().token();
        JsonNode pessoa = criarPessoaJuridica(token);
        JsonNode fornecedor = criarFornecedor(token, pessoa.get("id").asString());
        JsonNode pedido = criarPedidoCompra(token, fornecedor.get("id").asString());
        JsonNode entrada = criarEntradaMercadoria(token, fornecedor.get("id").asString(),
                pedido.get("id").asString());
        UUID entradaId = UUID.fromString(entrada.get("id").asString());
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        EntradaMercadoria usuarioA = tx.execute(status -> entradaMercadoriaRepository.findById(entradaId).orElseThrow());
        EntradaMercadoria usuarioB = tx.execute(status -> entradaMercadoriaRepository.findById(entradaId).orElseThrow());

        usuarioA.setObservacao("Alteracao concorrente A");
        tx.executeWithoutResult(status -> entradaMercadoriaRepository.saveAndFlush(usuarioA));

        usuarioB.setObservacao("Alteracao concorrente B");
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> entradaMercadoriaRepository.saveAndFlush(usuarioB)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void deveManterSaldoConsistenteComEntradasSimultaneasNoMesmoEstoque() throws Exception {
        String token = criarContexto().token();
        JsonNode deposito = criarDeposito(token);
        JsonNode localizacao = criarLocalizacao(token, deposito.get("id").asString());
        JsonNode categoria = criarCategoria(token);
        JsonNode unidade = criarUnidade(token);
        JsonNode produto = criarProduto(token, categoria.get("id").asString(), unidade.get("id").asString());
        JsonNode estoque = criarEstoque(token, produto.get("id").asString(),
                deposito.get("id").asString(), localizacao.get("id").asString());
        JsonNode pessoa = criarPessoaJuridica(token);
        JsonNode fornecedor = criarFornecedor(token, pessoa.get("id").asString());
        JsonNode pedido = criarPedidoCompra(token, fornecedor.get("id").asString());
        JsonNode pedidoItem = criarPedidoCompraItem(token, pedido.get("id").asString(), produto.get("id").asString());

        List<String> entradas = new ArrayList<>();
        for (int indice = 0; indice < 20; indice++) {
            JsonNode entrada = criarEntradaMercadoria(token, fornecedor.get("id").asString(),
                    pedido.get("id").asString());
            criarEntradaMercadoriaItemComQuantidade(token, entrada.get("id").asString(), pedidoItem.get("id").asString(),
                    produto.get("id").asString(), deposito.get("id").asString(), localizacao.get("id").asString(), "1.000");
            entradas.add(entrada.get("id").asString());
        }

        var executor = Executors.newFixedThreadPool(8);
        List<Callable<Boolean>> tarefas = entradas.stream()
                .<Callable<Boolean>>map(entradaId -> () -> efetivarSemFalharTeste(token, entradaId))
                .toList();
        int efetivadas = 0;
        try {
            for (var resultado : executor.invokeAll(tarefas, 60, TimeUnit.SECONDS)) {
                if (resultado.isDone() && !resultado.isCancelled() && Boolean.TRUE.equals(resultado.get())) {
                    efetivadas++;
                }
            }
        } finally {
            executor.shutdownNow();
        }

        String response = mockMvc.perform(get("/api/v1/estoques/" + estoque.get("id").asString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode saldo = objectMapper.readTree(response);

        assertEquals(100.0 + efetivadas, saldo.get("quantidade").asDouble(), 0.001);
        assertEquals(saldo.get("quantidade").asDouble(), saldo.get("quantidadeDisponivel").asDouble(), 0.001);
    }

    @Test
    void deveIsolarPedidosEntradasEItensPorEmpresa() throws Exception {
        ContextoTeste contextoA = criarContexto();
        ContextoTeste contextoB = criarContexto();
        String tokenA = contextoA.token();
        String tokenB = contextoB.token();
        JsonNode pessoa = criarPessoaJuridica(tokenA);
        JsonNode fornecedor = criarFornecedor(tokenA, pessoa.get("id").asString());
        JsonNode categoria = criarCategoria(tokenA);
        JsonNode unidade = criarUnidade(tokenA);
        JsonNode produto = criarProduto(tokenA, categoria.get("id").asString(), unidade.get("id").asString());
        JsonNode pedido = criarPedidoCompra(tokenA, fornecedor.get("id").asString());
        JsonNode pedidoItem = criarPedidoCompraItem(tokenA, pedido.get("id").asString(), produto.get("id").asString());
        JsonNode entrada = criarEntradaMercadoria(
                tokenA, fornecedor.get("id").asString(), pedido.get("id").asString());

        mockMvc.perform(get("/api/v1/pedidos-compra/" + pedido.get("id").asString())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/pedidos-compra-itens/" + pedidoItem.get("id").asString())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/entradas-mercadoria/" + entrada.get("id").asString())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        String pedidosB = mockMvc.perform(get("/api/v1/pedidos-compra?page=0&size=50")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String entradasB = mockMvc.perform(get("/api/v1/entradas-mercadoria?page=0&size=50")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(pedidosB).doesNotContain(pedido.get("id").asString());
        org.assertj.core.api.Assertions.assertThat(entradasB).doesNotContain(entrada.get("id").asString());

        mockMvc.perform(post("/api/v1/pedidos-compra-itens")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pedidoCompraId": "%s",
                                  "produtoId": "%s",
                                  "quantidade": 1,
                                  "valorUnitario": 1
                                }
                                """.formatted(pedido.get("id").asString(), produto.get("id").asString())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarTokenOperacionalSemEmpresa() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        String token = jwtTestFactory.accessTokenSemEmpresa(
                usuario.getId(), usuario.getEmail(), java.util.Set.of("COMPRAS:*"));

        mockMvc.perform(get("/api/v1/pedidos-compra")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissaoDeCompras() throws Exception {
        Empresa empresa = empresaTestFactory.criarAtiva();
        Usuario usuario = usuarioTestFactory.criarAtivoSemPerfil();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("SEGURANCA:*"));

        mockMvc.perform(get("/api/v1/pedidos-compra")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode criarPessoaJuridica(String token) throws Exception {
        String cnpj = cnpjUnico();
        String response = mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoPessoa": "JURIDICA",
                                  "nome": "Fornecedor Compras %s",
                                  "nomeFantasia": "Fornecedor Compras",
                                  "cpfCnpj": "%s",
                                  "rgIe": "ISENTO",
                                  "telefone": "7133334444",
                                  "email": "fornecedor-compras-%s@teste.com"
                                }
                                """.formatted(cnpj, cnpj, cnpj)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarFornecedor(String token, String pessoaId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": "%s",
                                  "prazoMedioEntrega": 3,
                                  "observacao": "Fornecedor do fluxo de compras"
                                }
                                """.formatted(pessoaId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarDeposito(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/depositos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "CMP-%s",
                                  "nome": "Deposito Compras",
                                  "tipoDeposito": "CENTRAL",
                                  "principal": false,
                                  "permiteVenda": true,
                                  "permiteCompra": true,
                                  "permiteTransferencia": true,
                                  "controlaEstoque": true,
                                  "bloqueado": false
                                }
                                """.formatted(codigoCurto())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarLocalizacao(String token, String depositoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/localizacoes-estoque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "depositoId": "%s",
                                  "codigo": "CMP-%s",
                                  "descricao": "Localizacao compras",
                                  "principal": false,
                                  "bloqueada": false
                                }
                                """.formatted(depositoId, codigoCurto())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarCategoria(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Categoria Compras %s", "descricao": "Categoria compras"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarUnidade(String token) throws Exception {
        String sigla = "C" + codigoCurto().substring(0, 6);
        String response = mockMvc.perform(post("/api/v1/unidades-medida")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sigla": "%s", "descricao": "Unidade compras", "casasDecimais": 0}
                                """.formatted(sigla)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
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
                                  "codigo": "CMP-PROD-%s",
                                  "nome": "Produto Compras",
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
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
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
                                  "estoqueMinimo": 0.000
                                }
                                """.formatted(produtoId, depositoId, localizacaoId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarPedidoCompra(String token, String fornecedorId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pedidos-compra")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fornecedorId": "%s",
                                  "numero": "PC-%s",
                                  "dataEmissao": "2026-07-07",
                                  "dataPrevistaEntrega": "2026-07-10",
                                  "observacao": "Pedido do fluxo de compras"
                                }
                                """.formatted(fornecedorId, codigoCurto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarPedidoCompraItem(String token, String pedidoId, String produtoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pedidos-compra-itens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pedidoCompraId": "%s",
                                  "produtoId": "%s",
                                  "quantidade": 10.000,
                                  "valorUnitario": 12.50,
                                  "observacao": "Item do pedido"
                                }
                                """.formatted(pedidoId, produtoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(125.00))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode criarEntradaMercadoria(String token, String fornecedorId, String pedidoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/entradas-mercadoria")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fornecedorId": "%s",
                                  "pedidoCompraId": "%s",
                                  "numero": "EM-%s",
                                  "dataEntrada": "2026-07-07",
                                  "observacao": "Entrada do fluxo de compras"
                                }
                                """.formatted(fornecedorId, pedidoId, codigoCurto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private void criarEntradaMercadoriaItem(String token, String entradaId, String pedidoItemId, String produtoId,
                                            String depositoId, String localizacaoId) throws Exception {
        criarEntradaMercadoriaItemComQuantidade(token, entradaId, pedidoItemId, produtoId, depositoId, localizacaoId, "10.000");
    }

    private void criarEntradaMercadoriaItemComQuantidade(String token, String entradaId, String pedidoItemId, String produtoId,
                                                         String depositoId, String localizacaoId, String quantidade) throws Exception {
        mockMvc.perform(post("/api/v1/entradas-mercadoria-itens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entradaMercadoriaId": "%s",
                                  "pedidoCompraItemId": "%s",
                                  "produtoId": "%s",
                                  "depositoId": "%s",
                                  "localizacaoId": "%s",
                                  "quantidade": %s,
                                  "valorUnitario": 12.50,
                                  "observacao": "Item recebido"
                                }
                                """.formatted(entradaId, pedidoItemId, produtoId, depositoId, localizacaoId, quantidade)))
                .andExpect(status().isCreated());
    }

    private boolean efetivarSemFalharTeste(String token, String entradaId) {
        try {
            int statusCode = mockMvc.perform(post("/api/v1/entradas-mercadoria/" + entradaId + "/efetivar")
                            .header("Authorization", "Bearer " + token))
                    .andReturn().getResponse().getStatus();
            return statusCode >= 200 && statusCode < 300;
        } catch (Exception ex) {
            return false;
        }
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "78" + "%012d".formatted(parte);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private ContextoTeste criarContexto() {
        Empresa empresa = empresaTestFactory.criarAtiva();
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("COMPRAS:*", "ESTOQUE:*"));
        return new ContextoTeste(token, empresa.getId());
    }

    private record ContextoTeste(String token, UUID empresaId) {
    }
}
