package br.com.invplus.integration.vendas;

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
import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.estoque.service.ReservaEstoqueService;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamentoParcela;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Testes de integracao dos endpoints de faturamento da venda.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VendaFaturamentoControllerTests {

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
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UnidadeMedidaRepository unidadeRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private DepositoRepository depositoRepository;
    @Autowired
    private LocalizacaoEstoqueRepository localizacaoRepository;
    @Autowired
    private EstoqueRepository estoqueRepository;
    @Autowired
    private PedidoVendaRepository pedidoRepository;
    @Autowired
    private ReservaEstoqueService reservaService;
    @Autowired
    private CondicaoPagamentoRepository condicaoPagamentoRepository;

    @Test
    void deveFaturarBuscarListarECancelarFaturamento() throws Exception {
        String token = login().get("accessToken").asString();
        Fixture fixture = criarFixtureComReserva();
        String numero = "VF-" + codigoCurto();
        UUID condicaoPagamentoId = condicaoPagamento().getId();

        String response = mockMvc.perform(post("/api/v1/vendas-faturamentos/faturar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pedidoVendaId":"%s","condicaoPagamentoId":"%s","numero":"%s","observacao":"Faturamento controller"}
                                """.formatted(fixture.pedidoId(), condicaoPagamentoId, numero)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pedidoVendaId").value(fixture.pedidoId().toString()))
                .andExpect(jsonPath("$.status").value("FATURADO"))
                .andExpect(jsonPath("$.valorTotal").value(120.00))
                .andExpect(jsonPath("$.contaReceberId").exists())
                .andReturn().getResponse().getContentAsString();
        JsonNode faturamento = objectMapper.readTree(response);
        String faturamentoId = faturamento.get("id").asString();

        mockMvc.perform(get("/api/v1/vendas-faturamentos/" + faturamentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(faturamentoId));

        mockMvc.perform(get("/api/v1/vendas-faturamentos?pedidoVendaId=" + fixture.pedidoId()
                        + "&empresaId=" + fixture.empresaId()
                        + "&clienteId=" + fixture.clienteId()
                        + "&numero=" + numero
                        + "&status=FATURADO&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(faturamentoId));

        mockMvc.perform(post("/api/v1/vendas-faturamentos/" + faturamentoId + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void deveBloquearPedidoNaoAprovado() throws Exception {
        String token = login().get("accessToken").asString();
        PedidoVenda pedido = criarPedido(StatusPedidoVenda.ABERTO);
        UUID condicaoPagamentoId = condicaoPagamento().getId();

        mockMvc.perform(post("/api/v1/vendas-faturamentos/faturar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pedidoVendaId":"%s","condicaoPagamentoId":"%s","numero":"VF-BLOQ-%s"}
                                """.formatted(pedido.getId(), condicaoPagamentoId, codigoCurto())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Somente pedido aprovado pode ser faturado"));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/vendas-faturamentos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Faturamento",
                "sem-faturamento-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("PEDIDO_VENDA:*")
                .build());

        mockMvc.perform(get("/api/v1/vendas-faturamentos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@invplus.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private Fixture criarFixtureComReserva() {
        PedidoVenda pedido = criarPedido(StatusPedidoVenda.APROVADO);
        Estoque estoque = criarEstoque(pedido.getEmpresa());
        reservaService.criar(new ReservaEstoqueRequest(
                estoque.getId(),
                new BigDecimal("20.000"),
                MovimentacaoOrigem.VENDA,
                pedido.getId(),
                null,
                "Reserva do teste controller"
        ));
        return new Fixture(pedido.getId(), pedido.getEmpresa().getId(), pedido.getCliente().getId());
    }

    private PedidoVenda criarPedido(StatusPedidoVenda status) {
        Empresa empresa = empresa();
        Cliente cliente = cliente();
        PedidoVenda pedido = new PedidoVenda();
        pedido.setEmpresa(empresa);
        pedido.setCliente(cliente);
        pedido.atualizar("PV-FAT-" + codigoCurto(), LocalDate.now(), null, BigDecimal.ZERO, BigDecimal.ZERO, "Pedido");
        pedido.atualizarValorTotal(new BigDecimal("120.00"));
        pedido.setStatus(status);
        return pedidoRepository.saveAndFlush(pedido);
    }

    private Estoque criarEstoque(Empresa empresa) {
        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-" + codigoCurto(), "Deposito Faturamento", "Teste", TipoDeposito.CENTRAL,
                false, true, true, true, true, false);
        deposito = depositoRepository.saveAndFlush(deposito);

        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setDeposito(deposito);
        localizacao.atualizar("LOC-" + codigoCurto(), "Rua A", "Rua A", "01", "01", "01", "01",
                false, false, "Teste");
        localizacao = localizacaoRepository.saveAndFlush(localizacao);

        Estoque estoque = new Estoque();
        estoque.setProduto(produto());
        estoque.setEmpresa(empresa);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(new BigDecimal("100.000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null, null, null);
        return estoqueRepository.saveAndFlush(estoque);
    }

    private Empresa empresa() {
        Empresa empresa = new Empresa();
        String cnpj = cnpjUnico("66");
        empresa.atualizar("Empresa Fat " + cnpj, "Empresa Fat", cnpj, "ISENTO", null,
                "NORMAL", 3, "71999990000", "empresa-fat-" + cnpj + "@teste.com");
        return empresaRepository.saveAndFlush(empresa);
    }

    private Cliente cliente() {
        String cnpj = cnpjUnico("65");
        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente Fat " + cnpj, "Cliente Fat", cnpj, "ISENTO",
                "7133334444", null, "cliente-fat-" + cnpj + "@teste.com", null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.atualizar(BigDecimal.ZERO, null, null, "Cliente faturamento");
        return clienteRepository.saveAndFlush(cliente);
    }

    private Produto produto() {
        Categoria categoria = new Categoria();
        categoria.atualizar(null, "Categoria Fat " + codigoCurto(), "Teste", 0);
        categoria = categoriaRepository.saveAndFlush(categoria);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("F" + codigoCurto().substring(0, 6), "Unidade fat", (short) 0);
        unidade = unidadeRepository.saveAndFlush(unidade);

        Produto produto = new Produto();
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-FAT-" + codigoCurto(), null, null, null, "Produto Fat", "Teste",
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false, true, true, true);
        return produtoRepository.saveAndFlush(produto);
    }

    private CondicaoPagamento condicaoPagamento() {
        CondicaoPagamento condicao = new CondicaoPagamento();
        condicao.atualizar("AV-" + codigoCurto(), "A vista", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                RegraVencimento.MANTER_DATA, true);
        CondicaoPagamentoParcela parcela = new CondicaoPagamentoParcela();
        parcela.atualizar(condicao, 1, 0, new BigDecimal("100.0000"));
        condicao.substituirParcelas(java.util.List.of(parcela));
        return condicaoPagamentoRepository.saveAndFlush(condicao);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String cnpjUnico(String prefixo) {
        return prefixo + "%012d".formatted(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 1_000_000_000_000L);
    }

    private record Fixture(UUID pedidoId, UUID empresaId, UUID clienteId) {
    }
}
