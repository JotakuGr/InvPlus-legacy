package br.com.invplus.integration.vendas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoPrecoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoUnidadeRepository;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.PedidoVendaItem;
import br.com.invplus.modules.vendas.repository.PedidoVendaItemRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Prova o fluxo PedidoVenda -> ReservaEstoque -> Estoque -> MovimentacaoEstoque.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoVendaReservaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UnidadeMedidaRepository unidadeMedidaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoPrecoRepository produtoPrecoRepository;

    @Autowired
    private ProdutoUnidadeRepository produtoUnidadeRepository;

    @Autowired
    private DepositoRepository depositoRepository;

    @Autowired
    private LocalizacaoEstoqueRepository localizacaoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private PedidoVendaRepository pedidoRepository;

    @Autowired
    private PedidoVendaItemRepository itemRepository;

    @Autowired
    private ReservaEstoqueRepository reservaRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Test
    void deveAprovarPedidoReservarEstoqueECancelarRestaurandoDisponivel() throws Exception {
        String token = login().get("accessToken").asString();
        Contexto contexto = criarContexto();

        mockMvc.perform(post("/api/v1/pedidos-venda/" + contexto.pedido().getId() + "/aprovar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        Estoque reservado = estoqueRepository.findById(contexto.estoque().getId()).orElseThrow();
        assertThat(reservado.getQuantidade()).isEqualByComparingTo("100.000");
        assertThat(reservado.getQuantidadeReservada()).isEqualByComparingTo("20.000");
        assertThat(reservado.getQuantidadeDisponivel()).isEqualByComparingTo("80.000");

        var reservasAtivas = reservaRepository.findByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA,
                contexto.pedido().getId(),
                StatusReservaEstoque.ATIVA
        );
        assertThat(reservasAtivas).hasSize(1);
        assertThat(reservasAtivas.getFirst().getQuantidade()).isEqualByComparingTo("20.000");

        var movimentacoes = movimentacaoRepository.findByOrigemAndOrigemIdOrderByDataMovimentacaoDesc(
                MovimentacaoOrigem.VENDA,
                contexto.pedido().getId()
        );
        assertThat(movimentacoes).anyMatch(movimentacao ->
                movimentacao.getTipoMovimentacao() == TipoMovimentacaoEstoque.RESERVA);

        mockMvc.perform(get("/api/v1/pedidos-venda/" + contexto.pedido().getId() + "/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.status").value("APROVADO"))
                .andExpect(jsonPath("$.itens[0].produtoId").value(contexto.produto().getId().toString()))
                .andExpect(jsonPath("$.reservas[0].status").value("ATIVA"))
                .andExpect(jsonPath("$.reservas[0].quantidade").value(20.000));

        mockMvc.perform(post("/api/v1/pedidos-venda/" + contexto.pedido().getId() + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        Estoque restaurado = estoqueRepository.findById(contexto.estoque().getId()).orElseThrow();
        assertThat(restaurado.getQuantidade()).isEqualByComparingTo("100.000");
        assertThat(restaurado.getQuantidadeReservada()).isEqualByComparingTo("0.000");
        assertThat(restaurado.getQuantidadeDisponivel()).isEqualByComparingTo("100.000");

        assertThat(reservaRepository.findByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA,
                contexto.pedido().getId(),
                StatusReservaEstoque.ATIVA
        )).isEmpty();
    }

    @Test
    void deveEvitarReservaDuplicadaEmAprovacaoConcorrente() throws Exception {
        String token = login().get("accessToken").asString();
        Contexto contexto = criarContexto();
        CountDownLatch inicio = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);

        try {
            var tarefas = List.of(
                    executor.submit(() -> aprovarConcorrente(token, contexto.pedido().getId(), inicio)),
                    executor.submit(() -> aprovarConcorrente(token, contexto.pedido().getId(), inicio))
            );
            inicio.countDown();
            @SuppressWarnings("UseSpecificCatch")
            List<Integer> statuses = tarefas.stream().map(tarefa -> {
                try {
                    return tarefa.get();
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }).toList();

            assertThat(statuses).contains(200);
            assertThat(statuses).contains(409);
            assertThat(statuses.stream().filter(status -> status == 200).count()).isEqualTo(1);
            assertThat(statuses).doesNotContain(500);

            var reservasAtivas = reservaRepository.findByOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                    MovimentacaoOrigem.VENDA,
                    contexto.pedido().getId(),
                    StatusReservaEstoque.ATIVA
            );
            Estoque estoque = estoqueRepository.findById(contexto.estoque().getId()).orElseThrow();
            assertThat(reservasAtivas).hasSize(1);
            assertThat(estoque.getQuantidadeReservada()).isEqualByComparingTo("20.000");
            assertThat(estoque.getQuantidadeDisponivel()).isEqualByComparingTo("80.000");
        } finally {
            executor.shutdownNow();
        }
    }

    private int aprovarConcorrente(String token, UUID pedidoId, CountDownLatch inicio) throws Exception {
        inicio.await();
        return mockMvc.perform(post("/api/v1/pedidos-venda/" + pedidoId + "/aprovar")
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getStatus();
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

    private Contexto criarContexto() {
        Empresa empresa = criarEmpresa();
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        ProdutoPreco preco = criarPreco(produto);
        ProdutoUnidade unidade = criarProdutoUnidade(produto);
        Deposito deposito = criarDeposito(empresa);
        LocalizacaoEstoque localizacao = criarLocalizacao(deposito);
        Estoque estoque = criarEstoque(empresa, produto, deposito, localizacao);
        PedidoVenda pedido = criarPedido(empresa, cliente);
        criarItem(pedido, produto, preco, unidade);
        return new Contexto(pedido, produto, estoque);
    }

    private Empresa criarEmpresa() {
        Empresa empresa = new Empresa();
        empresa.atualizar("Empresa Reserva Venda " + UUID.randomUUID(), "Empresa Reserva Venda", cnpjUnico(),
                "ISENTO", null, "NORMAL", 3, "71999990000", "empresa-reserva-venda-" + UUID.randomUUID() + "@teste.com");
        return empresaRepository.saveAndFlush(empresa);
    }

    private Cliente criarCliente() {
        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente Reserva Venda", "Cliente Reserva Venda", cnpjUnico(),
                "ISENTO", "7133334444", "71999998888", "cliente-reserva-venda-" + UUID.randomUUID() + "@teste.com", null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);
        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.atualizar(BigDecimal.valueOf(5000), null, null, "Cliente reserva venda");
        return clienteRepository.saveAndFlush(cliente);
    }

    private Produto criarProduto() {
        Categoria categoria = new Categoria();
        categoria.atualizar(null, "Categoria Reserva Venda " + UUID.randomUUID(), "Teste", 0);
        categoria = categoriaRepository.saveAndFlush(categoria);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("R" + codigoCurto().substring(0, 6), "Unidade reserva venda", (short) 0);
        unidade = unidadeMedidaRepository.saveAndFlush(unidade);

        Produto produto = new Produto();
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PRV-" + codigoCurto(), null, null, null, "Produto Reserva Venda", "Teste",
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true);
        return produtoRepository.saveAndFlush(produto);
    }

    private ProdutoPreco criarPreco(Produto produto) {
        ProdutoPreco preco = new ProdutoPreco();
        preco.setProduto(produto);
        preco.atualizar(TipoPrecoProduto.VENDA, BigDecimal.TEN, LocalDate.of(2026, 7, 1), null, true, "Preco venda");
        return produtoPrecoRepository.saveAndFlush(preco);
    }

    private ProdutoUnidade criarProdutoUnidade(Produto produto) {
        ProdutoUnidade unidade = new ProdutoUnidade();
        unidade.setProduto(produto);
        unidade.setUnidadeMedida(produto.getUnidadeMedida());
        unidade.atualizar(BigDecimal.ONE, true, false, true, true, false, "Unidade venda");
        return produtoUnidadeRepository.saveAndFlush(unidade);
    }

    private Deposito criarDeposito(Empresa empresa) {
        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEPV-" + codigoCurto(), "Deposito Reserva Venda", "Teste",
                TipoDeposito.CENTRAL, true, true, true, true, true, false);
        return depositoRepository.saveAndFlush(deposito);
    }

    private LocalizacaoEstoque criarLocalizacao(Deposito deposito) {
        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setDeposito(deposito);
        localizacao.atualizar("LOCV-" + codigoCurto(), "Localizacao venda", "Rua A", "01", "01",
                "01", "01", true, false, "Teste");
        return localizacaoRepository.saveAndFlush(localizacao);
    }

    private Estoque criarEstoque(Empresa empresa, Produto produto, Deposito deposito, LocalizacaoEstoque localizacao) {
        Estoque estoque = new Estoque();
        estoque.setEmpresa(empresa);
        estoque.setProduto(produto);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null, null, null);
        return estoqueRepository.saveAndFlush(estoque);
    }

    private PedidoVenda criarPedido(Empresa empresa, Cliente cliente) {
        PedidoVenda pedido = new PedidoVenda();
        pedido.setEmpresa(empresa);
        pedido.setCliente(cliente);
        pedido.atualizar("PVR-" + codigoCurto(), LocalDate.of(2026, 7, 7), null,
                BigDecimal.ZERO, BigDecimal.ZERO, "Pedido reserva venda");
        return pedidoRepository.saveAndFlush(pedido);
    }

    private void criarItem(PedidoVenda pedido, Produto produto, ProdutoPreco preco, ProdutoUnidade unidade) {
        PedidoVendaItem item = new PedidoVendaItem();
        item.setPedidoVenda(pedido);
        item.setProduto(produto);
        item.setProdutoPreco(preco);
        item.setProdutoUnidade(unidade);
        item.atualizar(BigDecimal.valueOf(20), preco.getPreco(), BigDecimal.ZERO, BigDecimal.ZERO, "Item reserva");
        itemRepository.saveAndFlush(item);
        pedido.atualizarValorTotal(item.getValorTotal());
        pedidoRepository.saveAndFlush(pedido);
    }

    private String cnpjUnico() {
        return "73" + "%012d".formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L));
    }

    private String codigoCurto() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }

    private record Contexto(PedidoVenda pedido, Produto produto, Estoque estoque) {
    }
}
