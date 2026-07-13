package br.com.invplus.integration.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
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
import tools.jackson.databind.ObjectMapper;

/**
 * Testes REST do cadastro hierarquico de centros de custo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CentroCustoControllerTests {

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
    private CentroCustoRepository centroCustoRepository;

    @Test
    void deveCriarFiltrarAtualizarEExcluirCentroCusto() throws Exception {
        Empresa empresa = prepararEmpresa();
        String token = tokenAdmin(empresa);
        String paiId = criar(token, null, "ADM-" + sufixo(), "Administrativo");
        String filhoId = criar(token, UUID.fromString(paiId),
                "MKT-" + sufixo(), "Marketing");

        mockMvc.perform(get("/api/v1/centros-custo")
                        .queryParam("centroCustoPaiId", paiId)
                        .queryParam("tipo", "AMBOS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(filhoId))
                .andExpect(jsonPath("$.content[0].caminhoCompleto")
                        .value("Administrativo > Marketing"));

        mockMvc.perform(put("/api/v1/centros-custo/" + filhoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.fromString(paiId),
                                "MKT-EDIT-" + sufixo(), "Marketing Digital")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Marketing Digital"));

        mockMvc.perform(delete("/api/v1/centros-custo/" + filhoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/centros-custo/" + filhoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBloquearDuplicidadePaiDeOutraEmpresaEAutorelacionamento() throws Exception {
        Empresa empresa = prepararEmpresa();
        String token = tokenAdmin(empresa);
        Empresa outra = prepararEmpresa();
        String codigo = "DUP-" + sufixo();
        String id = criar(token, null, codigo, "Centro original");
        CentroCusto paiOutraEmpresa = new CentroCusto();
        paiOutraEmpresa.atualizar(outra, null, "OUT-" + sufixo(), "Outra empresa", null, TipoCentroCusto.AMBOS);
        paiOutraEmpresa = centroCustoRepository.saveAndFlush(paiOutraEmpresa);

        mockMvc.perform(post("/api/v1/centros-custo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null, codigo, "Duplicado")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/centros-custo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(paiOutraEmpresa.getId(),
                                "FILHO-" + sufixo(), "Filho invalido")))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/v1/centros-custo/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(UUID.fromString(id), codigo, "Centro original")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/v1/centros-custo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403SemPermissao() throws Exception {
        Usuario usuario = new Usuario("Sem Centro Custo",
                "sem-centro-" + UUID.randomUUID() + "@invplus.com", passwordEncoder.encode("admin123"));
        usuarioRepository.saveAndFlush(usuario);
        String token = JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, usuario, prepararEmpresa());

        mockMvc.perform(get("/api/v1/centros-custo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String criar(String token, UUID paiId, String codigo, String nome) throws Exception {
        String response = mockMvc.perform(post("/api/v1/centros-custo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(paiId, codigo, nome)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asString();
    }

    private String payload(UUID paiId, String codigo, String nome) {
        String pai = paiId == null ? "null" : "\"" + paiId + "\"";
        return """
                {
                  "centroCustoPaiId": %s,
                  "codigo": "%s",
                  "nome": "%s",
                  "descricao": "Centro de custo de teste",
                  "tipo": "AMBOS"
                }
                """.formatted(pai, codigo, nome);
    }

    private Empresa prepararEmpresa() {
        String sufixo = sufixo();
        return empresaRepository.saveAndFlush(new Empresa("Empresa CC " + sufixo, "Empresa CC " + sufixo,
                documento(sufixo), null, null, null));
    }

    private String tokenAdmin(Empresa empresa) {
        Usuario admin = usuarioTestFactory.criarAtivoComPerfilAdmin();
        return JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, admin, empresa);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("12345678000000" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
