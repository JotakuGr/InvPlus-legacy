package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaPagarRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaPagarResponse;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.service.ContaPagarService;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import br.com.invplus.support.context.AbstractContextoEmpresarialIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prova o fluxo Empresa -> Pessoa -> Fornecedor -> ContaPagar.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ContaPagarBusinessFlowIntegrationTest extends AbstractContextoEmpresarialIntegrationTest {

    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private ContaPagarService contaPagarService;

    @Test
    void deveCriarConsultarECancelarObrigacaoDoFornecedor() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Empresa empresa = empresaRepository.save(new Empresa("Empresa Fluxo " + sufixo,
                "Empresa Fluxo " + sufixo, documento("11", "99"), null, null, null));
        ativarEmpresa(empresa.getId());

        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Fornecedor Fluxo " + sufixo, null,
                documento("77", "88"), null, null, null, null, null);
        pessoa = pessoaRepository.save(pessoa);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoa(pessoa);
        fornecedor.atualizar(null, 5, null);
        fornecedor = fornecedorRepository.save(fornecedor);

        ContaPagarRequest request = new ContaPagarRequest(null, fornecedor.getId(), null, null, null,
                "CP-FLUXO-" + sufixo, "Compra de mercadorias", LocalDate.of(2026, 7, 8),
                new BigDecimal("750.00"), "Fluxo integrado");
        ContaPagarResponse criada = contaPagarService.criar(request);

        assertThat(contaPagarService.buscar(criada.id()).fornecedorId()).isEqualTo(fornecedor.getId());
        assertThat(contaPagarService.cancelar(criada.id()).status()).isEqualTo(StatusContaPagar.CANCELADA);
    }

    private String documento(String prefixo, String sufixo) {
        String meio = String.format("%010d", Math.abs((long) UUID.randomUUID().hashCode()) % 10_000_000_000L);
        return prefixo + meio + sufixo;
    }
}
