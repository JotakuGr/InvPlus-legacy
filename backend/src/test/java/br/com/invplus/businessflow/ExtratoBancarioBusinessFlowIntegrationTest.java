package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioService;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida o fluxo persistido de conta bancaria para importacao de extrato.
 */
@SpringBootTest
@ActiveProfiles("test")
class ExtratoBancarioBusinessFlowIntegrationTest {

    @Autowired
    private ExtratoBancarioService extratoService;
    @Autowired
    private ContaBancariaService contaBancariaService;
    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @Transactional
    void deveImportarFiltrarCancelarEValidarHashDoExtrato() {
        String sufixo = sufixo();
        Empresa empresa = empresaRepository.save(new Empresa("Empresa Extrato " + sufixo,
                "Empresa Extrato " + sufixo, documento(sufixo), null, null, null));
        var conta = contaBancariaService.criar(contaRequest(empresa.getId(), sufixo));
        String hash = hash(sufixo);

        var extrato = extratoService.importar(extratoRequest(conta.id(), hash,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)));

        assertThat(extrato.status()).isEqualTo(StatusExtratoBancario.IMPORTADO);
        assertThat(extrato.importadoEm()).isNotNull();
        assertThat(extratoService.listar(conta.id(), OrigemExtratoBancario.OFX,
                StatusExtratoBancario.IMPORTADO, LocalDate.of(2026, 7, 1), null,
                null, LocalDate.of(2026, 7, 31), true, PageRequest.of(0, 10)).getContent())
                .extracting("id")
                .containsExactly(extrato.id());

        assertThatThrownBy(() -> extratoService.importar(extratoRequest(conta.id(), hash,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31))))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> extratoService.importar(extratoRequest(conta.id(), "invalido",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31))))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> extratoService.importar(extratoRequest(conta.id(), hash("B" + sufixo),
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 7, 31))))
                .isInstanceOf(InvalidOperationException.class);

        var cancelado = extratoService.cancelar(extrato.id());

        assertThat(cancelado.status()).isEqualTo(StatusExtratoBancario.CANCELADO);
        assertThat(cancelado.ativo()).isTrue();
        assertThatThrownBy(() -> extratoService.cancelar(extrato.id()))
                .isInstanceOf(InvalidOperationException.class);
    }

    private ContaBancariaRequest contaRequest(UUID empresaId, String sufixo) {
        return new ContaBancariaRequest(empresaId, null, "001", "Banco Fluxo",
                "12" + sufixo.substring(0, 4), "1", "98" + sufixo.substring(4),
                "0", TipoContaBancaria.CORRENTE, "Titular Fluxo",
                "12345678901", "pix-" + sufixo + "@teste.com", BigDecimal.ZERO,
                LocalDate.of(2026, 7, 9), true);
    }

    private ExtratoBancarioRequest extratoRequest(UUID contaId, String hash,
            LocalDate dataInicio, LocalDate dataFim) {
        return new ExtratoBancarioRequest(contaId, dataInicio, dataFim, BigDecimal.ZERO,
                new BigDecimal("1000.00"), OrigemExtratoBancario.OFX, "extrato.ofx",
                hash, "Fluxo de extrato");
    }

    private String hash(String sufixo) {
        return (sufixo.replaceAll("[^A-Fa-f0-9]", "A") + "abcdef0123456789"
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").substring(0, 64);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("76543200000000" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
