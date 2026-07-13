package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioLancamentoRequest;
import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioLancamentoService;
import br.com.invplus.modules.financeiro.service.ExtratoBancarioService;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.support.context.AbstractContextoEmpresarialIntegrationTest;
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
 * Valida o fluxo persistido ExtratoBancario -> Lancamentos bancarios.
 */
@SpringBootTest
@ActiveProfiles("test")
class ExtratoBancarioLancamentoBusinessFlowIntegrationTest extends AbstractContextoEmpresarialIntegrationTest {

    @Autowired
    private ExtratoBancarioLancamentoService lancamentoService;
    @Autowired
    private ExtratoBancarioService extratoService;
    @Autowired
    private ContaBancariaService contaBancariaService;
    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @Transactional
    void deveCriarFiltrarEditarConciliarEValidarRegrasDoLancamento() {
        String sufixoEmpresa = sufixo();
        Empresa empresa = empresaRepository.save(new Empresa("Empresa Lanc " + sufixoEmpresa,
                "Empresa Lanc " + sufixoEmpresa, documento(sufixoEmpresa), null, null, null));
        ativarEmpresa(empresa.getId());
        UUID extratoId = prepararExtrato(sufixo());
        UUID outroExtratoId = prepararExtrato(sufixo());
        String identificador = "BANK-" + sufixo();

        var credito = lancamentoService.criar(request(extratoId, TipoLancamentoBancario.CREDITO,
                identificador, CategoriaLancamentoBancario.PIX));
        var debito = lancamentoService.criar(request(extratoId, TipoLancamentoBancario.DEBITO,
                "BANK-" + sufixo(), CategoriaLancamentoBancario.TARIFA));
        var mesmoIdentificadorOutroExtrato = lancamentoService.criar(request(outroExtratoId,
                TipoLancamentoBancario.CREDITO, identificador, CategoriaLancamentoBancario.PIX));

        assertThat(credito.conciliado()).isFalse();
        assertThat(debito.tipo()).isEqualTo(TipoLancamentoBancario.DEBITO);
        assertThat(mesmoIdentificadorOutroExtrato.identificadorBancario()).isEqualTo(identificador);
        assertThat(lancamentoService.listar(extratoId, null, TipoLancamentoBancario.CREDITO,
                CategoriaLancamentoBancario.PIX, false, LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31), true, PageRequest.of(0, 10)).getContent())
                .extracting("id")
                .contains(credito.id());

        assertThatThrownBy(() -> lancamentoService.criar(request(extratoId,
                TipoLancamentoBancario.CREDITO, identificador, CategoriaLancamentoBancario.PIX)))
                .isInstanceOf(ConflictException.class);

        var atualizado = lancamentoService.atualizar(credito.id(), request(extratoId,
                TipoLancamentoBancario.CREDITO, "BANK-" + sufixo(), CategoriaLancamentoBancario.TRANSFERENCIA));
        assertThat(atualizado.categoriaBancaria()).isEqualTo(CategoriaLancamentoBancario.TRANSFERENCIA);

        lancamentoService.marcarComoConciliado(atualizado.id());
        assertThatThrownBy(() -> lancamentoService.atualizar(atualizado.id(), request(extratoId,
                TipoLancamentoBancario.CREDITO, "BANK-" + sufixo(), CategoriaLancamentoBancario.PIX)))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> lancamentoService.excluir(atualizado.id()))
                .isInstanceOf(InvalidOperationException.class);

        var solto = lancamentoService.criar(request(extratoId, TipoLancamentoBancario.CREDITO,
                "BANK-" + sufixo(), CategoriaLancamentoBancario.OUTRO));
        lancamentoService.excluir(solto.id());
        assertThatThrownBy(() -> lancamentoService.buscar(solto.id()))
                .isInstanceOf(Exception.class);
    }

    private UUID prepararExtrato(String sufixo) {
        var conta = contaBancariaService.criar(new ContaBancariaRequest(null,
                "001", "Banco Lanc", "12" + sufixo.substring(0, 4), "1",
                "98" + sufixo.substring(4), "0", TipoContaBancaria.CORRENTE,
                "Titular Lanc", "12345678901", "pix-" + sufixo + "@teste.com",
                BigDecimal.ZERO, LocalDate.of(2026, 7, 9), false));
        return extratoService.importar(new ExtratoBancarioRequest(conta.id(), LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31), BigDecimal.ZERO, new BigDecimal("1000.00"),
                OrigemExtratoBancario.OFX, "extrato.ofx", hash(sufixo), null)).id();
    }

    private ExtratoBancarioLancamentoRequest request(UUID extratoId, TipoLancamentoBancario tipo,
            String identificador, CategoriaLancamentoBancario categoria) {
        return new ExtratoBancarioLancamentoRequest(extratoId, LocalDate.of(2026, 7, 10),
                "Lancamento " + tipo, "DOC-" + identificador, tipo, BigDecimal.TEN,
                new BigDecimal("100.00"), identificador, categoria, "Fluxo");
    }

    private String hash(String sufixo) {
        return (sufixo.replaceAll("[^A-Fa-f0-9]", "a") + "abcdef0123456789"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb").substring(0, 64);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("98765400000000" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
