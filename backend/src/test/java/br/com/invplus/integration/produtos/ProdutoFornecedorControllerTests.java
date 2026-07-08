package br.com.invplus.integration.produtos;

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
 * Testes de integracao dos endpoints de ProdutoFornecedor.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoFornecedorControllerTests {

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
     * Cobre CRUD, filtros por produto/fornecedor/principal/codigo e soft delete.
     */
    @Test
    void deveExecutarCrudCompletoComFiltrosESoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        ReferenciasProduto referenciasProduto = criarReferenciasProduto(token);
        String produtoId = criarProduto(token, referenciasProduto);
        String fornecedorId = criarFornecedor(token, "Fornecedor Produto", "produto-fornecedor@teste.com");

        String criado = mockMvc.perform(post("/api/v1/produtos-fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, fornecedorId, "COD-FORN-001", "7", true, "1", "PREFERENCIAL", "Principal")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.fornecedorId").value(fornecedorId))
                .andExpect(jsonPath("$.codigoFornecedor").value("COD-FORN-001"))
                .andExpect(jsonPath("$.principal").value(true))
                .andExpect(jsonPath("$.prioridade").value(1))
                .andExpect(jsonPath("$.status").value("PREFERENCIAL"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/produtos-fornecedores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/produtos-fornecedores?page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/v1/produtos-fornecedores?produtoId=" + produtoId + "&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(get("/api/v1/produtos-fornecedores?fornecedorId=" + fornecedorId + "&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(get("/api/v1/produtos-fornecedores?principal=true&codigoFornecedor=COD-FORN&status=PREFERENCIAL&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/v1/produtos-fornecedores/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(produtoId, fornecedorId, "COD-FORN-002", "10", true, "2", "HOMOLOGADO", "Atualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoFornecedor").value("COD-FORN-002"))
                .andExpect(jsonPath("$.leadTimeDias").value(10))
                .andExpect(jsonPath("$.prioridade").value(2))
                .andExpect(jsonPath("$.status").value("HOMOLOGADO"));

        mockMvc.perform(delete("/api/v1/produtos-fornecedores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/produtos-fornecedores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/produtos-fornecedores"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de ProdutoFornecedor deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao ProdutoFornecedor",
                "sem-produto-fornecedor-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/produtos-fornecedores")
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

    private String criarFornecedor(String token, String nome, String email) throws Exception {
        String pessoaId = criarPessoa(token, nome, email);
        String response = mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": "%s",
                                  "condicaoPagamentoId": "%s",
                                  "prazoMedioEntrega": 7,
                                  "observacao": "Fornecedor para produto"
                                }
                                """.formatted(pessoaId, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarPessoa(String token, String nome, String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoPessoa": "JURIDICA",
                                  "nome": "%s",
                                  "nomeFantasia": "Fornecedor Produto",
                                  "cpfCnpj": "%s",
                                  "rgIe": "1234567",
                                  "telefone": "7133334444",
                                  "celular": "71999998888",
                                  "email": "%s",
                                  "observacao": "Pessoa para produto fornecedor"
                                }
                                """.formatted(nome, cnpjUnico(), email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private ReferenciasProduto criarReferenciasProduto(String token) throws Exception {
        return new ReferenciasProduto(criarCategoria(token), criarMarca(token), criarUnidade(token));
    }

    private String criarProduto(String token, ReferenciasProduto referencias) throws Exception {
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
                                  "nome": "Produto Fornecedor",
                                  "descricao": "Produto para fornecedor",
                                  "precoCusto": 10.00,
                                  "precoVenda": 20.00,
                                  "pesoLiquido": 1.000,
                                  "pesoBruto": 1.200,
                                  "alturaCm": 10.000,
                                  "larguraCm": 20.000,
                                  "comprimentoCm": 30.000,
                                  "estoqueMinimo": 5.000,
                                  "controlaLote": false,
                                  "controlaValidade": false,
                                  "permiteFracionamento": true
                                }
                                """.formatted(referencias.categoriaId(), referencias.marcaId(), referencias.unidadeId(),
                                codigo, codigoUnico("SKU"), codigo, codigoBarrasUnico())))
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
                                  "descricao": "Categoria para produto fornecedor"
                                }
                                """.formatted(nomeUnico("Categoria ProdutoFornecedor"))))
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
                                  "descricao": "Marca para produto fornecedor"
                                }
                                """.formatted(nomeUnico("Marca ProdutoFornecedor"))))
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

    private String payload(
            String produtoId,
            String fornecedorId,
            String codigoFornecedor,
            String leadTimeDias,
            boolean principal,
            String prioridade,
            String status,
            String observacao
    ) {
        return """
                {
                  "produtoId": "%s",
                  "fornecedorId": "%s",
                  "codigoFornecedor": "%s",
                  "leadTimeDias": %s,
                  "principal": %s,
                  "prioridade": %s,
                  "status": "%s",
                  "observacao": "%s"
                }
                """.formatted(produtoId, fornecedorId, codigoFornecedor, leadTimeDias,
                principal, prioridade, status, observacao);
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

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "13" + "%012d".formatted(parte);
    }

    private record ReferenciasProduto(String categoriaId, String marcaId, String unidadeId) {
    }
}
