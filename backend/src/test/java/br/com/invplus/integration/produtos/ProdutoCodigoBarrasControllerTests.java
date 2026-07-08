package br.com.invplus.integration.produtos;

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
 * Testes de integracao dos endpoints de ProdutoCodigoBarras.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoCodigoBarrasControllerTests {

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
     * Cobre criar, buscar, listar com filtros, atualizar, excluir e ocultar codigo excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        String codigo = codigoNumericoUnico();

        String criado = mockMvc.perform(post("/api/v1/produtos-codigos-barras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "EAN_UNIDADE", codigo, true, "EAN unidade")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.tipoCodigo").value("EAN_UNIDADE"))
                .andExpect(jsonPath("$.principal").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/produtos-codigos-barras/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/produtos-codigos-barras?produtoId=" + produtoId
                        + "&tipoCodigo=EAN_UNIDADE"
                        + "&codigo=" + codigo
                        + "&principal=true&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/produtos-codigos-barras/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "GTIN_LOGISTICO", codigo, true, "GTIN atualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCodigo").value("GTIN_LOGISTICO"))
                .andExpect(jsonPath("$.observacao").value("GTIN atualizado"));

        mockMvc.perform(delete("/api/v1/produtos-codigos-barras/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/produtos-codigos-barras/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/produtos-codigos-barras?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Produto inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/produtos-codigos-barras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID().toString(), "EAN_UNIDADE", codigoNumericoUnico(), false, "Codigo")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto nao encontrado"));
    }

    /**
     * Codigo ativo deve ser unico.
     */
    @Test
    void naoDeveCriarCodigoDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        String codigo = codigoNumericoUnico();

        criarCodigo(token, produtoId, codigo, false);

        mockMvc.perform(post("/api/v1/produtos-codigos-barras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "EAN_CAIXA", codigo, false, "Duplicado")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe codigo de barras ativo com este codigo"));
    }

    /**
     * Produto deve ter apenas um codigo principal ativo.
     */
    @Test
    void deveBloquearSegundoCodigoPrincipal() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);

        criarCodigo(token, produtoId, codigoNumericoUnico(), true);

        mockMvc.perform(post("/api/v1/produtos-codigos-barras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "EAN_CAIXA", codigoNumericoUnico(), true, "Segundo principal")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Produto ja possui codigo de barras principal ativo"));
    }

    /**
     * Codigo vazio deve retornar 400.
     */
    @Test
    void deveBloquearCodigoVazio() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);

        mockMvc.perform(post("/api/v1/produtos-codigos-barras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "EAN_UNIDADE", "", false, "Vazio")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/produtos-codigos-barras"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de ProdutoCodigoBarras deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Produto Codigo",
                "sem-produto-codigo-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/produtos-codigos-barras")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@invplus.com",
                                  "senha": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private String criarCodigo(String token, String produtoId, String codigo, Boolean principal) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-codigos-barras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "EAN_UNIDADE", codigo, principal, "Codigo")))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarProduto(String token) throws Exception {
        Referencias referencias = criarReferencias(token);
        String response = mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadProduto(referencias)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private Referencias criarReferencias(String token) throws Exception {
        return new Referencias(criarCategoria(token), criarMarca(token), criarUnidade(token));
    }

    private String criarCategoria(String token) throws Exception {
        String nome = nomeUnico("Categoria Codigo");
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Categoria para ProdutoCodigoBarras"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String nome = nomeUnico("Marca Codigo");
        String response = mockMvc.perform(post("/api/v1/marcas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Marca para ProdutoCodigoBarras"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarUnidade(String token) throws Exception {
        String sigla = siglaUnica();
        String response = mockMvc.perform(post("/api/v1/unidades-medida")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sigla": "%s",
                                  "descricao": "Unidade %s",
                                  "casasDecimais": 0
                                }
                                """.formatted(sigla, sigla)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(String produtoId, String tipoCodigo, String codigo, Boolean principal, String observacao) {
        return """
                {
                  "produtoId": "%s",
                  "tipoCodigo": "%s",
                  "codigo": "%s",
                  "principal": %s,
                  "observacao": "%s"
                }
                """.formatted(produtoId, tipoCodigo, codigo, principal, observacao);
    }

    private String payloadProduto(Referencias referencias) {
        String codigo = codigoUnico("PROD-COD");
        String sku = codigoUnico("SKU-COD");
        String barras = codigoBarrasUnico();
        return """
                {
                  "categoriaId": "%s",
                  "marcaId": "%s",
                  "unidadeMedidaId": "%s",
                  "codigo": "%s",
                  "sku": "%s",
                  "codigoInterno": "INT-%s",
                  "codigoBarras": "%s",
                  "nome": "Produto Codigo",
                  "descricao": "Produto para teste de codigo",
                  "tipoProduto": "PRODUTO",
                  "naturezaProduto": "AMBOS",
                  "statusProduto": "ATIVO",
                  "precoCusto": 10.0000,
                  "precoVenda": 20.0000,
                  "pesoLiquido": 1.000,
                  "pesoBruto": 1.200,
                  "alturaCm": 10.000,
                  "larguraCm": 20.000,
                  "comprimentoCm": 30.000,
                  "estoqueMinimo": 5.000,
                  "controlaLote": false,
                  "controlaValidade": false,
                  "permiteFracionamento": true,
                  "controlaEstoque": true,
                  "permiteCompra": true,
                  "permiteVenda": true
                }
                """.formatted(referencias.categoriaId(), referencias.marcaId(), referencias.unidadeId(),
                codigo, sku, codigo, barras);
    }

    private String nomeUnico(String prefixo) {
        return prefixo + " " + Long.toString(Math.abs(System.nanoTime()), 36);
    }

    private String codigoUnico(String prefixo) {
        return prefixo + "-" + Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }

    private String codigoBarrasUnico() {
        long parte = Math.abs(System.nanoTime() % 10_000_000_000L);
        return "789" + "%010d".formatted(parte);
    }

    private String codigoNumericoUnico() {
        long parte = Math.abs(System.nanoTime() % 10_000_000_000L);
        return "790" + "%010d".formatted(parte);
    }

    private String siglaUnica() {
        long valor = Math.abs(System.nanoTime());
        return "B" + Long.toString(valor, 36).toUpperCase().substring(0, 5);
    }

    private record Referencias(String categoriaId, String marcaId, String unidadeId) {
    }
}
