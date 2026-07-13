package br.com.invplus.modules.financeiro.importacao.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.importacao.enums.StatusImportacaoExtrato;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioRepository;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.security.service.AuthenticatedUserDetails;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;

/** Testes integrados do service de importacao e sua atomicidade. */
@SpringBootTest
@ActiveProfiles("test")
class ImportacaoExtratoServiceTest {

    @AfterEach
    @SuppressWarnings("unused")
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Autowired private ImportacaoExtratoService service;
    @Autowired private ContaBancariaService contaBancariaService;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ContaBancariaRepository contaBancariaRepository;
    @Autowired private ExtratoBancarioRepository extratoRepository;
    @Autowired private ExtratoBancarioLancamentoRepository lancamentoRepository;

    @Test
    void deveImportarOfxCriarExtratoELancamentosSemAlterarSaldoOuConciliar() throws IOException {
        var conta = prepararConta("99999", "9");
        BigDecimal saldoAntes = conta.getSaldoAtual();
        var response = service.importarOfx(arquivo("extrato-creditos-debitos.ofx", sufixo()),
                conta.getId(), "Importacao teste");

        var extrato = extratoRepository.findById(response.extratoBancarioId()).orElseThrow();
        var lancamentos = lancamentoRepository
                .findByExtratoBancarioIdAndAtivoTrueAndDeletedAtIsNull(extrato.getId());
        assertThat(response.status()).isEqualTo(StatusImportacaoExtrato.CONCLUIDA);
        assertThat(response.quantidadeImportada()).isEqualTo(3);
        assertThat(extrato.getOrigem()).isEqualTo(OrigemExtratoBancario.OFX);
        assertThat(lancamentos).hasSize(3).allMatch(item -> !item.getConciliado());
        assertThat(lancamentos).anyMatch(item -> item.getValor().compareTo(new BigDecimal("120.50")) == 0);
        assertThat(contaBancariaRepository.findById(conta.getId()).orElseThrow().getSaldoAtual())
                .isEqualByComparingTo(saldoAntes);
    }

    @Test
    void deveIdentificarContaAutomaticamenteEIgnorarFitidDuplicado() throws IOException {
        String identificador = UUID.randomUUID().toString().replaceAll("\\D", "") + "000000000000";
        String banco = "9" + identificador.substring(0, 2);
        String agencia = identificador.substring(2, 6);
        String numero = identificador.substring(6, 11);
        var conta = prepararConta(banco, agencia, numero, "1");
        var response = service.importarOfx(
                arquivo("extrato-duplicado.ofx", sufixo(), banco, agencia, numero + "-1"),
                null, null);

        assertThat(response.contaBancariaId()).isEqualTo(conta.getId());
        assertThat(response.status()).isEqualTo(StatusImportacaoExtrato.CONCLUIDA_COM_ALERTAS);
        assertThat(response.quantidadeLida()).isEqualTo(2);
        assertThat(response.quantidadeImportada()).isEqualTo(1);
        assertThat(response.quantidadeIgnorada()).isEqualTo(1);
        assertThat(response.alertas()).hasSize(1);
    }

    @Test
    void deveBloquearArquivoVazioExtensaoInvalidaEContaIncompativel() throws IOException {
        var conta = prepararConta("88888", "8");
        var vazio = new MockMultipartFile("arquivo", "vazio.ofx", "application/x-ofx", new byte[0]);
        var invalido = new MockMultipartFile("arquivo", "extrato.txt", "text/plain", "<OFX/>".getBytes());

        assertThatThrownBy(() -> service.importarOfx(vazio, conta.getId(), null))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> service.importarOfx(invalido, conta.getId(), null))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> service.importarOfx(arquivo("extrato-valido.ofx", sufixo()),
                conta.getId(), null)).isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("conta");
    }

    @Test
    void deveSerIdempotentePorHash() throws IOException {
        var conta = prepararConta("99999", "9");
        MockMultipartFile arquivo = arquivo("extrato-valido.ofx", sufixo());
        var primeira = service.importarOfx(arquivo, conta.getId(), null);

        assertThatThrownBy(() -> service.importarOfx(arquivo, conta.getId(), null))
                .isInstanceOf(ConflictException.class);
        assertThat(extratoRepository.findById(primeira.extratoBancarioId())).isPresent();
        assertThat(lancamentoRepository.findByExtratoBancarioIdAndAtivoTrueAndDeletedAtIsNull(
                primeira.extratoBancarioId())).hasSize(1);
    }

    private br.com.invplus.modules.financeiro.entity.ContaBancaria prepararConta(String numero, String digito) {
        return prepararConta("001", "1234", numero, digito);
    }

    private br.com.invplus.modules.financeiro.entity.ContaBancaria prepararConta(
            String banco, String agencia, String numero, String digito) {
        String sufixo = sufixo();
        Empresa empresa = empresaRepository.saveAndFlush(new Empresa("Empresa OFX " + sufixo,
                "Empresa OFX " + sufixo, documento(sufixo), null, null, null));
        var principal = new AuthenticatedUserDetails(UUID.randomUUID(), "ofx@teste.local", "",
                List.of(), true, empresa.getId(), null);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
        var response = contaBancariaService.criar(new ContaBancariaRequest(null,
                banco, "Banco OFX", agencia, null, numero, digito, TipoContaBancaria.CORRENTE,
                "Empresa OFX", null, null, new BigDecimal("500.00"), LocalDate.of(2026, 7, 1), true));
        return contaBancariaRepository.findById(response.id()).orElseThrow();
    }

    private MockMultipartFile arquivo(String nome, String nonce) throws IOException {
        return arquivo(nome, nonce, "001", "1234", "99999-9");
    }

    private MockMultipartFile arquivo(
            String nome, String nonce, String banco, String agencia, String conta) throws IOException {
        byte[] original;
        try (var stream = getClass().getResourceAsStream("/ofx/" + nome)) {
            if (stream == null) throw new IOException("Fixture ausente");
            original = stream.readAllBytes();
        }
        String conteudo = new String(original, StandardCharsets.UTF_8)
                .replace("<BANKID>001</BANKID>", "<BANKID>" + banco + "</BANKID>")
                .replace("<BRANCHID>1234</BRANCHID>", "<BRANCHID>" + agencia + "</BRANCHID>")
                .replace("<ACCTID>99999-9</ACCTID>", "<ACCTID>" + conta + "</ACCTID>")
                + "\n<!-- " + nonce + " -->";
        return new MockMultipartFile("arquivo", nome, "application/x-ofx",
                conteudo.getBytes(StandardCharsets.UTF_8));
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("44555666000199" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
