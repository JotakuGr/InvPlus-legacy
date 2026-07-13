package br.com.invplus.integration.produtos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.modules.cadastros.entity.Empresa;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integracao dos endpoints de ProdutoPreco.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoPrecoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioTestFactory usuarioTestFactory;

    @Autowired
    private EmpresaTestFactory empresaTestFactory;

    @Autowired
    private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;

    @Autowired
    private JwtTestFactory jwtTestFactory;

    /**
     * Cobre criar, buscar, listar com filtros, atualizar, excluir e ocultar preco excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = loginContextual();
        String produtoId = criarProduto(token);

        String criado = mockMvc.perform(post("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "VENDA", "19.9000", "2026-07-01", null, true, "Preco principal")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.tipoPreco").value("VENDA"))
                .andExpect(jsonPath("$.preco").value(19.9000))
                .andExpect(jsonPath("$.principal").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/produtos-precos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/produtos-precos?produtoId=" + produtoId
                        + "&tipoPreco=VENDA&principal=true&ativo=true&vigenciaInicioDe=2026-07-01&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(get("/api/v1/produtos-precos/vigente?produtoId=" + produtoId
                        + "&tipoPreco=VENDA&data=2026-07-01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(put("/api/v1/produtos-precos/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "VENDA", "21.5000", "2026-07-01", null, true, "Preco atualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(21.5000))
                .andExpect(jsonPath("$.observacao").value("Preco atualizado"));

        mockMvc.perform(delete("/api/v1/produtos-precos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/produtos-precos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/produtos-precos?page=0&size=50")
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
        String token = loginContextual();

        mockMvc.perform(post("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID().toString(), "VENDA", "19.9000", "2026-07-01", null, false, "Preco")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto nao encontrado"));
    }

    /**
     * Produto, tipo, empresa e vigencia inicial nao podem duplicar ativos.
     */
    @Test
    void naoDeveCriarPrecoDuplicado() throws Exception {
        String token = loginContextual();
        String produtoId = criarProduto(token);

        criarPreco(token, produtoId, "VENDA", "2026-07-01", false);

        mockMvc.perform(post("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "VENDA", "20.9000", "2026-07-01", null, false, "Duplicado")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe preco ativo para este produto, tipo, empresa e vigencia inicial"));
    }

    /**
     * Produto deve ter apenas um preco principal por tipo e empresa.
     */
    @Test
    void deveBloquearSegundoPrecoPrincipal() throws Exception {
        String token = loginContextual();
        String produtoId = criarProduto(token);

        criarPreco(token, produtoId, "VENDA", "2026-07-01", true);

        mockMvc.perform(post("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "VENDA", "22.9000", "2026-08-01", null, true, "Segundo principal")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Produto ja possui preco principal ativo para este tipo e empresa"));
    }

    /**
     * Preco negativo deve retornar 400.
     */
    @Test
    void deveBloquearPrecoNegativo() throws Exception {
        String token = loginContextual();
        String produtoId = criarProduto(token);

        mockMvc.perform(post("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "VENDA", "-1.0000", "2026-07-01", null, false, "Negativo")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Vigencia final anterior a inicial deve retornar 400.
     */
    @Test
    void deveBloquearVigenciaInvalida() throws Exception {
        String token = loginContextual();
        String produtoId = criarProduto(token);

        mockMvc.perform(post("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "VENDA", "19.9000", "2026-07-10", "2026-07-01", false, "Invalido")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vigencia final nao pode ser anterior a inicial"));
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/produtos-precos"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de ProdutoPreco deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoSemPerfil();
        Empresa empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("SEGURANCA:*"));

        mockMvc.perform(get("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveIsolarPrecosPorEmpresaDoToken() throws Exception {
        ContextoTeste contextoA = criarContexto();
        ContextoTeste contextoB = criarContexto();
        String produtoId = criarProduto(contextoA.accessToken());

        String response = mockMvc.perform(post("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + contextoA.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "VENDA", "31.9000", "2026-09-01", null, false,
                                "Preco empresa A")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empresaId").value(contextoA.empresaId().toString()))
                .andReturn().getResponse().getContentAsString();
        String precoId = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/produtos-precos/" + precoId)
                        .header("Authorization", "Bearer " + contextoB.accessToken()))
                .andExpect(status().isNotFound());

        String listagemB = mockMvc.perform(get("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + contextoB.accessToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(listagemB).doesNotContain(precoId);
    }

    private String loginContextual() throws Exception {
        return criarContexto().accessToken();
    }

    private ContextoTeste criarContexto() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}
                                """.formatted(usuario.getEmail(), UsuarioTestFactory.SENHA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return new ContextoTeste(objectMapper.readTree(response).get("accessToken").asString(), empresa.getId());
    }

    private String criarPreco(String token, String produtoId, String tipoPreco, String vigenciaInicio, Boolean principal) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-precos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, tipoPreco, "19.9000", vigenciaInicio, null, principal, "Preco")))
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
        String nome = nomeUnico("Categoria Preco");
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Categoria para ProdutoPreco"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String nome = nomeUnico("Marca Preco");
        String response = mockMvc.perform(post("/api/v1/marcas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Marca para ProdutoPreco"
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

    private String payload(String produtoId, String tipoPreco, String preco,
                           String vigenciaInicio, String vigenciaFim, Boolean principal, String observacao) {
        return """
                {
                  "produtoId": "%s",
                  "tipoPreco": "%s",
                  "preco": %s,
                  "vigenciaInicio": "%s",
                  "vigenciaFim": %s,
                  "principal": %s,
                  "observacao": "%s"
                }
                """.formatted(produtoId, tipoPreco, preco,
                vigenciaInicio, vigenciaFim == null ? "null" : "\"" + vigenciaFim + "\"", principal, observacao);
    }

    private String payloadProduto(Referencias referencias) {
        String codigo = codigoUnico("PROD-PRECO");
        String sku = codigoUnico("SKU-PRECO");
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
                  "nome": "Produto Preco",
                  "descricao": "Produto para teste de preco",
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
        return "791" + "%010d".formatted(parte);
    }

    private String siglaUnica() {
        long valor = Math.abs(System.nanoTime());
        return "P" + Long.toString(valor, 36).toUpperCase().substring(0, 5);
    }

    private record Referencias(String categoriaId, String marcaId, String unidadeId) {
    }

    private record ContextoTeste(String accessToken, UUID empresaId) {
    }
}
