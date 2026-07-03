package br.com.invplus.modules.pessoas.controller;

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
 * Testes de integracao dos endpoints de DocumentoPessoa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentoPessoaControllerTests {

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
     * Cobre criar sem arquivo, buscar, listar, atualizar, excluir e ocultar documento excluido.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Pessoa Documento", "pessoa-documento-controller@teste.com");

        String criado = mockMvc.perform(post("/api/v1/documentos-pessoa")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, null, "RG", "RG-" + UUID.randomUUID(), "2026-01-01", "2027-01-01")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pessoaId").value(pessoaId))
                .andExpect(jsonPath("$.nomePessoa").value("Pessoa Documento"))
                .andExpect(jsonPath("$.tipoDocumento").value("RG"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/documentos-pessoa/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/documentos-pessoa?page=0&size=20&sort=criadoEm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/api/v1/documentos-pessoa/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, null, "RG", "RG-ATUALIZADO-" + UUID.randomUUID(), "2026-02-01", "2027-02-01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orgaoEmissor").value("SSP"));

        mockMvc.perform(delete("/api/v1/documentos-pessoa/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/documentos-pessoa/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/documentos-pessoa?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Garante criacao de documento com Arquivo anexado.
     */
    @Test
    void deveCriarDocumentoComArquivo() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Pessoa Documento Arquivo", "pessoa-documento-arquivo@teste.com");
        String arquivoId = criarArquivo(token, pessoaId);

        mockMvc.perform(post("/api/v1/documentos-pessoa")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, arquivoId, "CONTRATO_SOCIAL", "CS-" + UUID.randomUUID(), "2026-01-01", "2027-01-01")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.arquivoId").value(arquivoId))
                .andExpect(jsonPath("$.nomeArquivo").value("documento.pdf"));
    }

    /**
     * Pessoa inexistente deve retornar 404.
     */
    @Test
    void deveRetornar404ParaPessoaInexistente() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/documentos-pessoa")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.randomUUID().toString(), null, "RG", "RG-" + UUID.randomUUID(), "2026-01-01", "2027-01-01")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pessoa nao encontrada"));
    }

    /**
     * Arquivo inexistente deve retornar 404 quando informado.
     */
    @Test
    void deveRetornar404ParaArquivoInexistente() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Pessoa Documento Sem Arquivo", "pessoa-documento-sem-arquivo@teste.com");

        mockMvc.perform(post("/api/v1/documentos-pessoa")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, UUID.randomUUID().toString(), "RG", "RG-" + UUID.randomUUID(), "2026-01-01", "2027-01-01")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Arquivo nao encontrado"));
    }

    /**
     * Duplicidade de tipo e numero para a mesma pessoa deve retornar 409.
     */
    @Test
    void naoDeveCriarDocumentoDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Pessoa Documento Duplicado", "pessoa-documento-duplicado@teste.com");
        String numero = "RG-" + UUID.randomUUID();

        criarDocumento(token, pessoaId, numero);

        mockMvc.perform(post("/api/v1/documentos-pessoa")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, null, "RG", numero, "2026-01-01", "2027-01-01")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Pessoa ja possui documento ativo com este tipo e numero"));
    }

    /**
     * Validade anterior a emissao deve retornar 400.
     */
    @Test
    void deveRetornar400ParaValidadeAnteriorAEmissao() throws Exception {
        String token = login().get("accessToken").asString();
        String pessoaId = criarPessoa(token, "Pessoa Documento Data", "pessoa-documento-data@teste.com");

        mockMvc.perform(post("/api/v1/documentos-pessoa")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, null, "CNH", "CNH-" + UUID.randomUUID(), "2026-07-03", "2026-07-02")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/documentos-pessoa"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de DocumentoPessoa deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Documento",
                "sem-documento-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/documentos-pessoa")
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

    private String criarPessoa(String token, String nome, String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/pessoas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadPessoa(nome, email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private String criarArquivo(String token, String entidadeId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/arquivos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadArquivo(entidadeId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private void criarDocumento(String token, String pessoaId, String numero) throws Exception {
        mockMvc.perform(post("/api/v1/documentos-pessoa")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(pessoaId, null, "RG", numero, "2026-01-01", "2027-01-01")))
                .andExpect(status().isCreated());
    }

    private String payload(String pessoaId, String arquivoId, String tipoDocumento, String numero, String dataEmissao, String dataValidade) {
        String arquivoJson = arquivoId == null ? "null" : "\"" + arquivoId + "\"";
        return """
                {
                  "pessoaId": "%s",
                  "arquivoId": %s,
                  "tipoDocumento": "%s",
                  "numero": "%s",
                  "orgaoEmissor": "SSP",
                  "dataEmissao": "%s",
                  "dataValidade": "%s",
                  "observacao": "Documento cadastral"
                }
                """.formatted(pessoaId, arquivoJson, tipoDocumento, numero, dataEmissao, dataValidade);
    }

    private String payloadPessoa(String nome, String email) {
        String cnpj = cnpjUnico();
        return """
                {
                  "tipoPessoa": "JURIDICA",
                  "nome": "%s",
                  "nomeFantasia": "Pessoa Documento LTDA",
                  "cpfCnpj": "%s",
                  "rgIe": "ISENTO",
                  "telefone": "7133334444",
                  "celular": "71999998888",
                  "email": "%s",
                  "observacao": "Pessoa para documento"
                }
                """.formatted(nome, cnpj, email);
    }

    private String payloadArquivo(String entidadeId) {
        String hash = hashUnico();
        return """
                {
                  "nomeOriginal": "documento.pdf",
                  "nomeArmazenado": "documento-%s.pdf",
                  "tipoMime": "application/pdf",
                  "extensao": "pdf",
                  "tamanhoBytes": 1024,
                  "hashSha256": "%s",
                  "url": "/files/documento-%s.pdf",
                  "storage": "LOCAL",
                  "entidade": "PESSOA",
                  "entidadeId": "%s"
                }
                """.formatted(hash.substring(0, 12), hash, hash.substring(0, 12), entidadeId);
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "17" + "%012d".formatted(parte);
    }

    private String hashUnico() {
        return "%064x".formatted(Math.abs(System.nanoTime()));
    }
}
