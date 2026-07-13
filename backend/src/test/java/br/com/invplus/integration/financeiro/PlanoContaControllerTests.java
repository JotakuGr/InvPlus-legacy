package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.service.JwtService;
import br.com.invplus.support.JwtContextoTestSupport;
import br.com.invplus.support.security.UsuarioTestFactory;
import java.util.UUID;
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
 * Testes REST do cadastro hierarquico do plano de contas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlanoContaControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaRepository empresaRepository;
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
    private PlanoContaRepository planoContaRepository;

    @Test
    void deveCriarFiltrarAtualizarEExcluirPlanoConta() throws Exception {
        Empresa empresa = prepararEmpresa();
        String token = tokenAdmin(empresa);
        String paiId = criar(token, null, "3-" + sufixo(),
                "Receitas", "SINTETICA");
        String filhoId = criar(token, UUID.fromString(paiId),
                "3.1-" + sufixo(), "Vendas", "ANALITICA");

        mockMvc.perform(get("/api/v1/planos-contas")
                        .queryParam("planoContaPaiId", paiId)
                        .queryParam("tipo", "ANALITICA")
                        .queryParam("natureza", "RECEITA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(filhoId))
                .andExpect(jsonPath("$.content[0].caminhoCompleto").value("Receitas > Vendas"));

        mockMvc.perform(put("/api/v1/planos-contas/" + filhoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.fromString(paiId),
                                "3.1.1-" + sufixo(), "Venda de Produtos", "ANALITICA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Venda de Produtos"));

        mockMvc.perform(delete("/api/v1/planos-contas/" + filhoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/planos-contas/" + filhoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBloquearDuplicidadePaiDeOutraEmpresaEAutorelacionamento() throws Exception {
        Empresa empresa = prepararEmpresa();
        String token = tokenAdmin(empresa);
        Empresa outra = prepararEmpresa();
        String codigo = "DUP-" + sufixo();
        String id = criar(token, null, codigo, "Receitas", "SINTETICA");
        PlanoConta paiOutraEmpresa = new PlanoConta();
        paiOutraEmpresa.atualizar(outra, null, "OUT-" + sufixo(), "Ativos", null,
                TipoPlanoConta.SINTETICA, NaturezaPlanoConta.ATIVO);
        paiOutraEmpresa = planoContaRepository.saveAndFlush(paiOutraEmpresa);

        mockMvc.perform(post("/api/v1/planos-contas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null, codigo, "Duplicado", "ANALITICA")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/planos-contas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(paiOutraEmpresa.getId(),
                                "FILHO-" + sufixo(), "Filho invalido", "ANALITICA")))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/v1/planos-contas/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.fromString(id),
                                codigo, "Receitas", "SINTETICA")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/planos-contas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Plano Conta",
                "sem-plano-" + UUID.randomUUID() + "@invplus.com", passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, usuario, prepararEmpresa());

        mockMvc.perform(get("/api/v1/planos-contas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String criar(String token, UUID paiId,
            String codigo, String nome, String tipo) throws Exception {
        String response = mockMvc.perform(post("/api/v1/planos-contas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(paiId, codigo, nome, tipo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(UUID paiId, String codigo, String nome, String tipo) {
        String pai = paiId == null ? "null" : "\"" + paiId + "\"";
        return """
                {
                  "planoContaPaiId": %s,
                  "codigo": "%s",
                  "nome": "%s",
                  "descricao": "Plano de conta de teste",
                  "tipo": "%s",
                  "natureza": "RECEITA"
                }
                """.formatted(pai, codigo, nome, tipo);
    }

    private Empresa prepararEmpresa() {
        String sufixo = sufixo();
        return empresaRepository.saveAndFlush(new Empresa("Empresa PC " + sufixo,
                "Empresa PC " + sufixo, documento(sufixo), null, null, null));
    }

    private String tokenAdmin(Empresa empresa) {
        Usuario admin = usuarioTestFactory.criarAtivoComPerfilAdmin();
        return JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, admin, empresa);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("76543210000000" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
