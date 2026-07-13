package br.com.invplus.integration.cadastros;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.UUID;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.support.security.EmpresaTestFactory;
import br.com.invplus.support.security.JwtTestFactory;
import br.com.invplus.support.security.UsuarioEmpresaTestFactory;
import br.com.invplus.support.security.UsuarioTestFactory;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Exercita os principais endpoints REST do modulo Cadastros.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CadastrosControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioTestFactory usuarioTestFactory;

    @Autowired
    private EmpresaTestFactory empresaTestFactory;

    @Autowired
    private UsuarioEmpresaTestFactory usuarioEmpresaTestFactory;

    @Autowired
    private JwtTestFactory jwtTestFactory;

    /**
     * Valida criacao e consulta de empresa usando o perfil ADMIN.
     */
    @Test
    void deveCriarEConsultarEmpresaComPermissaoDeCadastros() throws Exception {
        String accessToken = criarContexto().accessToken();
        String cnpj = cnpjUnico();

        String response = mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "razaoSocial": "Empresa Controller LTDA",
                                  "nomeFantasia": "Empresa Controller",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "regimeTributario": "SIMPLES_NACIONAL",
                                  "crt": 1,
                                  "telefone": "71999990000",
                                  "email": "controller@empresa.com"
                                }
                                """.formatted(cnpj)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Controller LTDA"))
                .andExpect(jsonPath("$.cnpj").value(cnpj))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode empresa = objectMapper.readTree(response);

        mockMvc.perform(get("/api/v1/empresas/" + empresa.get("id").asString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("controller@empresa.com"));
    }

    /**
     * Confirma que os endpoints de Cadastros exigem autenticacao.
     */
    @Test
    void deveNegarListagemDeEmpresasSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/empresas"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Confirma que a API padrao oculta empresas excluidas logicamente.
     */
    @Test
    void deveOcultarEmpresaExcluidaLogicamenteNaApi() throws Exception {
        String accessToken = criarContexto().accessToken();
        String cnpj = cnpjUnico();

        String response = mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "razaoSocial": "Empresa Soft Delete LTDA",
                                  "nomeFantasia": "Empresa Soft Delete",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "regimeTributario": "SIMPLES_NACIONAL",
                                  "crt": 1,
                                  "telefone": "71999991111",
                                  "email": "softdelete@empresa.com"
                                }
                                """.formatted(cnpj)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String empresaId = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(delete("/api/v1/empresas/" + empresaId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/empresas/" + empresaId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        String listagem = mockMvc.perform(get("/api/v1/empresas")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listagem).doesNotContain(empresaId);
    }

    /**
     * Valida o fluxo das entidades fundamentais: pais, estado, cidade e filial.
     */
    @Test
    void deveCriarHierarquiaGeograficaEFilial() throws Exception {
        ContextoTeste contexto = criarContexto();
        String accessToken = contexto.accessToken();
        String empresaId = contexto.empresaId().toString();
        String estadoCodigoIbge = codigoIbgeEstadoUnico();
        String cidadeCodigoIbge = codigoIbgeCidadeUnico();

        PaisCriado pais = criarPaisUnico(accessToken);
        String paisId = pais.id();
        String uf = pais.codigoIso2();

        String estadoResponse = mockMvc.perform(post("/api/v1/cadastros/estados")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paisId": "%s",
                                  "nome": "Estado %s",
                                  "uf": "%s",
                                  "codigoIbge": "%s"
                                }
                                """.formatted(paisId, uf, uf, estadoCodigoIbge)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paisId").value(paisId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String estadoId = objectMapper.readTree(estadoResponse).get("id").asString();

        String cidadeResponse = mockMvc.perform(post("/api/v1/cadastros/cidades")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estadoId": "%s",
                                  "nome": "Cidade %s",
                                  "codigoIbge": "%s"
                                }
                                """.formatted(estadoId, uf, cidadeCodigoIbge)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoId").value(estadoId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String cidadeId = objectMapper.readTree(cidadeResponse).get("id").asString();
        String enderecoResponse = mockMvc.perform(post("/api/v1/cadastros/enderecos")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empresaId": "%s",
                                  "cidadeId": "%s",
                                  "tipo": "PRINCIPAL",
                                  "logradouro": "Rua Filial",
                                  "numero": "10",
                                  "bairro": "Centro",
                                  "cidade": "Cidade %s",
                                  "uf": "%s",
                                  "cep": "40000000",
                                  "principal": true
                                }
                                """.formatted(empresaId, cidadeId, uf, uf)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cidadeId").value(cidadeId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String enderecoId = objectMapper.readTree(enderecoResponse).get("id").asString();

        mockMvc.perform(post("/api/v1/cadastros/filiais")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enderecoId": "%s",
                                  "codigo": "FIL-%s",
                                  "nome": "Filial %s",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "telefone": "71999992222",
                                  "email": "filial%s@empresa.com",
                                  "matriz": false
                                }
                                """.formatted(enderecoId, uf, uf, cnpjUnico(), uf.toLowerCase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empresaId").value(empresaId))
                .andExpect(jsonPath("$.enderecoId").value(enderecoId))
                .andExpect(jsonPath("$.matriz").value(false));
    }

    @Test
    void deveIsolarFiliaisPorEmpresaDoToken() throws Exception {
        ContextoTeste contextoA = criarContexto();
        ContextoTeste contextoB = criarContexto();
        String sufixo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String response = mockMvc.perform(post("/api/v1/cadastros/filiais")
                        .header("Authorization", "Bearer " + contextoA.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "FIL-%s",
                                  "nome": "Filial Isolada %s",
                                  "cnpj": "%s",
                                  "matriz": false
                                }
                                """.formatted(sufixo, sufixo, cnpjUnico())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empresaId").value(contextoA.empresaId().toString()))
                .andReturn().getResponse().getContentAsString();

        String filialId = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/v1/cadastros/filiais/" + filialId)
                        .header("Authorization", "Bearer " + contextoB.accessToken()))
                .andExpect(status().isNotFound());

        String listagemB = mockMvc.perform(get("/api/v1/cadastros/filiais")
                        .header("Authorization", "Bearer " + contextoB.accessToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(listagemB).doesNotContain(filialId);
    }

    private ContextoTeste criarContexto() {
        Usuario usuario = usuarioTestFactory.criarAtivoComPerfilAdmin();
        Empresa empresa = empresaTestFactory.criarAtiva();
        usuarioEmpresaTestFactory.criar(usuario, empresa, true, true);
        String token = jwtTestFactory.accessToken(
                usuario.getId(), usuario.getEmail(), empresa.getId(), null, java.util.Set.of("CADASTROS:*"));
        return new ContextoTeste(token, empresa.getId());
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "77" + "%012d".formatted(parte);
    }

    private String codigoIso2Unico() {
        long valor = Long.remainderUnsigned(UUID.randomUUID().getLeastSignificantBits(), 1296);
        return "%2s".formatted(Long.toString(valor, 36).toUpperCase()).replace(' ', '0');
    }

    private PaisCriado criarPaisUnico(String accessToken) throws Exception {
        for (int tentativa = 0; tentativa < 40; tentativa++) {
            String codigoIso2 = codigoIso2Unico();
            String codigoBacen = codigoBacenUnico();
            MvcResult result = mockMvc.perform(post("/api/v1/cadastros/paises")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nome": "Pais %s",
                                      "codigoIso2": "%s",
                                      "codigoIso3": "PX%s",
                                      "codigoBacen": "%s"
                                    }
                                    """.formatted(codigoIso2, codigoIso2, codigoIso2.substring(1), codigoBacen)))
                    .andReturn();
            int statusCode = result.getResponse().getStatus();
            if (statusCode == 201) {
                JsonNode pais = objectMapper.readTree(result.getResponse().getContentAsString());
                assertThat(pais.get("version").asInt()).isZero();
                return new PaisCriado(pais.get("id").asString(), codigoIso2);
            }
            if (statusCode != 409) {
                assertThat(statusCode).isEqualTo(201);
            }
        }
        throw new AssertionError("Nao foi possivel gerar codigo ISO2 unico para o teste de cadastros");
    }

    private String codigoBacenUnico() {
        long valor = Math.abs(System.nanoTime() % 10_000_000_000L);
        return "%010d".formatted(valor);
    }

    private String codigoIbgeEstadoUnico() {
        long valor = Math.abs(System.nanoTime() % 100);
        return "%02d".formatted(valor);
    }

    private String codigoIbgeCidadeUnico() {
        long valor = Math.abs(System.nanoTime() % 10_000_000);
        return "%07d".formatted(valor);
    }

    private record PaisCriado(String id, String codigoIso2) {
    }

    private record ContextoTeste(String accessToken, UUID empresaId) {
    }
}
