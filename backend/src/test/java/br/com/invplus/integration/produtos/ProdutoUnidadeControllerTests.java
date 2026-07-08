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
 * Testes de integracao dos endpoints de ProdutoUnidade.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoUnidadeControllerTests {

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
     * Cobre criar, buscar, listar com filtros, atualizar, excluir e ocultar registro excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        String unidadeId = criarUnidade(token, "U");

        String criado = mockMvc.perform(post("/api/v1/produtos-unidades")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, unidadeId, "1.000000", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.unidadeMedidaId").value(unidadeId))
                .andExpect(jsonPath("$.unidadePadrao").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/produtos-unidades/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/produtos-unidades?produtoId=" + produtoId
                        + "&unidadeMedidaId=" + unidadeId
                        + "&unidadePadrao=true&permiteCompra=true&permiteVenda=true&permiteEstoque=true&permiteFracionamento=true&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/produtos-unidades/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, unidadeId, "1.000000", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadePadrao").value(true));

        mockMvc.perform(delete("/api/v1/produtos-unidades/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/produtos-unidades/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/produtos-unidades?page=0&size=50")
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
        String unidadeId = criarUnidade(token, "X");

        mockMvc.perform(post("/api/v1/produtos-unidades")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID().toString(), unidadeId, "1.000000", false)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto nao encontrado"));
    }

    /**
     * Unidade inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaUnidadeInexistente() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);

        mockMvc.perform(post("/api/v1/produtos-unidades")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, UUID.randomUUID().toString(), "1.000000", false)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Unidade de medida nao encontrada"));
    }

    /**
     * Produto e unidade nao podem duplicar ativos.
     */
    @Test
    void naoDeveCriarDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        String unidadeId = criarUnidade(token, "D");
        criarProdutoUnidade(token, produtoId, unidadeId, "1.000000", false);

        mockMvc.perform(post("/api/v1/produtos-unidades")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, unidadeId, "1.000000", false)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe unidade ativa para este produto"));
    }

    /**
     * Valida fator zero, fator negativo e padrao diferente de 1.
     */
    @Test
    void deveBloquearFatoresInvalidos() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        String unidadeId = criarUnidade(token, "Z");

        mockMvc.perform(post("/api/v1/produtos-unidades").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(payload(produtoId, unidadeId, "0.000000", false)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/produtos-unidades").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(payload(produtoId, unidadeId, "-1.000000", false)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/produtos-unidades").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(payload(produtoId, unidadeId, "12.000000", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unidade padrao deve ter fator de conversao igual a 1"));
    }

    /**
     * Produto deve ter apenas uma unidade padrao ativa.
     */
    @Test
    void deveBloquearSegundaUnidadePadrao() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        criarProdutoUnidade(token, produtoId, criarUnidade(token, "A"), "1.000000", true);

        mockMvc.perform(post("/api/v1/produtos-unidades")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, criarUnidade(token, "B"), "1.000000", true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Produto ja possui unidade padrao ativa"));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/produtos-unidades")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Produto Unidade",
                "sem-produto-unidade-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash()).authorities("SEGURANCA:*").build());

        mockMvc.perform(get("/api/v1/produtos-unidades").header("Authorization", "Bearer " + token))
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

    private String criarProdutoUnidade(String token, String produtoId, String unidadeId, String fator, Boolean padrao) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-unidades")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, unidadeId, fator, padrao)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarProduto(String token) throws Exception {
        String categoriaId = criarCategoria(token);
        String marcaId = criarMarca(token);
        String unidadeId = criarUnidade(token, "P");
        String codigo = codigoUnico("PROD-UNID");
        String response = mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadProduto(categoriaId, marcaId, unidadeId, codigo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarCategoria(String token) throws Exception {
        String nome = nomeUnico("Categoria Unidade");
        String response = mockMvc.perform(post("/api/v1/categorias").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"descricao\":\"Categoria ProdutoUnidade\"}".formatted(nome)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String nome = nomeUnico("Marca Unidade");
        String response = mockMvc.perform(post("/api/v1/marcas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"descricao\":\"Marca ProdutoUnidade\"}".formatted(nome)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarUnidade(String token, String prefixo) throws Exception {
        String sigla = siglaUnica(prefixo);
        String response = mockMvc.perform(post("/api/v1/unidades-medida").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sigla\":\"%s\",\"descricao\":\"Unidade %s\",\"casasDecimais\":0}".formatted(sigla, sigla)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(String produtoId, String unidadeId, String fator, Boolean padrao) {
        return """
                {
                  "produtoId": "%s",
                  "unidadeMedidaId": "%s",
                  "fatorConversao": %s,
                  "unidadePadrao": %s,
                  "permiteCompra": true,
                  "permiteVenda": true,
                  "permiteEstoque": true,
                  "permiteFracionamento": true,
                  "observacao": "Unidade alternativa"
                }
                """.formatted(produtoId, unidadeId, fator, padrao);
    }

    private String payloadProduto(String categoriaId, String marcaId, String unidadeId, String codigo) {
        String barras = codigoBarrasUnico();
        return """
                {
                  "categoriaId":"%s","marcaId":"%s","unidadeMedidaId":"%s","codigo":"%s","sku":"%s",
                  "codigoInterno":"INT-%s","codigoBarras":"%s","nome":"Produto Unidade","descricao":"Produto unidade",
                  "tipoProduto":"PRODUTO","naturezaProduto":"AMBOS","statusProduto":"ATIVO",
                  "precoCusto":10.0000,"precoVenda":20.0000,"pesoLiquido":1.000,"pesoBruto":1.200,
                  "alturaCm":10.000,"larguraCm":20.000,"comprimentoCm":30.000,"estoqueMinimo":5.000,
                  "controlaLote":false,"controlaValidade":false,"permiteFracionamento":true,
                  "controlaEstoque":true,"permiteCompra":true,"permiteVenda":true
                }
                """.formatted(categoriaId, marcaId, unidadeId, codigo, codigoUnico("SKU-UNID"), codigo, barras);
    }

    private String nomeUnico(String prefixo) {
        return prefixo + " " + Long.toString(Math.abs(System.nanoTime()), 36);
    }

    private String codigoUnico(String prefixo) {
        return prefixo + "-" + Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }

    private String codigoBarrasUnico() {
        return "793" + "%010d".formatted(Math.abs(System.nanoTime() % 10_000_000_000L));
    }

    private String siglaUnica(String prefixo) {
        return prefixo + Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase().substring(0, 5);
    }
}
