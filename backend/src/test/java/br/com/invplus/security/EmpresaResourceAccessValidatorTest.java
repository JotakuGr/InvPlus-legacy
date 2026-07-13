package br.com.invplus.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.invplus.security.exception.AcessoEmpresaNegadoException;
import br.com.invplus.security.exception.AcessoFilialNegadoException;
import br.com.invplus.security.validator.EmpresaResourceAccessValidator;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmpresaResourceAccessValidatorTest {

    private final EmpresaResourceAccessValidator validator = new EmpresaResourceAccessValidator();

    @Test
    void deveAceitarEmpresaEFilialDoContexto() {
        UUID empresa = UUID.randomUUID();
        UUID filial = UUID.randomUUID();
        assertThatCode(() -> validator.validarEmpresa(empresa, empresa)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validarFilialOpcional(filial, filial)).doesNotThrowAnyException();
    }

    @Test
    void deveBloquearEmpresaDiferente() {
        assertThatThrownBy(() -> validator.validarEmpresa(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(AcessoEmpresaNegadoException.class);
    }

    @Test
    void deveBloquearFilialDiferenteQuandoAmbasInformadas() {
        assertThatThrownBy(() -> validator.validarFilialOpcional(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(AcessoFilialNegadoException.class);
    }
}
