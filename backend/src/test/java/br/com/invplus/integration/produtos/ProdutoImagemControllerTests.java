package br.com.invplus.integration.produtos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integracao dos endpoints de ProdutoImagem.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoImagemControllerTests {

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
     * Cobre criar, buscar, listar com filtros, atualizar, excluir e ocultar imagem excluida.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        String arquivoId = uploadArquivo(token, produtoId, "principal.png");

        String criado = mockMvc.perform(post("/api/v1/produtos-imagens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, arquivoId, true, 0, "Imagem principal")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.arquivoId").value(arquivoId))
                .andExpect(jsonPath("$.principal").value(true))
                .andExpect(jsonPath("$.ordem").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/produtos-imagens/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/produtos-imagens?produtoId=" + produtoId
                        + "&arquivoId=" + arquivoId
                        + "&principal=true&ativo=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/produtos-imagens/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, arquivoId, true, 2, "Imagem atualizada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordem").value(2))
                .andExpect(jsonPath("$.descricao").value("Imagem atualizada"));

        mockMvc.perform(delete("/api/v1/produtos-imagens/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/produtos-imagens/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/produtos-imagens?page=0&size=50")
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
        String produtoId = UUID.randomUUID().toString();
        String arquivoId = uploadArquivo(token, produtoId, "sem-produto.png");

        mockMvc.perform(post("/api/v1/produtos-imagens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, arquivoId, false, 0, "Imagem")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto nao encontrado"));
    }

    /**
     * Arquivo inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaArquivoInexistente() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);

        mockMvc.perform(post("/api/v1/produtos-imagens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, UUID.randomUUID().toString(), false, 0, "Imagem")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Arquivo nao encontrado"));
    }

    /**
     * Produto + Arquivo ativo nao pode duplicar.
     */
    @Test
    void naoDeveCriarProdutoArquivoDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        String arquivoId = uploadArquivo(token, produtoId, "duplicada.png");

        criarProdutoImagem(token, produtoId, arquivoId, false);

        mockMvc.perform(post("/api/v1/produtos-imagens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, arquivoId, false, 1, "Duplicada")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Produto ja possui vinculo ativo com este arquivo"));
    }

    /**
     * Ordem negativa deve retornar 400.
     */
    @Test
    void deveBloquearOrdemNegativa() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        String arquivoId = uploadArquivo(token, produtoId, "ordem.png");

        mockMvc.perform(post("/api/v1/produtos-imagens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, arquivoId, false, -1, "Ordem invalida")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Produto deve ter apenas uma imagem principal ativa.
     */
    @Test
    void deveBloquearSegundaImagemPrincipal() throws Exception {
        String token = login().get("accessToken").asString();
        String produtoId = criarProduto(token);
        String primeira = uploadArquivo(token, produtoId, "principal-1.png");
        String segunda = uploadArquivo(token, produtoId, "principal-2.png");

        criarProdutoImagem(token, produtoId, primeira, true);

        mockMvc.perform(post("/api/v1/produtos-imagens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, segunda, true, 1, "Segunda principal")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Produto ja possui imagem principal ativa"));
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/produtos-imagens"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de ProdutoImagem deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Produto Imagem",
                "sem-produto-imagem-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/produtos-imagens")
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

    private String criarProdutoImagem(String token, String produtoId, String arquivoId, Boolean principal) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-imagens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, arquivoId, principal, 0, "Imagem")))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String uploadArquivo(String token, String produtoId, String nome) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                nome,
                "image/png",
                (nome + UUID.randomUUID()).getBytes()
        );
        String response = mockMvc.perform(multipart("/api/v1/arquivos/upload")
                        .file(file)
                        .param("entidade", "PRODUTO")
                        .param("entidadeId", produtoId)
                        .header("Authorization", "Bearer " + token))
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
        String nome = nomeUnico("Categoria Imagem");
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Categoria para ProdutoImagem"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String nome = nomeUnico("Marca Imagem");
        String response = mockMvc.perform(post("/api/v1/marcas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Marca para ProdutoImagem"
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

    private String payload(String produtoId, String arquivoId, Boolean principal, Integer ordem, String descricao) {
        return """
                {
                  "produtoId": "%s",
                  "arquivoId": "%s",
                  "principal": %s,
                  "ordem": %d,
                  "descricao": "%s"
                }
                """.formatted(produtoId, arquivoId, principal, ordem, descricao);
    }

    private String payloadProduto(Referencias referencias) {
        String codigo = codigoUnico("PROD-IMG");
        String sku = codigoUnico("SKU-IMG");
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
                  "nome": "Produto Imagem",
                  "descricao": "Produto para teste de imagem",
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

    private String siglaUnica() {
        long valor = Math.abs(System.nanoTime());
        return "I" + Long.toString(valor, 36).toUpperCase().substring(0, 5);
    }

    private record Referencias(String categoriaId, String marcaId, String unidadeId) {
    }
}
