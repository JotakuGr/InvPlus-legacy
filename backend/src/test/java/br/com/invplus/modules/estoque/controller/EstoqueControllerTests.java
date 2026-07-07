package br.com.invplus.modules.estoque.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
 * Testes de integracao dos endpoints de Estoque.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstoqueControllerTests {

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
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        Contexto contexto = criarContexto();

        String criado = mockMvc.perform(post("/api/v1/estoques")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(contexto, "10.000", "2.000", "3.000", "20.000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(contexto.produto.getId().toString()))
                .andExpect(jsonPath("$.quantidade").value(10.000))
                .andExpect(jsonPath("$.quantidadeReservada").value(2.000))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(8.000))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/estoques/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/estoques/saldo?produtoId=" + contexto.produto.getId()
                        + "&empresaId=" + contexto.empresa.getId()
                        + "&depositoId=" + contexto.deposito.getId()
                        + "&localizacaoId=" + contexto.localizacao.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(8.000));

        mockMvc.perform(get("/api/v1/estoques?produtoId=" + contexto.produto.getId()
                        + "&empresaId=" + contexto.empresa.getId()
                        + "&depositoId=" + contexto.deposito.getId()
                        + "&localizacaoId=" + contexto.localizacao.getId()
                        + "&comSaldo=true&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/estoques/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(contexto, "12.000", "2.000", "3.000", "20.000")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeDisponivel").value(10.000));

        mockMvc.perform(delete("/api/v1/estoques/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        String listagem = mockMvc.perform(get("/api/v1/estoques?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(listagem).doesNotContain(id);
    }

    @Test
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        String token = login().get("accessToken").asString();
        Contexto contexto = criarContexto();
        Contexto invalido = new Contexto(produtoComIdAleatorio(), contexto.empresa, contexto.deposito, contexto.localizacao);

        mockMvc.perform(post("/api/v1/estoques")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(invalido, "1.000", "0.000", "0.000", "2.000")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto nao encontrado"));
    }

    @Test
    void deveBloquearLocalizacaoDeOutroDeposito() throws Exception {
        String token = login().get("accessToken").asString();
        Contexto contexto = criarContexto();
        Deposito outroDeposito = criarDeposito(contexto.empresa);
        LocalizacaoEstoque localizacaoOutroDeposito = criarLocalizacao(outroDeposito);
        Contexto invalido = new Contexto(contexto.produto, contexto.empresa, contexto.deposito, localizacaoOutroDeposito);

        mockMvc.perform(post("/api/v1/estoques")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(invalido, "1.000", "0.000", "0.000", "2.000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Localizacao deve pertencer ao deposito informado"));
    }

    @Test
    void deveBloquearDuplicidadeDeSaldo() throws Exception {
        String token = login().get("accessToken").asString();
        Contexto contexto = criarContexto();
        criarEstoque(token, contexto, "A");

        mockMvc.perform(post("/api/v1/estoques")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(contexto, "5.000", "0.000", "0.000", "10.000")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe saldo ativo para este produto nesta empresa, deposito e localizacao"));
    }

    @Test
    void deveListarEstoqueBaixoEZerado() throws Exception {
        String token = login().get("accessToken").asString();
        Contexto contexto = criarContexto();
        String id = criarEstoque(token, contexto, "B");

        mockMvc.perform(get("/api/v1/estoques?estoqueBaixo=true&produtoId=" + contexto.produto.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));
    }

    @Test
    void deveBuscarEstoqueCompletoEDisponivel() throws Exception {
        String token = login().get("accessToken").asString();
        Contexto contexto = criarContexto();
        String estoqueId = criarEstoque(token, contexto, "C");

        mockMvc.perform(post("/api/v1/reservas-estoque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estoqueId": "%s",
                                  "quantidade": 1.000,
                                  "origem": "VENDA",
                                  "origemId": "%s",
                                  "observacao": "Reserva para agregado"
                                }
                                """.formatted(estoqueId, UUID.randomUUID())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/estoques/" + estoqueId + "/completo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoque.id").value(estoqueId))
                .andExpect(jsonPath("$.ultimaMovimentacao.tipoMovimentacao").value("RESERVA"))
                .andExpect(jsonPath("$.reservasAtivas[0].status").value("ATIVA"))
                .andExpect(jsonPath("$.disponibilidade.saldo").value(2.000))
                .andExpect(jsonPath("$.disponibilidade.reservado").value(1.000))
                .andExpect(jsonPath("$.disponibilidade.disponivel").value(1.000));

        mockMvc.perform(get("/api/v1/estoques/disponivel?produtoId=" + contexto.produto.getId()
                        + "&empresaId=" + contexto.empresa.getId()
                        + "&depositoId=" + contexto.deposito.getId()
                        + "&localizacaoId=" + contexto.localizacao.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueId").value(estoqueId))
                .andExpect(jsonPath("$.saldo").value(2.000))
                .andExpect(jsonPath("$.reservado").value(1.000))
                .andExpect(jsonPath("$.disponivel").value(1.000));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/estoques")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Estoque",
                "sem-estoque-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash()).authorities("SEGURANCA:*").build());

        mockMvc.perform(get("/api/v1/estoques").header("Authorization", "Bearer " + token))
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

    private String criarEstoque(String token, Contexto contexto, String sufixo) throws Exception {
        String response = mockMvc.perform(post("/api/v1/estoques")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(contexto, "2.000", "0.000", "3.000", "10.000")))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(Contexto contexto, String quantidade, String reservada, String minimo, String maximo) {
        return """
                {
                  "produtoId": "%s",
                  "empresaId": "%s",
                  "depositoId": "%s",
                  "localizacaoId": "%s",
                  "quantidade": %s,
                  "quantidadeReservada": %s,
                  "quantidadeAvariada": 0.000,
                  "quantidadeTransito": 0.000,
                  "estoqueMinimo": %s,
                  "estoqueMaximo": %s
                }
                """.formatted(contexto.produto.getId(), contexto.empresa.getId(), contexto.deposito.getId(),
                contexto.localizacao.getId(), quantidade, reservada, minimo, maximo);
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
        empresa.atualizar("Empresa Estoque " + UUID.randomUUID(), "Empresa Estoque", cnpjUnico(),
                "ISENTO", null, "NORMAL", 3, "71999990000", "empresa-estoque-" + UUID.randomUUID() + "@teste.com");
        return empresaRepository.saveAndFlush(empresa);
    }

    private Deposito criarDeposito(Empresa empresa) {
        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-" + codigoCurto(), "Deposito Estoque", "Teste",
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
        categoria.atualizar(null, "Categoria Estoque " + UUID.randomUUID(), "Teste", 0);
        categoria = categoriaRepository.saveAndFlush(categoria);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("U" + codigoCurto().substring(0, 6), "Unidade Estoque", (short) 0);
        unidade = unidadeRepository.saveAndFlush(unidade);

        Produto produto = new Produto();
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-" + codigoCurto(), null, null, null, "Produto Estoque", "Teste",
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true);
        return produtoRepository.saveAndFlush(produto);
    }

    private Produto produtoComIdAleatorio() {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        return produto;
    }

    private String cnpjUnico() {
        return "17" + "%012d".formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L));
    }

    private String codigoCurto() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }

    private record Contexto(Produto produto, Empresa empresa, Deposito deposito, LocalizacaoEstoque localizacao) {
    }
}
