package br.com.invplus.modules.cadastros.controller;

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
 * Testes de integracao dos endpoints de Arquivo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArquivoControllerTests {

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
     * Cobre criar, buscar, listar paginado, atualizar e excluir logicamente.
     */
    @Test
    void deveExecutarCrudCompletoComSoftDelete() throws Exception {
        String token = login().get("accessToken").asString();
        String entidadeId = UUID.randomUUID().toString();
        String hash = hashUnico();

        String criado = mockMvc.perform(post("/api/v1/arquivos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(hash, entidadeId, "LOCAL")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeOriginal").value("contrato.pdf"))
                .andExpect(jsonPath("$.tipoMime").value("application/pdf"))
                .andExpect(jsonPath("$.storage").value("LOCAL"))
                .andExpect(jsonPath("$.entidade").value("EMPRESA"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(criado).get("id").asString();

        mockMvc.perform(get("/api/v1/arquivos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/arquivos?page=0&size=20&sort=nomeOriginal")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/v1/arquivos?entidade=EMPRESA&entidadeId=" + entidadeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/api/v1/arquivos/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(hash, entidadeId, "MINIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storage").value("MINIO"));

        mockMvc.perform(delete("/api/v1/arquivos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/arquivos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/arquivos?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(id);
    }

    /**
     * Garante unicidade de hash SHA-256 entre arquivos ativos.
     */
    @Test
    void naoDeveCriarHashDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        String hash = hashUnico();

        criarArquivo(token, hash);

        mockMvc.perform(post("/api/v1/arquivos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(hash, UUID.randomUUID().toString(), "LOCAL")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ja existe arquivo ativo com este hash SHA-256"));
    }

    /**
     * Hash fora do formato SHA-256 deve retornar 400.
     */
    @Test
    void deveRetornar400ParaHashInvalido() throws Exception {
        String token = login().get("accessToken").asString();

        mockMvc.perform(post("/api/v1/arquivos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("123", UUID.randomUUID().toString(), "LOCAL")))
                .andExpect(status().isBadRequest());
    }

    /**
     * Upload multipart valido deve gravar metadados de imagem.
     */
    @Test
    void deveFazerUploadDeImagemValida() throws Exception {
        String token = login().get("accessToken").asString();
        String entidadeId = UUID.randomUUID().toString();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "produto.png",
                "image/png",
                ("imagem-" + UUID.randomUUID()).getBytes()
        );

        mockMvc.perform(multipart("/api/v1/arquivos/upload")
                        .file(file)
                        .param("entidade", "PRODUTO")
                        .param("entidadeId", entidadeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeOriginal").value("produto.png"))
                .andExpect(jsonPath("$.tipoMime").value("image/png"))
                .andExpect(jsonPath("$.extensao").value("png"))
                .andExpect(jsonPath("$.storage").value("LOCAL"))
                .andExpect(jsonPath("$.entidade").value("PRODUTO"))
                .andExpect(jsonPath("$.entidadeId").value(entidadeId))
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.containsString("/uploads/")));
    }

    /**
     * Upload vazio deve retornar erro de validacao.
     */
    @Test
    void deveBloquearUploadVazio() throws Exception {
        String token = login().get("accessToken").asString();
        MockMultipartFile file = new MockMultipartFile("file", "vazio.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/v1/arquivos/upload")
                        .file(file)
                        .param("entidade", "PRODUTO")
                        .param("entidadeId", UUID.randomUUID().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Arquivo enviado nao pode ser vazio"));
    }

    /**
     * Upload com tipo MIME fora da lista permitida deve retornar 400.
     */
    @Test
    void deveBloquearUploadComTipoNaoPermitido() throws Exception {
        String token = login().get("accessToken").asString();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.js",
                "application/javascript",
                "alert(1)".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/arquivos/upload")
                        .file(file)
                        .param("entidade", "PRODUTO")
                        .param("entidadeId", UUID.randomUUID().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tipo de arquivo nao permitido"));
    }

    /**
     * Upload maior que 10 MB deve ser recusado pela regra do ArquivoService.
     */
    @Test
    void deveBloquearUploadAcimaDoLimite() throws Exception {
        String token = login().get("accessToken").asString();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "grande.pdf",
                "application/pdf",
                new byte[(10 * 1024 * 1024) + 1]
        );

        mockMvc.perform(multipart("/api/v1/arquivos/upload")
                        .file(file)
                        .param("entidade", "PRODUTO")
                        .param("entidadeId", UUID.randomUUID().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Arquivo excede o tamanho maximo de 10 MB"));
    }

    /**
     * Ausencia de token deve retornar 401.
     */
    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/arquivos"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Usuario autenticado sem permissao de Arquivo deve retornar 403.
     */
    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario(
                "Sem Permissao Arquivo",
                "sem-arquivo-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123")
        );
        usuarioRepository.saveAndFlush(usuario);

        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("SEGURANCA:*")
                .build());

        mockMvc.perform(get("/api/v1/arquivos")
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

    private void criarArquivo(String token, String hash) throws Exception {
        mockMvc.perform(post("/api/v1/arquivos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(hash, UUID.randomUUID().toString(), "LOCAL")))
                .andExpect(status().isCreated());
    }

    private String payload(String hash, String entidadeId, String storage) {
        return """
                {
                  "nomeOriginal": "contrato.pdf",
                  "nomeArmazenado": "contrato-%s.pdf",
                  "tipoMime": "application/pdf",
                  "extensao": "pdf",
                  "tamanhoBytes": 1024,
                  "hashSha256": "%s",
                  "url": "/files/contrato-%s.pdf",
                  "storage": "%s",
                  "entidade": "EMPRESA",
                  "entidadeId": "%s"
                }
                """.formatted(hash.substring(0, Math.min(12, hash.length())), hash,
                hash.substring(0, Math.min(12, hash.length())), storage, entidadeId);
    }

    private String hashUnico() {
        return "%064x".formatted(Math.abs(System.nanoTime()));
    }
}
