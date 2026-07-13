package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.service.ContaBancariaService;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.support.context.AbstractContextoEmpresarialIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida o cadastro de conta bancaria com persistencia real e filtros operacionais.
 */
@SpringBootTest
@ActiveProfiles("test")
class ContaBancariaBusinessFlowIntegrationTest extends AbstractContextoEmpresarialIntegrationTest {

    @Autowired
    private ContaBancariaService service;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private FilialRepository filialRepository;

    @AfterEach
    void limparContexto() {
        testEmpresaContext.limpar();
    }

    @Test
    @Transactional
    void deveCriarFiltrarEValidarRegrasDeContaBancaria() {
        String sufixo = sufixo();
        Empresa empresa = empresaRepository.save(new Empresa("Empresa Banco " + sufixo,
                "Empresa Banco " + sufixo, documento(sufixo), null, null, null));
        Empresa outraEmpresa = empresaRepository.save(new Empresa("Outra Banco " + sufixo,
                "Outra Banco " + sufixo, documento("99" + sufixo), null, null, null));
        ativarEmpresa(empresa.getId());
        Filial filial = filialRepository.save(filial(empresa, "F" + sufixo.substring(0, 4)));
        Filial filialOutraEmpresa = filialRepository.save(filial(outraEmpresa, "O" + sufixo.substring(0, 4)));

        var conta = service.criar(request(empresa.getId(), filial.getId(), "001", "1234" + sufixo,
                "98765", true, new BigDecimal("250.00"), "12345678901"));
        var caixa = service.criar(request(empresa.getId(), null, "999", "CX" + sufixo,
                "1", false, null, null));

        assertThat(conta.saldoAtual()).isEqualByComparingTo("250.00");
        assertThat(caixa.saldoAtual()).isZero();
        assertThat(service.listar(filial.getId(), "001", null,
                TipoContaBancaria.CORRENTE, true, true, PageRequest.of(0, 10)).getContent())
                .extracting("id")
                .containsExactly(conta.id());

        assertThatThrownBy(() -> service.criar(request(empresa.getId(), null, "001", "1234" + sufixo,
                "98765", false, BigDecimal.ZERO, null)))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.criar(request(empresa.getId(), null, "237", "2222" + sufixo,
                "22222", true, BigDecimal.ZERO, null)))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.criar(request(empresa.getId(), filialOutraEmpresa.getId(), "104",
                "3333" + sufixo, "33333", false, BigDecimal.ZERO, null)))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> service.criar(request(empresa.getId(), null, "341", "4444" + sufixo,
                "44444", false, BigDecimal.ZERO, "12345")))
                .isInstanceOf(InvalidOperationException.class);
    }

    private ContaBancariaRequest request(UUID empresaId, UUID filialId, String bancoCodigo,
            String agencia, String numeroConta, Boolean principal, BigDecimal saldoInicial, String documento) {
        return new ContaBancariaRequest(filialId, bancoCodigo, "Banco " + bancoCodigo,
                agencia, "1", numeroConta, "0", TipoContaBancaria.CORRENTE,
                "Titular " + bancoCodigo, documento, "pix-" + bancoCodigo + "@teste.com",
                saldoInicial, LocalDate.of(2026, 7, 9), principal);
    }

    private Filial filial(Empresa empresa, String codigo) {
        Filial filial = new Filial();
        filial.atualizar(empresa, null, codigo, "Filial " + codigo, null, null, null, null, false);
        return filial;
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("54321000000000" + numeros).substring(numeros.length());
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
