package br.com.invplus.integration.financeiro;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import br.com.invplus.support.JwtContextoTestSupport;
import br.com.invplus.support.context.TestEmpresaContext;
import br.com.invplus.support.security.UsuarioTestFactory;
import tools.jackson.databind.ObjectMapper;

/** Testes multipart, consulta e seguranca da importacao OFX. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportacaoExtratoControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ContaBancariaService contaBancariaService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Autowired private UsuarioTestFactory usuarioTestFactory;
    @Autowired private TestEmpresaContext testEmpresaContext;

    @AfterEach
    @SuppressWarnings("unused")
    void limparContexto() {
        testEmpresaContext.limpar();
    }

    @Test
    void deveImportarConsultarEListarOfx() throws Exception {
        JsonNode login = login();
        String token = login.get("accessToken").asString();
        ContaFixture conta = prepararConta(UUID.fromString(login.get("contexto").get("empresaId").asString()));
        MockMultipartFile arquivo = arquivo("extrato-valido.ofx", sufixo(), conta);

        String body = mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx")
                        .file(arquivo).param("contaBancariaId", conta.id().toString())
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
                        .queryParam("contaBancariaId", conta.id().toString())
                        .queryParam("formato", "OFX").queryParam("status", "CONCLUIDA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id));
    }

    @Test
    void deveBloquearArquivoAusenteInvalidoEHashDuplicado() throws Exception {
        JsonNode login = login();
        String token = login.get("accessToken").asString();
        ContaFixture conta = prepararConta(UUID.fromString(login.get("contexto").get("empresaId").asString()));
        MockMultipartFile arquivo = arquivo("extrato-valido.ofx", sufixo(), conta);

        mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx")
                        .param("contaBancariaId", conta.id().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        MockMultipartFile invalido = new MockMultipartFile("arquivo", "extrato.txt", "text/plain",
                "conteudo invalido".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx").file(invalido)
                        .param("contaBancariaId", conta.id().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx").file(arquivo)
                        .param("contaBancariaId", conta.id().toString())
                        .header("Authorization", "Bearer " + token)).andExpect(status().isCreated());
        mockMvc.perform(multipart("/api/v1/extratos-bancarios/importar/ofx").file(arquivo)
                        .param("contaBancariaId", conta.id().toString())
                        .header("Authorization", "Bearer " + token)).andExpect(status().isConflict());
    }

    @Test
    void deveRetornar401SemTokenE403SemPermissao() throws Exception {
        mockMvc.perform(get("/api/v1/importacoes-extratos")).andExpect(status().isUnauthorized());

        Usuario usuario = new Usuario("Sem Importacao",
                "sem-importacao-" + UUID.randomUUID() + "@invplus.com", passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        Empresa empresa = novaEmpresa(sufixo());
        String token = JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, usuario, empresa);
        mockMvc.perform(get("/api/v1/importacoes-extratos")
                        .header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    private ContaFixture prepararConta(UUID empresaId) {
        String numeros = UUID.randomUUID().toString().replaceAll("\\D", "") + "00000000000";
        String banco = "8" + numeros.substring(0, 2);
        String agencia = numeros.substring(2, 6);
        String numero = numeros.substring(6, 11);
        UUID id = contaBancariaService.criar(new ContaBancariaRequest(empresaId, null, banco,
                "Banco OFX Ctrl", agencia, null, numero, "9", TipoContaBancaria.CORRENTE,
                "Empresa OFX Ctrl", null, null, BigDecimal.ZERO, LocalDate.of(2026, 7, 1), false)).id();
        return new ContaFixture(id, banco, agencia, numero + "-9");
    }

    private MockMultipartFile arquivo(String nome, String nonce, ContaFixture conta) throws Exception {
        byte[] bytes;
        try (var stream = getClass().getResourceAsStream("/ofx/" + nome)) {
            bytes = stream.readAllBytes();
        }
        String conteudo = new String(bytes, StandardCharsets.UTF_8)
                .replace("<BANKID>001</BANKID>", "<BANKID>" + conta.banco() + "</BANKID>")
                .replace("<BRANCHID>1234</BRANCHID>", "<BRANCHID>" + conta.agencia() + "</BRANCHID>")
                .replace("<ACCTID>99999-9</ACCTID>", "<ACCTID>" + conta.numero() + "</ACCTID>")
                + "\n<!-- " + nonce + " -->";
        return new MockMultipartFile("arquivo", nome, "application/x-ofx",
                conteudo.getBytes(StandardCharsets.UTF_8));
    }

    private Empresa novaEmpresa(String sufixo) {
        return empresaRepository.saveAndFlush(new Empresa("Empresa OFX Ctrl " + sufixo,
                "Empresa OFX Ctrl " + sufixo, documento(sufixo), null, null, null));
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private record ContaFixture(UUID id, String banco, String agencia, String numero) {
    }
}
