package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.PlanoContaRequest;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.financeiro.service.PlanoContaService;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida a arvore do plano de contas com persistencia e filtros reais.
 */
@SpringBootTest
@ActiveProfiles("test")
class PlanoContaBusinessFlowIntegrationTest {

    @Autowired
    private PlanoContaService service;
    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @Transactional
    void deveConstruirFiltrarArvoreEImpedirCiclo() {
        String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Empresa empresa = empresaRepository.save(new Empresa("Empresa Plano " + sufixo,
                "Empresa Plano " + sufixo, documento(sufixo), null, null, null));

        var raiz = service.criar(request(empresa.getId(), null, "3-" + sufixo,
                "Receitas", TipoPlanoConta.SINTETICA));
        var nivel2 = service.criar(request(empresa.getId(), raiz.id(), "3.1-" + sufixo,
                "Receitas Operacionais", TipoPlanoConta.SINTETICA));
        var nivel3 = service.criar(request(empresa.getId(), nivel2.id(), "3.1.1-" + sufixo,
                "Venda de Produtos", TipoPlanoConta.ANALITICA));

        assertThat(nivel3.caminhoCompleto())
                .isEqualTo("Receitas > Receitas Operacionais > Venda de Produtos");
        assertThat(service.listar(empresa.getId(), nivel2.id(), null, null,
                TipoPlanoConta.ANALITICA, NaturezaPlanoConta.RECEITA, true,
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent())
                .extracting("id")
                .containsExactly(nivel3.id());

        assertThatThrownBy(() -> service.atualizar(raiz.id(),
                request(empresa.getId(), nivel3.id(), raiz.codigo(),
                        raiz.nome(), TipoPlanoConta.SINTETICA)))
                .isInstanceOf(InvalidOperationException.class);
    }

    private PlanoContaRequest request(UUID empresaId, UUID paiId, String codigo,
            String nome, TipoPlanoConta tipo) {
        return new PlanoContaRequest(empresaId, paiId, codigo, nome, null,
                tipo, NaturezaPlanoConta.RECEITA);
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return ("87654321000000" + numeros).substring(numeros.length());
    }
}
