package br.com.invplus.modules.produtos.controller;

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
 * Testes de integracao dos endpoints de Produto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoControllerTests {

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
     * Cobre criar, buscar, listar, atualizar, excluir e ocultar produto excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        Referencias referencias = criarReferencias(token);
        String codigo = codigoUnico("PROD");
        String sku = codigoUnico("SKU");
        String barras = codigoBarrasUnico();

        String criado = mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(referencias, codigo, sku, barras, "Produto Controller", "10.00", "20.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.tipoProduto").value("PRODUTO"))
                .andExpect(jsonPath("$.naturezaProduto").value("AMBOS"))
                .andExpect(jsonPath("$.statusProduto").value("ATIVO"))
                .andExpect(jsonPath("$.controlaEstoque").value(true))
                .andExpect(jsonPath("$.permiteCompra").value(true))
                .andExpect(jsonPath("$.permiteVenda").value(true))
                .andExpect(jsonPath("$.categoriaId").value(referencias.categoriaId()))
                .andExpect(jsonPath("$.marcaId").value(referencias.marcaId()))
                .andExpect(jsonPath("$.unidadeMedidaId").value(referencias.unidadeId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/produtos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/produtos?page=0&size=20&sort=nome")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/v1/produtos?nome=Controller&codigo=" + codigo
                        + "&sku=" + sku
                        + "&codigoBarras=" + barras
                        + "&categoriaId=" + referencias.categoriaId()
                        + "&marcaId=" + referencias.marcaId()
                        + "&tipoProduto=PRODUTO"
                        + "&naturezaProduto=AMBOS"
                        + "&statusProduto=ATIVO"
                        + "&controlaEstoque=true"
                        + "&permiteCompra=true"
                        + "&permiteVenda=true"
                        + "&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(get("/api/v1/produtos/search?termo=" + codigo + "&page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/produtos/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(referencias, codigo, sku, barras, "Produto Atualizado", "12.00", "25.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto Atualizado"))
                .andExpect(jsonPath("$.precoVenda").value(25.00));

        mockMvc.perform(delete("/api/v1/produtos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/produtos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/produtos?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Categoria inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaCategoriaInexistente() throws Exception {
        String token = login().get("accessToken").asString();
        Referencias referencias = criarReferencias(token).comCategoria(UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadUnico(referencias)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoria nao encontrada"));
    }

    /**
     * Unidade inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaUnidadeInexistente() throws Exception {
        String token = login().get("accessToken").asString();
        Referencias referencias = criarReferencias(token).comUnidade(UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadUnico(referencias)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Unidade de medida nao encontrada"));
    }

    /**
     * Marca inexistente deve retornar 404 quando informada.
     */
    @Test
    void deveRetornar404ParaMarcaInexistente() throws Exception {
        String token = login().get("accessToken").asString();
        Referencias referencias = criarReferencias(token).comMarca(UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadUnico(referencias)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Marca nao encontrada"));
    }

    /**
     * Codigo duplicado deve retornar 409.
     */
    @Test
    void naoDeveCriarCodigoDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        Referencias referencias = criarReferencias(token);
        String codigo = codigoUnico("PROD");

        criarProduto(token, referencias, codigo, codigoUnico("SKU"), codigoBarrasUnico());

        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(referencias, codigo, codigoUnico("SKU"), codigoBarrasUnico(), "Duplicado", "10.00", "20.00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe produto ativo com este codigo"));
    }

    /**
     * SKU duplicado deve retornar 409.
     */
    @Test
    void naoDeveCriarSkuDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        Referencias referencias = criarReferencias(token);
        String sku = codigoUnico("SKU");

        criarProduto(token, referencias, codigoUnico("PROD"), sku, codigoBarrasUnico());

        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(referencias, codigoUnico("PROD"), sku, codigoBarrasUnico(), "SKU duplicado", "10.00", "20.00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe produto ativo com este SKU"));
    }

    /**
     * Codigo de barras duplicado deve retornar 409.
     */
    @Test
    void naoDeveCriarCodigoBarrasDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        Referencias referencias = criarReferencias(token);
        String barras = codigoBarrasUnico();

        criarProduto(token, referencias, codigoUnico("PROD"), codigoUnico("SKU"), barras);

        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(referencias, codigoUnico("PROD"), codigoUnico("SKU"), barras, "Barras duplicado", "10.00", "20.00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe produto ativo com este codigo de barras"));
    }

    /**
     * Preco negativo deve retornar 400.
     */
    @Test
    void deveRetornar400ParaPrecoNegativo() throws Exception {
        String token = login().get("accessToken").asString();
        Referencias referencias = criarReferencias(token);

        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(referencias, codigoUnico("PROD"), codigoUnico("SKU"), codigoBarrasUnico(), "Negativo", "-1.00", "20.00")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Codigo de barras fora do formato flexivel de GTIN deve retornar 400.
     */
    @Test
    void deveRetornar400ParaCodigoBarrasInvalido() throws Exception {
        String token = login().get("accessToken").asString();
        Referencias referencias = criarReferencias(token);

        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(referencias, codigoUnico("PROD"), codigoUnico("SKU"), "ABC123", "Barras invalido", "10.00", "20.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Codigo de barras deve conter 8, 12, 13 ou 14 digitos numericos"));
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/produtos"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de Produto deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Produto",
                "sem-produto-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/produtos")
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

    private Referencias criarReferencias(String token) throws Exception {
        return new Referencias(criarCategoria(token), criarMarca(token), criarUnidade(token));
    }

    private String criarCategoria(String token) throws Exception {
        String nome = nomeUnico("Categoria Produto");
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Categoria para produto"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String nome = nomeUnico("Marca Produto");
        String response = mockMvc.perform(post("/api/v1/marcas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Marca para produto"
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

    private void criarProduto(String token, Referencias referencias, String codigo, String sku, String barras) throws Exception {
        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(referencias, codigo, sku, barras, "Produto Base", "10.00", "20.00")))
                .andExpect(status().isCreated());
    }

    private String payloadUnico(Referencias referencias) {
        return payload(referencias, codigoUnico("PROD"), codigoUnico("SKU"), codigoBarrasUnico(),
                "Produto Teste", "10.00", "20.00");
    }

    private String payload(Referencias referencias, String codigo, String sku, String barras, String nome, String precoCusto, String precoVenda) {
        return """
                {
                  "categoriaId": "%s",
                  "marcaId": "%s",
                  "unidadeMedidaId": "%s",
                  "codigo": "%s",
                  "sku": "%s",
                  "codigoInterno": "INT-%s",
                  "codigoBarras": "%s",
                  "nome": "%s",
                  "descricao": "Produto para teste",
                  "tipoProduto": "PRODUTO",
                  "naturezaProduto": "AMBOS",
                  "statusProduto": "ATIVO",
                  "precoCusto": %s,
                  "precoVenda": %s,
                  "pesoLiquido": 1.000,
                  "pesoBruto": 1.200,
                  "alturaCm": 10.000,
                  "larguraCm": 20.000,
                  "comprimentoCm": 30.000,
                  "estoqueMinimo": 5.000,
                  "controlaLote": true,
                  "controlaValidade": false,
                  "permiteFracionamento": true,
                  "controlaEstoque": true,
                  "permiteCompra": true,
                  "permiteVenda": true
                }
                """.formatted(referencias.categoriaId(), referencias.marcaId(), referencias.unidadeId(),
                codigo, sku, codigo, barras, nome, precoCusto, precoVenda);
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

    private String siglaUnica() {
        long valor = Math.abs(System.nanoTime());
        return "P" + Long.toString(valor, 36).toUpperCase().substring(0, 5);
    }

    private record Referencias(String categoriaId, String marcaId, String unidadeId) {

        private Referencias comCategoria(String categoriaId) {
            return new Referencias(categoriaId, marcaId, unidadeId);
        }

        private Referencias comMarca(String marcaId) {
            return new Referencias(categoriaId, marcaId, unidadeId);
        }

        private Referencias comUnidade(String unidadeId) {
            return new Referencias(categoriaId, marcaId, unidadeId);
        }
    }
}
