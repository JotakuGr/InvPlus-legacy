package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Testes multipart, consulta e seguranca da importacao OFX. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportacaoExtratoControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ContaBancariaService contaBancariaService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @Test
    void deveImportarConsultarEListarOfx() throws Exception {
        String token = login().get("accessToken").asString();
        UUID contaId = prepararConta();
        MockMultipartFile arquivo = arquivo("extrato-valido.ofx", sufixo());

        String body = mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx")
                        .file(arquivo).param("contaBancariaId", contaId.toString())
                        .param("observacao", "Upload REST")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.quantidadeImportada").value(1))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(body).get("importacaoId").asString();

        mockMvc.perform(get("/api/v1/importacoes-extratos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mockMvc.perform(get("/api/v1/importacoes-extratos")
                        .queryParam("contaBancariaId", contaId.toString())
                        .queryParam("formato", "OFX").queryParam("status", "CONCLUIDA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id));
    }

    @Test
    void deveBloquearArquivoAusenteInvalidoEHashDuplicado() throws Exception {
        String token = login().get("accessToken").asString();
        UUID contaId = prepararConta();
        MockMultipartFile arquivo = arquivo("extrato-valido.ofx", sufixo());

        mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx")
                        .param("contaBancariaId", contaId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        MockMultipartFile invalido = new MockMultipartFile("arquivo", "extrato.txt", "text/plain",
                "conteudo invalido".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx").file(invalido)
                        .param("contaBancariaId", contaId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx").file(arquivo)
                        .param("contaBancariaId", contaId.toString())
                        .header("Authorization", "Bearer " + token)).andExpect(status().isCreated());
        mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx").file(arquivo)
                        .param("contaBancariaId", contaId.toString())
                        .header("Authorization", "Bearer " + token)).andExpect(status().isConflict());
    }

    @Test
    void deveRetornar401SemTokenE403SemPermissao() throws Exception {
        mockMvc.perform(get("/api/v1/importacoes-extratos")).andExpect(status().isUnauthorized());

        Usuario usuario = new Usuario("Sem Importacao",
                "sem-importacao-" + UUID.randomUUID() + "@invplus.com", passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash()).authorities("EXTRATO_BANCARIO:VISUALIZAR").build());
        mockMvc.perform(get("/api/v1/importacoes-extratos")
                        .header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    private UUID prepararConta() {
        String sufixo = sufixo();
        Empresa empresa = empresaRepository.saveAndFlush(new Empresa("Empresa OFX Ctrl " + sufixo,
                "Empresa OFX Ctrl " + sufixo, documento(sufixo), null, null, null));
        return contaBancariaService.criar(new ContaBancariaRequest(empresa.getId(), null, "001",
                "Banco OFX Ctrl", "1234", null, "99999", "9", TipoContaBancaria.CORRENTE,
                "Empresa OFX Ctrl", null, null, BigDecimal.ZERO, LocalDate.of(2026, 7, 1), true)).id();
    }

    private MockMultipartFile arquivo(String nome, String nonce) throws Exception {
        byte[] bytes;
        try (var stream = getClass().getResourceAsStream("/ofx/" + nome)) {
            bytes = stream.readAllBytes();
        }
        String conteudo = new String(bytes, StandardCharsets.UTF_8) + "\n<!-- " + nonce + " -->";
        return new MockMultipartFile("arquivo", nome, "application/x-ofx",
                conteudo.getBytes(StandardCharsets.UTF_8));
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/auth/login").contentType("application/json")
                        .content("{\"email\":\"admin@invplus.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("55666777000199" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
