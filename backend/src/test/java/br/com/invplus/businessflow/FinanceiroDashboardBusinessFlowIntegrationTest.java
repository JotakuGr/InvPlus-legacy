package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusContaPagar;
import br.com.invplus.modules.financeiro.enums.StatusContaReceber;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.relatorio.FinanceiroResumoService;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida o dashboard usando movimentos e titulos persistidos no fluxo financeiro real.
 */
@SpringBootTest
@ActiveProfiles("test")
class FinanceiroDashboardBusinessFlowIntegrationTest {

    private static final LocalDate DATA = LocalDate.of(2099, 12, 30);

    @Autowired
    private FinanceiroResumoService service;
    @Autowired
    private MovimentoFinanceiroRepository movimentoRepository;
    @Autowired
    private ContaReceberRepository contaReceberRepository;
    @Autowired
    private ContaPagarRepository contaPagarRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Test
    @Transactional
    void deveConsolidarFluxoContasAbertasEVencidasIgnorandoCancelados() {
        var antes = service.dashboard(DATA, DATA);
        Empresa empresa = empresaRepository.save(empresa());
        FormaPagamento forma = formaPagamentoRepository.save(formaPagamento());
        Cliente cliente = clienteRepository.save(cliente());
        Fornecedor fornecedor = fornecedorRepository.save(fornecedor());

        movimentoRepository.save(movimento(empresa, forma, TipoMovimentoFinanceiro.ENTRADA, "1000.00", false));
        movimentoRepository.save(movimento(empresa, forma, TipoMovimentoFinanceiro.SAIDA, "350.00", false));
        movimentoRepository.save(movimento(empresa, forma, TipoMovimentoFinanceiro.ENTRADA, "999.00", true));

        ContaReceber receber = new ContaReceber();
        receber.abrir(empresa, null, cliente, null, "CR-DASH-" + sufixo(), "Conta vencida dashboard",
                DATA, new BigDecimal("400.00"), null);
        receber.setStatus(StatusContaReceber.VENCIDA);
        contaReceberRepository.save(receber);

        ContaPagar pagar = new ContaPagar();
        pagar.abrir(empresa, null, fornecedor, null, "CP-DASH-" + sufixo(), "Conta vencida dashboard",
                DATA, new BigDecimal("250.00"), null);
        pagar.setStatus(StatusContaPagar.VENCIDA);
        contaPagarRepository.save(pagar);
        movimentoRepository.flush();

        var depois = service.dashboard(DATA, DATA);

        assertThat(depois.entradasPeriodo().subtract(antes.entradasPeriodo())).isEqualByComparingTo("1000.00");
        assertThat(depois.saidasPeriodo().subtract(antes.saidasPeriodo())).isEqualByComparingTo("350.00");
        assertThat(depois.saldoPeriodo().subtract(antes.saldoPeriodo())).isEqualByComparingTo("650.00");
        assertThat(depois.contasReceberAberto().subtract(antes.contasReceberAberto()))
                .isEqualByComparingTo("400.00");
        assertThat(depois.contasPagarAberto().subtract(antes.contasPagarAberto()))
                .isEqualByComparingTo("250.00");
        assertThat(depois.contasReceberVencidas()).isEqualTo(antes.contasReceberVencidas() + 1);
        assertThat(depois.contasPagarVencidas()).isEqualTo(antes.contasPagarVencidas() + 1);
    }

    private MovimentoFinanceiro movimento(Empresa empresa, FormaPagamento forma,
            TipoMovimentoFinanceiro tipo, String valor, boolean cancelar) {
        MovimentoFinanceiro movimento = new MovimentoFinanceiro();
        movimento.registrar(empresa, null, tipo, OrigemMovimentoFinanceiro.AJUSTE_MANUAL,
                UUID.randomUUID(), forma, null, null, new BigDecimal(valor), DATA,
                "Movimento dashboard", null);
        if (cancelar) {
            movimento.cancelar();
        }
        return movimento;
    }

    private Empresa empresa() {
        String sufixo = sufixo();
        return new Empresa("Empresa Dashboard " + sufixo, "Empresa Dashboard " + sufixo,
                documento("12", sufixo), null, null, null);
    }

    private FormaPagamento formaPagamento() {
        FormaPagamento forma = new FormaPagamento();
        String sufixo = sufixo();
        forma.atualizar("D" + sufixo, "Forma Dashboard " + sufixo, TipoFormaPagamento.PIX, false, false);
        return forma;
    }

    private Cliente cliente() {
        Pessoa pessoa = pessoaRepository.save(pessoa("Cliente Dashboard", "34"));
        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.atualizar(BigDecimal.ZERO, null, null, null);
        return cliente;
    }

    private Fornecedor fornecedor() {
        Pessoa pessoa = pessoaRepository.save(pessoa("Fornecedor Dashboard", "56"));
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoa(pessoa);
        fornecedor.atualizar(null, 0, null);
        return fornecedor;
    }

    private Pessoa pessoa(String nome, String prefixo) {
        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, nome + " " + sufixo(), null,
                documento(prefixo, sufixo()), null, null, null, null, null);
        return pessoa;
    }

    private String documento(String prefixo, String base) {
        String numeros = base.replaceAll("\\D", "");
        String valor = prefixo + numeros + "00000000000000";
        return valor.substring(0, 14);
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
