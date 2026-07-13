package br.com.invplus.businessflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaReceberRepository;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.service.JwtService;
import br.com.invplus.support.JwtContextoTestSupport;
import br.com.invplus.support.security.UsuarioTestFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Fluxo financeiro: ContaReceber -> ParcelaReceber -> FormaPagamento -> Recebimento.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecebimentoBusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ContaReceberRepository contaReceberRepository;
    @Autowired
    private ParcelaReceberRepository parcelaReceberRepository;
    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;
    @Autowired
    private MovimentoFinanceiroRepository movimentoFinanceiroRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Autowired
    private UsuarioTestFactory usuarioTestFactory;

    @Test
    void deveReceberParcelaAtualizarContaEPermitirEstorno() throws Exception {
        ContaReceber conta = prepararConta();
        String token = tokenAdmin(conta.getEmpresa());
        ParcelaReceber parcela = prepararParcela(conta);
        FormaPagamento formaPagamento = prepararFormaPagamento();

        String response = mockMvc.perform(post("/api/v1/recebimentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(parcela.getId(), formaPagamento.getId(), "600.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andReturn().getResponse().getContentAsString();
        String recebimentoId = objectMapper.readTree(response).get("id").asString();
        org.assertj.core.api.Assertions.assertThat(movimentoFinanceiroRepository
                .findByOrigemAndOrigemIdAndAtivoTrueAndDeletedAtIsNull(
                        OrigemMovimentoFinanceiro.RECEBIMENTO, UUID.fromString(recebimentoId))
                .orElseThrow().getStatus()).isEqualTo(StatusMovimentoFinanceiro.ATIVO);

        mockMvc.perform(get("/api/v1/parcelas-receber/" + parcela.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGA"))
                .andExpect(jsonPath("$.valorAberto").value(0.00));

        mockMvc.perform(get("/api/v1/contas-receber/" + conta.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGA"))
                .andExpect(jsonPath("$.valorPago").value(600.00));

        mockMvc.perform(post("/api/v1/recebimentos/" + recebimentoId + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
        org.assertj.core.api.Assertions.assertThat(movimentoFinanceiroRepository
                .findByOrigemAndOrigemIdAndAtivoTrueAndDeletedAtIsNull(
                        OrigemMovimentoFinanceiro.RECEBIMENTO, UUID.fromString(recebimentoId))
                .orElseThrow().getStatus()).isEqualTo(StatusMovimentoFinanceiro.CANCELADO);

        mockMvc.perform(get("/api/v1/contas-receber/" + conta.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.valorPago").value(0.00))
                .andExpect(jsonPath("$.valorAberto").value(600.00));
    }

    private String tokenAdmin(Empresa empresa) {
        Usuario admin = usuarioTestFactory.criarAtivoComPerfilAdmin();
        return JwtContextoTestSupport.gerarAccessToken(
                jwtService, usuarioEmpresaRepository, admin, empresa);
    }

    private ContaReceber prepararConta() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Empresa empresa = new Empresa("Empresa BF REC " + sufixo, "Empresa BF REC " + sufixo,
                documentoNumerico("12", "000199"), null, null, null);
        empresa = empresaRepository.saveAndFlush(empresa);

        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente BF REC " + sufixo, null,
                documentoNumerico("98", "000188"), null, null, null, null, null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.atualizar(BigDecimal.ZERO, null, null, null);
        cliente = clienteRepository.saveAndFlush(cliente);

        ContaReceber conta = new ContaReceber();
        conta.abrir(empresa, null, cliente, null, "CRBFREC-" + sufixo.toUpperCase(), "Conta fluxo recebimento",
                LocalDate.of(2026, 7, 8), new BigDecimal("600.00"), null);
        return contaReceberRepository.saveAndFlush(conta);
    }

    private ParcelaReceber prepararParcela(ContaReceber conta) {
        ParcelaReceber parcela = new ParcelaReceber();
        parcela.abrir(conta, 1, LocalDate.of(2026, 7, 10), new BigDecimal("600.00"), null);
        return parcelaReceberRepository.saveAndFlush(parcela);
    }

    private FormaPagamento prepararFormaPagamento() {
        String codigo = "BFREC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        FormaPagamento formaPagamento = new FormaPagamento();
        formaPagamento.atualizar(codigo, "Pix " + codigo, TipoFormaPagamento.PIX, false, false);
        return formaPagamentoRepository.saveAndFlush(formaPagamento);
    }

    private String payload(UUID parcelaId, UUID formaPagamentoId, String valor) {
        return """
                {"parcelaReceberId":"%s","formaPagamentoId":"%s","valorRecebido":%s,"juros":0,"multa":0,"desconto":0,"dataRecebimento":"2026-07-10","observacao":"PIX"}
                """.formatted(parcelaId, formaPagamentoId, valor);
    }

    private String documentoNumerico(String prefixo, String sufixo) {
        String base = prefixo + Math.abs(UUID.randomUUID().hashCode()) + sufixo + "00000000000000";
        return base.substring(0, 14);
    }
}
