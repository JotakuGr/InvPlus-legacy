package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioLancamentoRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioLancamentoService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioService;
import br.com.invplus.modules.financeiro.service.MovimentoFinanceiroService;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
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

/** Testes REST e de seguranca da conciliacao bancaria. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConciliacaoBancariaControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private FormaPagamentoRepository formaPagamentoRepository;
    @Autowired private ContaBancariaService contaBancariaService;
    @Autowired private ExtratoBancarioService extratoService;
    @Autowired private ExtratoBancarioLancamentoService lancamentoService;
    @Autowired private MovimentoFinanceiroService movimentoService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @Test
    void deveCriarBuscarListarFiltrarECancelar() throws Exception {
        String token = login().get("accessToken").asString();
        Cenario cenario = prepararCenario();
        String response = mockMvc.perform(post("/api/v1/conciliacoes-bancarias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(cenario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONCILIADA"))
                .andExpect(jsonPath("$.diferenca").value(0))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/conciliacoes-bancarias/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/conciliacoes-bancarias")
                        .queryParam("extratoBancarioLancamentoId", cenario.lancamentoId().toString())
                        .queryParam("movimentoFinanceiroId", cenario.movimentoId().toString())
                        .queryParam("tipoConciliacao", "MANUAL")
                        .queryParam("status", "CONCILIADA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(post("/api/v1/conciliacoes-bancarias/" + id + "/cancelar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivoCancelamento\":\"Movimento selecionado incorretamente\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/conciliacoes-bancarias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Conciliacao",
                "sem-conciliacao-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash()).authorities("EXTRATO_BANCARIO:VISUALIZAR").build());

        mockMvc.perform(get("/api/v1/conciliacoes-bancarias")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private Cenario prepararCenario() {
        String sufixo = sufixo();
        Empresa empresa = empresaRepository.saveAndFlush(new Empresa("Empresa Conc " + sufixo,
                "Empresa Conc " + sufixo, documento(sufixo), null, null, null));
        FormaPagamento forma = new FormaPagamento();
        forma.atualizar("FC" + sufixo, "Pix Conc " + sufixo, TipoFormaPagamento.PIX, false, false);
        forma = formaPagamentoRepository.saveAndFlush(forma);

        var conta = contaBancariaService.criar(new ContaBancariaRequest(empresa.getId(), null,
                "001", "Banco Conc", "1" + sufixo, null, "9" + sufixo, null,
                TipoContaBancaria.CORRENTE, "Empresa Conc", null, null,
                BigDecimal.ZERO, LocalDate.of(2026, 7, 10), true));
        var extrato = extratoService.importar(new ExtratoBancarioRequest(conta.id(),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), BigDecimal.ZERO,
                new BigDecimal("100.00"), OrigemExtratoBancario.OFX,
                "conc.ofx", hash(sufixo), null));
        var lancamento = lancamentoService.criar(new ExtratoBancarioLancamentoRequest(extrato.id(),
                LocalDate.of(2026, 7, 10), "Credito conciliavel", null,
                TipoLancamentoBancario.CREDITO, new BigDecimal("100.00"), null,
                "CONC-" + sufixo, CategoriaLancamentoBancario.PIX, null));
        var movimento = movimentoService.criarManual(new MovimentoFinanceiroRequest(empresa.getId(), null,
                forma.getId(), null, null, TipoMovimentoFinanceiro.ENTRADA,
                OrigemMovimentoFinanceiro.AJUSTE_MANUAL, null, new BigDecimal("100.00"),
                LocalDate.of(2026, 7, 10), "Entrada conciliavel", null));
        return new Cenario(lancamento.id(), movimento.id());
    }

    private String payload(Cenario cenario) {
        return """
                {
                  "extratoBancarioLancamentoId": "%s",
                  "movimentoFinanceiroId": "%s",
                  "tipoConciliacao": "MANUAL",
                  "observacao": "Conciliacao REST"
                }
                """.formatted(cenario.lancamentoId(), cenario.movimentoId());
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@invplus.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String hash(String valor) {
        return (valor.replaceAll("[^A-Fa-f0-9]", "a")
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").substring(0, 64);
    }

    private String documento(String valor) {
        String numeros = valor.replaceAll("\\D", "");
        return ("12345678000199" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private record Cenario(UUID lancamentoId, UUID movimentoId) {
    }
}
