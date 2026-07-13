package br.com.invplus.integration.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.support.security.JwtTestFactory;
import br.com.invplus.support.security.UsuarioEmpresaTestFactory;
import br.com.invplus.support.security.UsuarioTestFactory;
import java.math.BigDecimal;
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
 * Testes de integracao dos endpoints de ReservaEstoque.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaEstoqueControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UnidadeMedidaRepository unidadeRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private DepositoRepository depositoRepository;
    @Autowired
    private LocalizacaoEstoqueRepository localizacaoRepository;
    @Autowired
    private EstoqueRepository estoqueRepository;
    @Autowired
    private UsuarioTestFactory usuarioTestFactory;
    @Autowired
    private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;
    @Autowired
    private JwtTestFactory jwtTestFactory;

    @Test
    void deveCriarCancelarEBaixarReservasAtualizandoSaldo() throws Exception {
        Estoque estoque = criarContextoEstoque();
        String token = token(estoque.getEmpresa(), java.util.Set.of("ESTOQUE:*"));

        String reservaCancelada = criarReserva(token, estoque.getId(), "20.000", UUID.randomUUID())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.quantidade").value(20.000))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String reservaCanceladaId = objectMapper.readTree(reservaCancelada).get("id").asString();

        assertSaldo(estoque.getId(), "100.000", "20.000", "80.000");

        mockMvc.perform(post("/api/v1/reservas-estoque/" + reservaCanceladaId + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.canceladaEm").exists());

        assertSaldo(estoque.getId(), "100.000", "0.000", "100.000");

        String reservaBaixada = criarReserva(token, estoque.getId(), "20.000", UUID.randomUUID())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String reservaBaixadaId = objectMapper.readTree(reservaBaixada).get("id").asString();

        mockMvc.perform(post("/api/v1/reservas-estoque/" + reservaBaixadaId + "/baixar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BAIXADA"))
                .andExpect(jsonPath("$.baixadaEm").exists());

        assertSaldo(estoque.getId(), "80.000", "0.000", "80.000");
    }

    @Test
    void deveBloquearReservaAcimaDoDisponivelEQuantidadeInvalida() throws Exception {
        Estoque estoque = criarContextoEstoque();
        String token = token(estoque.getEmpresa(), java.util.Set.of("ESTOQUE:*"));

        criarReserva(token, estoque.getId(), "120.000", UUID.randomUUID())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantidade da reserva nao pode ser maior que a quantidade disponivel"));

        criarReserva(token, estoque.getId(), "0.000", UUID.randomUUID())
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBloquearReservaDuplicadaParaMesmaOrigemAtiva() throws Exception {
        Estoque estoque = criarContextoEstoque();
        String token = token(estoque.getEmpresa(), java.util.Set.of("ESTOQUE:*"));
        UUID origemId = UUID.randomUUID();
        criarReserva(token, estoque.getId(), "5.000", origemId).andExpect(status().isCreated());

        criarReserva(token, estoque.getId(), "5.000", origemId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe reserva ativa para esta origem"));
    }

    @Test
    void deveBloquearTransicoesInvalidas() throws Exception {
        Estoque estoque = criarContextoEstoque();
        String token = token(estoque.getEmpresa(), java.util.Set.of("ESTOQUE:*"));
        String cancelada = criarReserva(token, estoque.getId(), "5.000", UUID.randomUUID())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String canceladaId = objectMapper.readTree(cancelada).get("id").asString();
        mockMvc.perform(post("/api/v1/reservas-estoque/" + canceladaId + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/reservas-estoque/" + canceladaId + "/baixar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Reserva cancelada nao pode ser baixada"));

        String baixada = criarReserva(token, estoque.getId(), "5.000", UUID.randomUUID())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String baixadaId = objectMapper.readTree(baixada).get("id").asString();
        mockMvc.perform(post("/api/v1/reservas-estoque/" + baixadaId + "/baixar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/reservas-estoque/" + baixadaId + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Somente reserva ativa pode ser cancelada"));
    }

    @Test
    void deveListarComFiltros() throws Exception {
        Estoque estoque = criarContextoEstoque();
        String token = token(estoque.getEmpresa(), java.util.Set.of("ESTOQUE:*"));
        UUID origemId = UUID.randomUUID();
        String response = criarReserva(token, estoque.getId(), "5.000", origemId)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/reservas-estoque?estoqueId=" + estoque.getId()
                        + "&produtoId=" + estoque.getProduto().getId()
                        + "&depositoId=" + estoque.getDeposito().getId()
                        + "&localizacaoId=" + estoque.getLocalizacao().getId()
                        + "&status=ATIVA&origem=VENDA&origemId=" + origemId
                        + "&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/reservas-estoque")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Empresa empresa = new Empresa();
        empresa.atualizar("Empresa Sem Reserva " + UUID.randomUUID(), "Empresa", cnpjUnico(), "ISENTO", null,
                "NORMAL", 3, "71999990000", "sem-reserva-" + UUID.randomUUID() + "@teste.com");
        empresa = empresaRepository.saveAndFlush(empresa);
        String token = token(empresa, java.util.Set.of("SEGURANCA:*"));

        mockMvc.perform(get("/api/v1/reservas-estoque").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveIsolarReservaPorEmpresaDoToken() throws Exception {
        Estoque estoqueA = criarContextoEstoque();
        Estoque estoqueB = criarContextoEstoque();
        String tokenA = token(estoqueA.getEmpresa(), java.util.Set.of("ESTOQUE:*"));
        String tokenB = token(estoqueB.getEmpresa(), java.util.Set.of("ESTOQUE:*"));
        String response = criarReserva(tokenA, estoqueA.getId(), "5.000", UUID.randomUUID())
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String reservaId = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/reservas-estoque/" + reservaId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/reservas-estoque?page=0&size=50")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(listagem).doesNotContain(reservaId);
    }

    private org.springframework.test.web.servlet.ResultActions criarReserva(
            String token,
            UUID estoqueId,
            String quantidade,
            UUID origemId
    ) throws Exception {
        return mockMvc.perform(post("/api/v1/reservas-estoque")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "estoqueId": "%s",
                          "quantidade": %s,
                          "origem": "VENDA",
                          "origemId": "%s",
                          "observacao": "Reserva controller"
                        }
                        """.formatted(estoqueId, quantidade, origemId)));
    }

    private void assertSaldo(UUID estoqueId, String quantidade, String reservada, String disponivel) {
        Estoque estoque = estoqueRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(estoqueId).orElseThrow();
        assertThat(estoque.getQuantidade()).isEqualByComparingTo(quantidade);
        assertThat(estoque.getQuantidadeReservada()).isEqualByComparingTo(reservada);
        assertThat(estoque.getQuantidadeDisponivel()).isEqualByComparingTo(disponivel);
    }

    private Estoque criarContextoEstoque() {
        Empresa empresa = new Empresa();
        empresa.atualizar("Empresa Reserva " + UUID.randomUUID(), "Empresa", cnpjUnico(), "ISENTO", null,
                "NORMAL", 3, "71999990000", "empresa-reserva-" + UUID.randomUUID() + "@teste.com");
        empresa = empresaRepository.saveAndFlush(empresa);

        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-" + codigoCurto(), "Deposito Reserva", "Teste", TipoDeposito.CENTRAL,
                false, true, true, true, true, false);
        deposito = depositoRepository.saveAndFlush(deposito);

        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setDeposito(deposito);
        localizacao.atualizar("LOC-" + codigoCurto(), "Rua A", "Rua A", "03", "02", "04", "15",
                false, false, "Teste");
        localizacao = localizacaoRepository.saveAndFlush(localizacao);

        Produto produto = produtoRepository.saveAndFlush(produto());

        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setEmpresa(empresa);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(bd("100.000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null, null, null);
        return estoqueRepository.saveAndFlush(estoque);
    }

    private Produto produto() {
        Categoria categoria = new Categoria();
        categoria.atualizar(null, "Categoria Reserva " + UUID.randomUUID(), "Teste", 0);
        categoria = categoriaRepository.saveAndFlush(categoria);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("U" + codigoCurto().substring(0, 6), "Unidade reserva", (short) 0);
        unidade = unidadeRepository.saveAndFlush(unidade);

        Produto produto = new Produto();
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-" + codigoCurto(), null, null, null, "Produto Reserva", "Teste",
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false, true, true, true);
        return produto;
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String cnpjUnico() {
        return "8%013d".formatted(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10_000_000_000_000L);
    }

    private BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    private String token(Empresa empresa, java.util.Set<String> permissoes) {
        Usuario usuario = permissoes.contains("ESTOQUE:*")
                ? usuarioTestFactory.criarAtivoComPerfilAdmin()
                : usuarioTestFactory.criarAtivoSemPerfil();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        return jwtTestFactory.accessToken(usuario.getId(), usuario.getEmail(), empresa.getId(), null, permissoes);
    }
}
