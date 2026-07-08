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
 * Testes de integracao dos endpoints de ProdutoFiscal.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoFiscalControllerTests {

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
     * Cobre CRUD, filtros paginados e soft delete de ProdutoFiscal.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);

        String criado = mockMvc.perform(post("/api/v1/produtos-fiscais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "12345678", "1234567", "Fiscal inicial")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.ncm").value("12345678"))
                .andExpect(jsonPath("$.origemMercadoria").value("NACIONAL"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/produtos-fiscais/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/produtos-fiscais?produtoId=" + produtoId
                        + "&ncm=12345678&cest=1234567&origemMercadoria=NACIONAL&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/produtos-fiscais/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "87654321", null, "Fiscal atualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ncm").value("87654321"))
                .andExpect(jsonPath("$.observacao").value("Fiscal atualizado"));

        mockMvc.perform(delete("/api/v1/produtos-fiscais/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/produtos-fiscais/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/produtos-fiscais?page=0&size=50")
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

        mockMvc.perform(post("/api/v1/produtos-fiscais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID().toString(), "12345678", null, "Produto inexistente")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto nao encontrado"));
    }

    /**
     * Nao permite duas fichas fiscais ativas para o mesmo produto.
     */
    @Test
    void deveRetornar409ParaDuplicidade() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);

        mockMvc.perform(post("/api/v1/produtos-fiscais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "12345678", null, "Fiscal 1")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/produtos-fiscais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "87654321", null, "Fiscal 2")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Produto ja possui dados fiscais ativos"));
    }

    /**
     * NCM invalido deve retornar 400.
     */
    @Test
    void deveRetornar400ParaNcmInvalido() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);

        mockMvc.perform(post("/api/v1/produtos-fiscais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "123", null, "NCM invalido")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("NCM deve conter 8 digitos"));
    }

    /**
     * CEST invalido deve retornar 400.
     */
    @Test
    void deveRetornar400ParaCestInvalido() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);

        mockMvc.perform(post("/api/v1/produtos-fiscais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, "12345678", "123", "CEST invalido")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CEST deve conter 7 digitos"));
    }

    /**
     * Aliquota negativa deve retornar 400.
     */
    @Test
    void deveRetornar400ParaAliquotaNegativa() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);

        mockMvc.perform(post("/api/v1/produtos-fiscais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadComAliquota(produtoId, "-1.0000")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/produtos-fiscais"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario sem permissao fiscal/produto deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Produto Fiscal",
                "sem-produto-fiscal-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/produtos-fiscais")
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

    private String criarProduto(String token) throws Exception {
        String categoriaId = criarCategoria(token);
        String marcaId = criarMarca(token);
        String unidadeId = criarUnidade(token);
        String codigo = codigoUnico("PROD");
        String response = mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": "%s",
                                  "marcaId": "%s",
                                  "unidadeMedidaId": "%s",
                                  "codigo": "%s",
                                  "sku": "%s",
                                  "codigoInterno": "INT-%s",
                                  "codigoBarras": "%s",
                                  "nome": "Produto Fiscal",
                                  "descricao": "Produto para fiscal",
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
                                  "permiteFracionamento": false
                                }
                                """.formatted(categoriaId, marcaId, unidadeId, codigo,
                                codigoUnico("SKU"), codigo, codigoBarrasUnico())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarCategoria(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Categoria para fiscal"
                                }
                                """.formatted(nomeUnico("Categoria Fiscal"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/marcas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Marca para fiscal"
                                }
                                """.formatted(nomeUnico("Marca Fiscal"))))
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

    private String payload(String produtoId, String ncm, String cest, String observacao) {
        String cestLinha = cest == null ? "" : "\"cest\": \"" + cest + "\",";
        return """
                {
                  "produtoId": "%s",
                  "ncm": "%s",
                  %s
                  "origemMercadoria": "NACIONAL",
                  "cfopPadrao": "5102",
                  "cstIcms": "00",
                  "csosn": "102",
                  "cstPis": "01",
                  "cstCofins": "01",
                  "aliquotaIcms": 18.0000,
                  "aliquotaIpi": 0.0000,
                  "aliquotaPis": 1.6500,
                  "aliquotaCofins": 7.6000,
                  "observacao": "%s"
                }
                """.formatted(produtoId, ncm, cestLinha, observacao);
    }

    private String payloadComAliquota(String produtoId, String aliquotaIcms) {
        return """
                {
                  "produtoId": "%s",
                  "ncm": "12345678",
                  "origemMercadoria": "NACIONAL",
                  "aliquotaIcms": %s,
                  "aliquotaIpi": 0.0000,
                  "aliquotaPis": 1.6500,
                  "aliquotaCofins": 7.6000
                }
                """.formatted(produtoId, aliquotaIcms);
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
        return "F" + Long.toString(valor, 36).toUpperCase().substring(0, 5);
    }
}
