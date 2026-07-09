package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.CentroCustoRequest;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.service.CentroCustoService;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida a arvore completa de centros de custo com persistencia real.
 */
@SpringBootTest
@ActiveProfiles("test")
class CentroCustoBusinessFlowIntegrationTest {

    @Autowired
    private CentroCustoService service;
    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @Transactional
    void deveConstruirArvoreEImpedirCicloHierarquico() {
        String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Empresa empresa = empresaRepository.save(new Empresa("Empresa Fluxo CC " + sufixo,
                "Empresa Fluxo CC " + sufixo, documento(sufixo), null, null, null));

        var raiz = service.criar(request(empresa.getId(), null, "ADM-" + sufixo, "Administrativo"));
        var nivel2 = service.criar(request(empresa.getId(), raiz.id(), "COM-" + sufixo, "Comercial"));
        var nivel3 = service.criar(request(empresa.getId(), nivel2.id(), "MKT-" + sufixo, "Marketing"));

        assertThat(nivel3.caminhoCompleto()).isEqualTo("Administrativo > Comercial > Marketing");

        assertThatThrownBy(() -> service.atualizar(raiz.id(),
                request(empresa.getId(), nivel3.id(), raiz.codigo(), raiz.nome())))
                .isInstanceOf(InvalidOperationException.class);
    }

    private CentroCustoRequest request(UUID empresaId, UUID paiId, String codigo, String nome) {
        return new CentroCustoRequest(empresaId, paiId, codigo, nome, null, TipoCentroCusto.AMBOS);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("98765432000000" + numeros).substring(numeros.length());
    }
}
