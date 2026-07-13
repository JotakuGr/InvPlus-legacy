package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.service.JwtService;
import br.com.invplus.support.JwtContextoTestSupport;
import br.com.invplus.support.context.TestEmpresaContext;
import br.com.invplus.support.security.UsuarioTestFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Testes REST de importacao e cancelamento de extratos bancarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExtratoBancarioControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ContaBancariaService contaBancariaService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Autowired
    private UsuarioTestFactory usuarioTestFactory;
    @Autowired
    private TestEmpresaContext testEmpresaContext;

    @AfterEach
    void limparContexto() {
        testEmpresaContext.limpar();
    }

    @Test
    void deveImportarListarBuscarECancelarExtrato() throws Exception {
        ContaContexto contexto = prepararContaBancaria();
        String token = contexto.token();
        UUID contaId = contexto.contaId();
        String hash = hash(sufixo());

        String id = importar(token, contaId, hash);

        mockMvc.perform(get("/api/v1/extratos-bancarios")
                        .queryParam("contaBancariaId", contaId.toString())
                        .queryParam("origem", "OFX")
                        .queryParam("status", "IMPORTADO")
                        .queryParam("dataInicioDe", "2026-07-01")
                        .queryParam("dataFimAte", "2026-07-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(get("/api/v1/extratos-bancarios/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IMPORTADO"))
                .andExpect(jsonPath("$.arquivoHash").value(hash.toLowerCase()));

        mockMvc.perform(post("/api/v1/extratos-bancarios/" + id + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.ativo").value(true));

        mockMvc.perform(post("/api/v1/extratos-bancarios/" + id + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBloquearHashDuplicadoInvalidoEPeriodoInvalido() throws Exception {
        ContaContexto contexto = prepararContaBancaria();
        String token = contexto.token();
        UUID contaId = contexto.contaId();
        String hash = hash(sufixo());
        importar(token, contaId, hash);

        mockMvc.perform(post("/api/v1/extratos-bancarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(contaId, hash, "2026-07-01", "2026-07-31")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/extratos-bancarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(contaId, "hash-invalido", "2026-07-01", "2026-07-31")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/extratos-bancarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(contaId, hash(sufixo()), "2026-08-01", "2026-07-31")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/extratos-bancarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Extrato",
                "sem-extrato-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        Empresa empresa = novaEmpresa(sufixo());
        String token = JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, usuario, empresa);

        mockMvc.perform(get("/api/v1/extratos-bancarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String importar(String token, UUID contaId, String hash) throws Exception {
        String response = mockMvc.perform(post("/api/v1/extratos-bancarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(contaId, hash, "2026-07-01", "2026-07-31")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(UUID contaId, String hash, String dataInicio, String dataFim) {
        return """
                {
                  "contaBancariaId": "%s",
                  "dataInicio": "%s",
                  "dataFim": "%s",
                  "saldoInicial": 0.00,
                  "saldoFinal": 1000.00,
                  "origem": "OFX",
                  "arquivoNome": "extrato.ofx",
                  "arquivoHash": "%s",
                  "observacao": "Importacao via teste"
                }
                """.formatted(contaId, dataInicio, dataFim, hash);
    }

    private ContaContexto prepararContaBancaria() {
        String sufixo = sufixo();
        Empresa empresa = novaEmpresa(sufixo);
        Usuario admin = usuarioTestFactory.criarAtivoComPerfilAdmin();
        String token = JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, admin, empresa);
        testEmpresaContext.ativarEmpresa(admin.getId(), empresa.getId());
        UUID contaId = contaBancariaService.criar(new ContaBancariaRequest(null,
                "001", "Banco Extrato", "12" + sufixo.substring(0, 4), "1",
                "98" + sufixo.substring(4), "0", TipoContaBancaria.CORRENTE,
                "Titular Extrato", "12345678901", "pix-" + sufixo + "@teste.com",
                BigDecimal.ZERO, LocalDate.of(2026, 7, 9), true)).id();
        return new ContaContexto(contaId, token);
    }

    private Empresa novaEmpresa(String sufixo) {
        return empresaRepository.saveAndFlush(new Empresa("Empresa EB " + sufixo,
                "Empresa EB " + sufixo, documento(sufixo), null, null, null));
    }

    private String hash(String prefixo) {
        return (prefixo.replaceAll("[^A-Fa-f0-9]", "a") + "abcdef0123456789"
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").substring(0, 64);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("87654300000000" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private record ContaContexto(UUID contaId, String token) {
    }
}
