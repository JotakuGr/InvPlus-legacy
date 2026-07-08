package br.com.invplus.integration.estoque;

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
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import java.math.BigDecimal;
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
 * Testes de integracao dos endpoints somente-leitura de MovimentacaoEstoque.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimentacaoEstoqueControllerTests {

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
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Test
    void deveConsultarMovimentacaoGeradaPeloEstoque() throws Exception {
        String token = login().get("accessToken").asString();
        Contexto contexto = criarContexto();
        JsonNode estoque = criarEstoque(token, contexto, "10.000", "0.000");
        String movimentoId = estoque.get("ultimoMovimentoId").asString();

        mockMvc.perform(get("/api/v1/movimentacoes-estoque/" + movimentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(movimentoId))
                .andExpect(jsonPath("$.estoqueId").value(estoque.get("id").asString()))
                .andExpect(jsonPath("$.produtoId").value(contexto.produto.getId().toString()))
                .andExpect(jsonPath("$.tipoMovimentacao").value("ENTRADA"))
                .andExpect(jsonPath("$.quantidade").value(10.000))
                .andExpect(jsonPath("$.saldoAnterior").value(0.000))
                .andExpect(jsonPath("$.saldoPosterior").value(10.000));

        mockMvc.perform(get("/api/v1/movimentacoes-estoque?estoqueId=" + estoque.get("id").asString()
                        + "&produtoId=" + contexto.produto.getId()
                        + "&empresaId=" + contexto.empresa.getId()
                        + "&depositoId=" + contexto.deposito.getId()
                        + "&localizacaoId=" + contexto.localizacao.getId()
                        + "&tipoMovimentacao=ENTRADA&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(movimentoId));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/movimentacoes-estoque")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Movimentacao",
                "sem-movimentacao-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash()).authorities("SEGURANCA:*").build());

        mockMvc.perform(get("/api/v1/movimentacoes-estoque").header("Authorization", "Bearer " + token))
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

    private JsonNode criarEstoque(String token, Contexto contexto, String quantidade, String reservada) throws Exception {
        String response = mockMvc.perform(post("/api/v1/estoques")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": "%s",
                                  "empresaId": "%s",
                                  "depositoId": "%s",
                                  "localizacaoId": "%s",
                                  "quantidade": %s,
                                  "quantidadeReservada": %s,
                                  "quantidadeAvariada": 0.000,
                                  "quantidadeTransito": 0.000,
                                  "estoqueMinimo": 2.000,
                                  "estoqueMaximo": 50.000
                                }
                                """.formatted(contexto.produto.getId(), contexto.empresa.getId(), contexto.deposito.getId(),
                                contexto.localizacao.getId(), quantidade, reservada)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private Contexto criarContexto() {
        Empresa empresa = criarEmpresa();
        Deposito deposito = criarDeposito(empresa);
        LocalizacaoEstoque localizacao = criarLocalizacao(deposito);
        Produto produto = criarProduto();
        return new Contexto(produto, empresa, deposito, localizacao);
    }

    private Empresa criarEmpresa() {
        Empresa empresa = new Empresa();
        empresa.atualizar("Empresa Mov " + UUID.randomUUID(), "Empresa Mov", cnpjUnico(),
                "ISENTO", null, "NORMAL", 3, "71999990000", "empresa-mov-" + UUID.randomUUID() + "@teste.com");
        return empresaRepository.saveAndFlush(empresa);
    }

    private Deposito criarDeposito(Empresa empresa) {
        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-" + codigoCurto(), "Deposito Mov", "Teste",
                TipoDeposito.CENTRAL, false, true, true, true, true, false);
        return depositoRepository.saveAndFlush(deposito);
    }

    private LocalizacaoEstoque criarLocalizacao(Deposito deposito) {
        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setDeposito(deposito);
        localizacao.atualizar("LOC-" + codigoCurto(), "Rua A", "Rua A", "03", "02", "04", "15", false, false, "Teste");
        return localizacaoRepository.saveAndFlush(localizacao);
    }

    private Produto criarProduto() {
        Categoria categoria = new Categoria();
        categoria.atualizar(null, "Categoria Mov " + UUID.randomUUID(), "Teste", 0);
        categoria = categoriaRepository.saveAndFlush(categoria);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("U" + codigoCurto().substring(0, 6), "Unidade Mov", (short) 0);
        unidade = unidadeRepository.saveAndFlush(unidade);

        Produto produto = new Produto();
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-" + codigoCurto(), null, null, null, "Produto Mov", "Teste",
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true);
        return produtoRepository.saveAndFlush(produto);
    }

    private String cnpjUnico() {
        return "19" + "%012d".formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L));
    }

    private String codigoCurto() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }

    private record Contexto(Produto produto, Empresa empresa, Deposito deposito, LocalizacaoEstoque localizacao) {
    }
}
