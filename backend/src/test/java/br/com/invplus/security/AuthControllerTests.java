package br.com.invplus.security;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.entity.UsuarioEmpresa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.repository.UsuarioRepository;
import br.com.invplus.security.service.JwtService;
import br.com.invplus.support.security.EmpresaTestFactory;
import br.com.invplus.support.security.JwtTestFactory;
import br.com.invplus.support.security.UsuarioEmpresaTestFactory;
import br.com.invplus.support.security.UsuarioTestFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioTestFactory usuarioTestFactory;

    @Autowired
    private EmpresaTestFactory empresaTestFactory;

    @Autowired
    private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;

    @Autowired
    private JwtTestFactory jwtTestFactory;

    @Test
    void deveAutenticarAdministradorInicialERetornarJwt() throws Exception {
        DadosAuth dados = criarUsuarioComUmaEmpresa();
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(dados.usuario().getEmail(), UsuarioTestFactory.SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.status").value("AUTENTICADO"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.selectionToken").doesNotExist())
                .andExpect(jsonPath("$.contexto.empresaId").value(dados.empresa().getId().toString()))
                .andExpect(jsonPath("$.usuario.email").value(dados.usuario().getEmail()))
                .andExpect(jsonPath("$.usuario.perfis[0]").value("ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String accessToken = json.get("accessToken").asString();

        assertThat(accessToken).contains(".");
        assertThat(accessToken.split("\\.")).hasSize(3);
        assertThat(jwtService.extrairTokenUse(accessToken)).isEqualTo("ACCESS");
        assertThat(jwtService.extrairUsuarioId(accessToken)).isEqualTo(dados.usuario().getId());
        assertThat(jwtService.extrairUuidClaim(accessToken, "empresa_id")).isEqualTo(dados.empresa().getId());
        assertThat(jwtService.extrairUuidClaim(accessToken, "jti")).isNotNull();
        assertThat(json.get("usuario").get("permissoes")).isNotEmpty();
        assertThat(json.get("usuario").get("permissoes").toString()).contains("SEGURANCA:*");
    }

    @Test
    void deveNegarLoginComSenhaInvalida() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@invplus.com",
                                  "senha": "senha-errada"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais invalidas"));
    }

    @Test
    void deveNegarLoginComUsuarioInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "nao-existe@invplus.com",
                                  "senha": "admin123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais invalidas"));
    }

    @Test
    void deveNegarEndpointProtegidoSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirEndpointProtegidoComTokenValido() throws Exception {
        String accessToken = login(criarUsuarioComUmaEmpresa()).get("accessToken").asString();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarEndpointProtegidoComTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveConsiderarTokenExpiradoComoInvalido() {
        Object expirationOriginal = ReflectionTestUtils.getField(jwtService, "expirationMinutes");
        try {
            ReflectionTestUtils.setField(jwtService, "expirationMinutes", -1L);
            String token = jwtService.gerarToken(User.withUsername("admin@invplus.com")
                    .password("x")
                    .authorities("SEGURANCA:*")
                    .build());

            assertThat(jwtService.tokenValido(token)).isFalse();
        } finally {
            ReflectionTestUtils.setField(jwtService, "expirationMinutes", expirationOriginal);
        }
    }

    @Test
    void deveRenovarAccessTokenComRefreshTokenValido() throws Exception {
        DadosAuth dados = criarUsuarioComUmaEmpresa();
        String refreshToken = login(dados).get("refreshToken").asString();

        String response = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.contexto.empresaId").value(dados.empresa().getId().toString()))
                .andReturn().getResponse().getContentAsString();

        String novoAccessToken = objectMapper.readTree(response).get("accessToken").asString();
        assertThat(jwtService.extrairTokenUse(novoAccessToken)).isEqualTo("ACCESS");
        assertThat(jwtService.extrairUsuarioId(novoAccessToken)).isEqualTo(dados.usuario().getId());
        assertThat(jwtService.extrairUuidClaim(novoAccessToken, "empresa_id"))
                .isEqualTo(dados.empresa().getId());
    }

    @Test
    void deveRevogarRefreshTokenNoLogout() throws Exception {
        String refreshToken = login(criarUsuarioComUmaEmpresa()).get("refreshToken").asString();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveNegarAcessoQuandoUsuarioNaoTemPermissao() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoSemPerfil();
        Empresa empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("SEGURANCA:*"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveExigirSelecaoQuandoUsuarioPossuiVariasEmpresas() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresaA = empresaTestFactory.criarAtiva();
        Empresa empresaB = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresaA, true, true);
        usuarioEmpresaTestFactory.criar(usuario, empresaB, false, true);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SELECAO_EMPRESA_NECESSARIA"))
                .andExpect(jsonPath("$.selectionToken").isString())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.empresas.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        String selectionToken = objectMapper.readTree(response).get("selectionToken").asString();
        assertThat(jwtService.extrairTokenUse(selectionToken)).isEqualTo("COMPANY_SELECTION");
        assertThat(jwtService.extrairUsuarioId(selectionToken)).isEqualTo(usuario.getId());
        assertThat(jwtService.extrairUuidClaim(selectionToken, "jti")).isNotNull();
        assertThat(jwtService.extrairAuthorities(selectionToken)).isNull();
    }

    @Test
    void deveNegarLoginOperacionalSemVinculoEmpresarial() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(usuario)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveSelecionarEmpresaComSelectionToken() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresaA = empresaTestFactory.criarAtiva();
        Empresa empresaB = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresaA, true, true);
        usuarioEmpresaTestFactory.criar(usuario, empresaB, false, true);
        String selectionToken = loginSelectionToken(usuario);

        String response = mockMvc.perform(post("/api/v1/auth/contexto/selecionar")
                        .header("Authorization", "Bearer " + selectionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresaId":"%s"}
                                """.formatted(empresaB.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.contexto.empresaId").value(empresaB.getId().toString()))
                .andReturn().getResponse().getContentAsString();

        String accessToken = objectMapper.readTree(response).get("accessToken").asString();
        assertThat(jwtService.extrairTokenUse(accessToken)).isEqualTo("ACCESS");
        assertThat(jwtService.extrairUuidClaim(accessToken, "empresa_id")).isEqualTo(empresaB.getId());
    }

    @Test
    void deveNegarSelecaoDeEmpresaSemVinculo() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresaA = empresaTestFactory.criarAtiva();
        Empresa empresaB = empresaTestFactory.criarAtiva();
        Empresa empresaSemVinculo = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresaA, true, true);
        usuarioEmpresaTestFactory.criar(usuario, empresaB, false, true);

        mockMvc.perform(post("/api/v1/auth/contexto/selecionar")
                        .header("Authorization", "Bearer " + loginSelectionToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresaId":"%s"}
                                """.formatted(empresaSemVinculo.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveTrocarContextoEntreEmpresasVinculadas() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresaA = empresaTestFactory.criarAtiva();
        Empresa empresaB = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresaA, true, true);
        usuarioEmpresaTestFactory.criar(usuario, empresaB, false, true);
        String selectionToken = loginSelectionToken(usuario);
        String accessTokenA = selecionar(selectionToken, empresaA.getId()).get("accessToken").asString();

        String response = mockMvc.perform(post("/api/v1/auth/contexto/trocar")
                        .header("Authorization", "Bearer " + accessTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresaId":"%s"}
                                """.formatted(empresaB.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto.empresaId").value(empresaB.getId().toString()))
                .andReturn().getResponse().getContentAsString();

        JsonNode tokens = objectMapper.readTree(response);
        assertThat(jwtService.extrairUuidClaim(tokens.get("accessToken").asString(), "empresa_id"))
                .isEqualTo(empresaB.getId());
    }

    @Test
    void deveNegarRefreshQuandoVinculoFoiDesativado() throws Exception {
        DadosAuth dados = criarUsuarioComUmaEmpresa();
        String refreshToken = login(dados).get("refreshToken").asString();
        UsuarioEmpresa vinculo = usuarioEmpresaRepository
                .findByUsuarioIdAndEmpresaIdAndAtivoTrue(dados.usuario().getId(), dados.empresa().getId())
                .orElseThrow();
        vinculo.desativar();
        usuarioEmpresaRepository.saveAndFlush(vinculo);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarAccessTokenSemEmpresaEmEndpointOperacional() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        String token = jwtTestFactory.accessTokenSemEmpresa(
                usuario.getId(), usuario.getEmail(), java.util.Set.of("CONTA_BANCARIA:VISUALIZAR"));

        mockMvc.perform(get("/api/v1/contas-bancarias")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarSelectionTokenEmEndpointsOperacionais() throws Exception {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        String token = jwtTestFactory.selectionToken(usuario.getId());

        for (String endpoint : java.util.List.of(
                "/api/v1/estoques",
                "/api/v1/cadastros/filiais",
                "/api/v1/produtos-precos")) {
            mockMvc.perform(get(endpoint).header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
    }

    private JsonNode login(DadosAuth dados) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(dados.usuario())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private DadosAuth criarUsuarioComUmaEmpresa() {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        return new DadosAuth(usuario, empresa);
    }

    private String loginSelectionToken(Usuario usuario) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(usuario)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("selectionToken").asString();
    }

    private JsonNode selecionar(String selectionToken, UUID empresaId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/contexto/selecionar")
                        .header("Authorization", "Bearer " + selectionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresaId":"%s"}
                                """.formatted(empresaId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String loginPayload(Usuario usuario) {
        return """
                {"email":"%s","senha":"%s"}
                """.formatted(usuario.getEmail(), UsuarioTestFactory.SENHA);
    }

    private record DadosAuth(Usuario usuario, Empresa empresa) {
    }
}
