package br.com.invplus.integration.financeiro;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioLancamentoService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioService;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import br.com.invplus.support.JwtContextoTestSupport;
import br.com.invplus.support.context.TestEmpresaContext;
import br.com.invplus.support.security.UsuarioTestFactory;
import tools.jackson.databind.ObjectMapper;

/**
 * Testes REST dos lancamentos de extratos bancarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExtratoBancarioLancamentoControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ContaBancariaService contaBancariaService;
    @Autowired
    private ExtratoBancarioService extratoService;
    @Autowired
    private ExtratoBancarioLancamentoService lancamentoService;
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
    @SuppressWarnings("unused")
    void limparContexto() {
        testEmpresaContext.limpar();
    }

    @Test
    void deveCriarListarBuscarAtualizarEExcluirLancamento() throws Exception {
        ExtratoContexto contexto = prepararExtrato();
        String token = contexto.token();
        UUID extratoId = contexto.extratoId();
        String identificador = "BANK-" + sufixo();
        String id = criar(token, extratoId, "CREDITO", identificador);

        mockMvc.perform(get("/api/v1/extratos-bancarios-lancamentos")
                        .queryParam("extratoBancarioId", extratoId.toString())
                        .queryParam("tipo", "CREDITO")
                        .queryParam("categoriaBancaria", "PIX")
                        .queryParam("conciliado", "false")
                        .queryParam("dataLancamentoDe", "2026-07-01")
                        .queryParam("dataLancamentoAte", "2026-07-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(get("/api/v1/extratos-bancarios-lancamentos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CREDITO"))
                .andExpect(jsonPath("$.conciliado").value(false));

        mockMvc.perform(put("/api/v1/extratos-bancarios-lancamentos/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(extratoId, "DEBITO", "BANK-" + sufixo(), "TARIFA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("DEBITO"))
                .andExpect(jsonPath("$.categoriaBancaria").value("TARIFA"));

        mockMvc.perform(delete("/api/v1/extratos-bancarios-lancamentos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/extratos-bancarios-lancamentos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBloquearDuplicidadeValorInvalidoEConciliado() throws Exception {
        ExtratoContexto contexto = prepararExtrato();
        String token = contexto.token();
        UUID extratoId = contexto.extratoId();
        String identificador = "BANK-" + sufixo();
        String id = criar(token, extratoId, "CREDITO", identificador);

        mockMvc.perform(post("/api/v1/extratos-bancarios-lancamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(extratoId, "CREDITO", identificador, "PIX")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/extratos-bancarios-lancamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValor(extratoId, BigDecimal.ZERO, "BANK-" + sufixo())))
                .andExpect(status().isBadRequest());

        lancamentoService.marcarComoConciliado(UUID.fromString(id));

        mockMvc.perform(put("/api/v1/extratos-bancarios-lancamentos/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(extratoId, "CREDITO", "BANK-" + sufixo(), "PIX")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/v1/extratos-bancarios-lancamentos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/extratos-bancarios-lancamentos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Lancamento",
                "sem-lancamento-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        Empresa empresa = novaEmpresa(sufixo());
        String token = JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, usuario, empresa);

        mockMvc.perform(get("/api/v1/extratos-bancarios-lancamentos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String criar(String token, UUID extratoId, String tipo, String identificador) throws Exception {
        String response = mockMvc.perform(post("/api/v1/extratos-bancarios-lancamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(extratoId, tipo, identificador, "PIX")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(UUID extratoId, String tipo, String identificador, String categoria) {
        return """
                {
                  "extratoBancarioId": "%s",
                  "dataLancamento": "2026-07-10",
                  "descricao": "Lancamento bancario",
                  "documento": "DOC-001",
                  "tipo": "%s",
                  "valor": 10.00,
                  "saldoAposLancamento": 100.00,
                  "identificadorBancario": "%s",
                  "categoriaBancaria": "%s",
                  "observacao": "Teste"
                }
                """.formatted(extratoId, tipo, identificador, categoria);
    }

    private String payloadValor(UUID extratoId, BigDecimal valor, String identificador) {
        return """
                {
                  "extratoBancarioId": "%s",
                  "dataLancamento": "2026-07-10",
                  "descricao": "Lancamento bancario",
                  "tipo": "CREDITO",
                  "valor": %s,
                  "identificadorBancario": "%s"
                }
                """.formatted(extratoId, valor.toPlainString(), identificador);
    }

    private ExtratoContexto prepararExtrato() {
        String sufixo = sufixo();
        Empresa empresa = novaEmpresa(sufixo);
        Usuario admin = usuarioTestFactory.criarAtivoComPerfilAdmin();
        String token = JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, admin, empresa);
        testEmpresaContext.ativarEmpresa(admin.getId(), empresa.getId());
        var conta = contaBancariaService.criar(new ContaBancariaRequest(null,
                "001", "Banco Lanc Ctrl", "12" + sufixo.substring(0, 4), "1",
                "98" + sufixo.substring(4), "0", TipoContaBancaria.CORRENTE,
                "Titular Lanc Ctrl", "12345678901", "pix-" + sufixo + "@teste.com",
                BigDecimal.ZERO, LocalDate.of(2026, 7, 9), true));
        UUID extratoId = extratoService.importar(new ExtratoBancarioRequest(conta.id(), LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31), BigDecimal.ZERO, new BigDecimal("1000.00"),
                OrigemExtratoBancario.OFX, "extrato.ofx", hash(sufixo), null)).id();
        return new ExtratoContexto(extratoId, token);
    }

    private Empresa novaEmpresa(String sufixo) {
        return empresaRepository.saveAndFlush(new Empresa("Empresa Lanc Ctrl " + sufixo,
                "Empresa Lanc Ctrl " + sufixo, documento(sufixo), null, null, null));
    }

    private String hash(String sufixo) {
        return (sufixo.replaceAll("[^A-Fa-f0-9]", "a") + "abcdef0123456789"
                + "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc").substring(0, 64);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("12345000000000" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private record ExtratoContexto(UUID extratoId, String token) {
    }
}
