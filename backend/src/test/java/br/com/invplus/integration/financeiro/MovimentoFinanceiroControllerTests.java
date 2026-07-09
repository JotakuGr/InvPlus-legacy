package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
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
 * Testes de integracao e seguranca da API de movimentos financeiros.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimentoFinanceiroControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Test
    void deveCriarListarFiltrarBuscarECancelarMovimentoManual() throws Exception {
        String token = login().get("accessToken").asString();
        Empresa empresa = prepararEmpresa();
        FormaPagamento forma = prepararFormaPagamento();

        String response = mockMvc.perform(post("/api/v1/movimentos-financeiros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), forma.getId(), "ENTRADA", "250.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimento").value("ENTRADA"))
                .andExpect(jsonPath("$.origem").value("AJUSTE_MANUAL"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/movimentos-financeiros/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/v1/movimentos-financeiros?empresaId=" + empresa.getId()
                        + "&tipoMovimento=ENTRADA&origem=AJUSTE_MANUAL&page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(post("/api/v1/movimentos-financeiros/" + id + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void deveBloquearValorInvalidoEOrigemAutomaticaManual() throws Exception {
        String token = login().get("accessToken").asString();
        Empresa empresa = prepararEmpresa();
        FormaPagamento forma = prepararFormaPagamento();

        mockMvc.perform(post("/api/v1/movimentos-financeiros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(empresa.getId(), forma.getId(), "ENTRADA", "0.00")))
                .andExpect(status().isBadRequest());

        String automatico = payload(empresa.getId(), forma.getId(), "ENTRADA", "10.00")
                .replace("AJUSTE_MANUAL", "RECEBIMENTO");
        mockMvc.perform(post("/api/v1/movimentos-financeiros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(automatico))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/movimentos-financeiros"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Movimento", "sem-movimento-" + UUID.randomUUID() + "@invplus.com",
                passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = jwtService.gerarToken(User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("PAGAMENTO:VISUALIZAR")
                .build());

        mockMvc.perform(get("/api/v1/movimentos-financeiros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@invplus.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private Empresa prepararEmpresa() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        return empresaRepository.saveAndFlush(new Empresa("Empresa Movimento " + sufixo,
                "Empresa Movimento " + sufixo, documentoNumerico(sufixo), null, null, null));
    }

    private FormaPagamento prepararFormaPagamento() {
        FormaPagamento forma = new FormaPagamento();
        String sufixo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        forma.atualizar("M" + sufixo, "Forma Movimento " + sufixo, TipoFormaPagamento.PIX, false, false);
        return formaPagamentoRepository.saveAndFlush(forma);
    }

    private String payload(UUID empresaId, UUID formaId, String tipo, String valor) {
        return """
                {
                  "empresaId": "%s",
                  "formaPagamentoId": "%s",
                  "tipoMovimento": "%s",
                  "origem": "AJUSTE_MANUAL",
                  "valor": %s,
                  "dataMovimento": "2026-07-08",
                  "descricao": "Ajuste financeiro manual"
                }
                """.formatted(empresaId, formaId, tipo, valor);
    }

    private String documentoNumerico(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("12345678000000" + numeros).substring(numeros.length());
    }
}
