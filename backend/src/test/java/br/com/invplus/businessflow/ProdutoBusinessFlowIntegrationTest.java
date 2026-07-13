package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.support.security.EmpresaTestFactory;
import br.com.invplus.support.security.UsuarioEmpresaTestFactory;
import br.com.invplus.support.security.UsuarioTestFactory;
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
 * Teste de fluxo de negocio entre Cadastros, Pessoas, Produtos e fiscal basico.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired private UsuarioTestFactory usuarioTestFactory;
    @Autowired private EmpresaTestFactory empresaTestFactory;
    @Autowired private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;

    /**
     * Prova o fluxo completo: Pessoa -> Fornecedor -> Produto -> FornecedorPreco -> ProdutoFiscal.
     */
    @Test
    void deveExecutarFluxoCompletoDoModuloProdutos() throws Exception {
        String token = login().get("accessToken").asString();

        String pessoaId = criarPessoaJuridica(token);
        String fornecedorId = criarFornecedor(token, pessoaId);
        ReferenciasProduto referencias = criarReferenciasProduto(token);
        ProdutoCriado produto = criarProduto(token, referencias);
        String produtoFornecedorId = criarProdutoFornecedor(token, produto.id(), fornecedorId);
        String precoFornecedorId = criarProdutoFornecedorPreco(token, produtoFornecedorId);
        String produtoFiscalId = criarProdutoFiscal(token, produto.id());

        validarPessoaFornecedor(token, pessoaId, fornecedorId);
        validarProduto(token, produto);
        validarProdutoFornecedor(token, produtoFornecedorId, produto.id(), fornecedorId);
        validarProdutoFornecedorPreco(token, precoFornecedorId, produtoFornecedorId, produto.id(), fornecedorId);
        validarProdutoFiscal(token, produtoFiscalId, produto.id());
        validarFiltrosEBuscasPrincipais(token, produto, fornecedorId, produtoFornecedorId, precoFornecedorId, produtoFiscalId);
    }

    private JsonNode login() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(usuario.getEmail(), UsuarioTestFactory.SENHA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private String criarPessoaJuridica(String token) throws Exception {
        String cnpj = cnpjUnico();
        String response = mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoPessoa": "JURIDICA",
                                  "nome": "Fornecedor Fluxo Produtos %s",
                                  "nomeFantasia": "Fluxo Produtos",
                                  "cpfCnpj": "%s",
                                  "rgIe": "ISENTO",
                                  "telefone": "7133334444",
                                  "celular": "71999998888",
                                  "email": "fluxo-produtos-%s@teste.com",
                                  "observacao": "Pessoa juridica criada pelo fluxo de produtos"
                                }
                                """.formatted(cnpj, cnpj, cnpj)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoPessoa").value("JURIDICA"))
                .andExpect(jsonPath("$.cpfCnpj").value(cnpj))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarFornecedor(String token, String pessoaId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": "%s",
                                  "condicaoPagamentoId": "%s",
                                  "prazoMedioEntrega": 5,
                                  "observacao": "Fornecedor do fluxo integrado"
                                }
                                """.formatted(pessoaId, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pessoaId").value(pessoaId))
                .andExpect(jsonPath("$.prazoMedioEntrega").value(5))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private ReferenciasProduto criarReferenciasProduto(String token) throws Exception {
        return new ReferenciasProduto(criarCategoria(token), criarMarca(token), criarUnidade(token));
    }

    private String criarCategoria(String token) throws Exception {
        String nome = nomeUnico("Categoria Fluxo");
        String response = mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Categoria criada pelo fluxo integrado"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nome))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarMarca(String token) throws Exception {
        String nome = nomeUnico("Marca Fluxo");
        String response = mockMvc.perform(post("/api/v1/marcas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Marca criada pelo fluxo integrado"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nome))
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
                .andExpect(jsonPath("$.sigla").value(sigla))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private ProdutoCriado criarProduto(String token, ReferenciasProduto referencias) throws Exception {
        String codigo = codigoUnico("PROD-FLUXO");
        String sku = codigoUnico("SKU-FLUXO");
        String codigoBarras = codigoBarrasUnico();
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
                                  "nome": "Produto Fluxo Integrado",
                                  "descricao": "Produto criado pelo fluxo integrado",
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
                                codigo, sku, codigo, codigoBarras)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.sku").value(sku))
                .andExpect(jsonPath("$.categoriaId").value(referencias.categoriaId()))
                .andExpect(jsonPath("$.marcaId").value(referencias.marcaId()))
                .andExpect(jsonPath("$.unidadeMedidaId").value(referencias.unidadeId()))
                .andExpect(jsonPath("$.statusProduto").value("ATIVO"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return new ProdutoCriado(node.get("id").asString(), codigo, sku, codigoBarras);
    }

    private String criarProdutoFornecedor(String token, String produtoId, String fornecedorId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-fornecedores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": "%s",
                                  "fornecedorId": "%s",
                                  "codigoFornecedor": "FORN-FLUXO-001",
                                  "leadTimeDias": 4,
                                  "principal": true,
                                  "prioridade": 1,
                                  "status": "PREFERENCIAL",
                                  "observacao": "Vinculo criado pelo fluxo integrado"
                                }
                                """.formatted(produtoId, fornecedorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.fornecedorId").value(fornecedorId))
                .andExpect(jsonPath("$.principal").value(true))
                .andExpect(jsonPath("$.status").value("PREFERENCIAL"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarProdutoFornecedorPreco(String token, String produtoFornecedorId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-fornecedores-precos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoFornecedorId": "%s",
                                  "preco": 12.5000,
                                  "vigenciaInicio": "2026-07-03",
                                  "vigenciaFim": null
                                }
                                """.formatted(produtoFornecedorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoFornecedorId").value(produtoFornecedorId))
                .andExpect(jsonPath("$.preco").value(12.5000))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarProdutoFiscal(String token, String produtoId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/produtos-fiscais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": "%s",
                                  "ncm": "12345678",
                                  "cest": "1234567",
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
                                  "observacao": "Fiscal criado pelo fluxo integrado"
                                }
                                """.formatted(produtoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.ncm").value("12345678"))
                .andExpect(jsonPath("$.cest").value("1234567"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private void validarPessoaFornecedor(String token, String pessoaId, String fornecedorId) throws Exception {
        mockMvc.perform(get("/api/v1/pessoas/" + pessoaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pessoaId))
                .andExpect(jsonPath("$.tipoPessoa").value("JURIDICA"));

        mockMvc.perform(get("/api/v1/fornecedores/" + fornecedorId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fornecedorId))
                .andExpect(jsonPath("$.pessoaId").value(pessoaId));
    }

    private void validarProduto(String token, ProdutoCriado produto) throws Exception {
        mockMvc.perform(get("/api/v1/produtos/" + produto.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produto.id()))
                .andExpect(jsonPath("$.codigo").value(produto.codigo()))
                .andExpect(jsonPath("$.sku").value(produto.sku()))
                .andExpect(jsonPath("$.codigoBarras").value(produto.codigoBarras()));
    }

    private void validarProdutoFornecedor(String token, String id, String produtoId, String fornecedorId) throws Exception {
        mockMvc.perform(get("/api/v1/produtos-fornecedores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.fornecedorId").value(fornecedorId));
    }

    private void validarProdutoFornecedorPreco(
            String token,
            String id,
            String produtoFornecedorId,
            String produtoId,
            String fornecedorId
    ) throws Exception {
        mockMvc.perform(get("/api/v1/produtos-fornecedores-precos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.produtoFornecedorId").value(produtoFornecedorId))
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.fornecedorId").value(fornecedorId));
    }

    private void validarProdutoFiscal(String token, String id, String produtoId) throws Exception {
        mockMvc.perform(get("/api/v1/produtos-fiscais/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.ncm").value("12345678"));
    }

    private void validarFiltrosEBuscasPrincipais(
            String token,
            ProdutoCriado produto,
            String fornecedorId,
            String produtoFornecedorId,
            String precoFornecedorId,
            String produtoFiscalId
    ) throws Exception {
        mockMvc.perform(get("/api/v1/produtos/search?termo=" + produto.codigo() + "&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(produto.id()));

        mockMvc.perform(get("/api/v1/produtos?codigo=" + produto.codigo()
                        + "&sku=" + produto.sku()
                        + "&codigoBarras=" + produto.codigoBarras()
                        + "&statusProduto=ATIVO&permiteCompra=true&permiteVenda=true&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(produto.id()));

        mockMvc.perform(get("/api/v1/produtos-fornecedores?produtoId=" + produto.id()
                        + "&fornecedorId=" + fornecedorId
                        + "&principal=true&status=PREFERENCIAL&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(produtoFornecedorId));

        mockMvc.perform(get("/api/v1/produtos-fornecedores-precos?produtoFornecedorId=" + produtoFornecedorId
                        + "&vigenciaInicioDe=2026-07-03&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(precoFornecedorId));

        mockMvc.perform(get("/api/v1/produtos-fiscais?produtoId=" + produto.id()
                        + "&ncm=12345678&cest=1234567&origemMercadoria=NACIONAL&page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(produtoFiscalId));
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

    private record ProdutoCriado(String id, String codigo, String sku, String codigoBarras) {
    }
}
