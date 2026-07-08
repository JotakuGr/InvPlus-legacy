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
import java.util.UUID;

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

    /**
     * Valida criacao e consulta de empresa usando o perfil ADMIN.
     */
    @Test
    void deveCriarEConsultarEmpresaComPermissaoDeCadastros() throws Exception {
        String accessToken = login().get("accessToken").asString();
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
        String accessToken = login().get("accessToken").asString();
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
        String accessToken = login().get("accessToken").asString();
        String codigoIso2 = codigoIso2Unico();
        String codigoBacen = codigoBacenUnico();
        String estadoCodigoIbge = codigoIbgeEstadoUnico();
        String cidadeCodigoIbge = codigoIbgeCidadeUnico();

        String paisResponse = mockMvc.perform(post("/api/v1/cadastros/paises")
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paisId = objectMapper.readTree(paisResponse).get("id").asString();
        String uf = codigoIso2;

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
        String empresaId = criarEmpresa(accessToken, cnpjUnico());

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
                                  "empresaId": "%s",
                                  "enderecoId": "%s",
                                  "codigo": "FIL-%s",
                                  "nome": "Filial %s",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "telefone": "71999992222",
                                  "email": "filial%s@empresa.com",
                                  "matriz": true
                                }
                                """.formatted(empresaId, enderecoId, uf, uf, cnpjUnico(), uf.toLowerCase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empresaId").value(empresaId))
                .andExpect(jsonPath("$.enderecoId").value(enderecoId))
                .andExpect(jsonPath("$.matriz").value(true));
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@invplus.com",
                                  "senha": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private String cnpjUnico() {
        long parte = Math.abs(System.nanoTime() % 1_000_000_000_000L);
        return "77" + "%012d".formatted(parte);
    }

    private String criarEmpresa(String accessToken, String cnpj) throws Exception {
        String response = mockMvc.perform(post("/api/v1/empresas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "razaoSocial": "Empresa Base LTDA",
                                  "nomeFantasia": "Empresa Base",
                                  "cnpj": "%s",
                                  "inscricaoEstadual": "ISENTO",
                                  "regimeTributario": "SIMPLES_NACIONAL",
                                  "crt": 1,
                                  "telefone": "71999990000",
                                  "email": "base%s@empresa.com"
                                }
                                """.formatted(cnpj, cnpj.substring(2, 8))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asString();
    }

    private String codigoIso2Unico() {
        long valor = Long.remainderUnsigned(UUID.randomUUID().getLeastSignificantBits(), 1296);
        return "%2s".formatted(Long.toString(valor, 36).toUpperCase()).replace(' ', '0');
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
}
