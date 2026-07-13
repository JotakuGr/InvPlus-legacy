package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.financeiro.relatorio.FinanceiroResumoService;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.service.MovimentoFinanceiroService;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.FornecedorRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.support.context.AbstractContextoEmpresarialIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida a DRE usando movimentos financeiros reais classificados por plano de conta.
 */
@SpringBootTest
@ActiveProfiles("test")
class FinanceiroDreBusinessFlowIntegrationTest extends AbstractContextoEmpresarialIntegrationTest {

    private static final LocalDate DATA = LocalDate.of(2098, 8, 10);

    @Autowired
    private MovimentoFinanceiroService movimentoService;
    @Autowired
    private FinanceiroResumoService resumoService;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;
    @Autowired
    private PlanoContaRepository planoContaRepository;
    @Autowired
    private CentroCustoRepository centroCustoRepository;

    @Test
    @Transactional
    void deveGerarDreAgrupadaPorPlanoContaComFiltrosEIgnorarCancelados() {
        String sufixo = sufixo();
        Empresa empresa = empresaRepository.save(empresa("Empresa DRE " + sufixo, "11" + sufixo));
        Empresa outraEmpresa = empresaRepository.save(empresa("Outra Empresa DRE " + sufixo, "22" + sufixo));
        ativarEmpresa(empresa.getId());
        Cliente cliente = clienteRepository.save(cliente("Cliente DRE " + sufixo, "33" + sufixo));
        Fornecedor fornecedor = fornecedorRepository.save(fornecedor("Fornecedor DRE " + sufixo, "44" + sufixo));
        FormaPagamento forma = formaPagamentoRepository.save(formaPagamento(sufixo));
        CentroCusto centro = centroCustoRepository.save(centro(empresa, "DRE-" + sufixo));
        CentroCusto outroCentro = centroCustoRepository.save(centro(empresa, "DRE-OUT-" + sufixo));
        PlanoConta receita = planoContaRepository.save(plano(empresa, "3.1." + sufixo,
                "Receita DRE", NaturezaPlanoConta.RECEITA));
        PlanoConta despesa = planoContaRepository.save(plano(empresa, "4.1." + sufixo,
                "Despesa DRE", NaturezaPlanoConta.DESPESA));

        movimentoService.criarManual(movimento(empresa, forma, cliente, null, centro, receita,
                TipoMovimentoFinanceiro.ENTRADA, new BigDecimal("1000.00"), "Receita principal"));
        movimentoService.criarManual(movimento(empresa, forma, null, fornecedor, centro, despesa,
                TipoMovimentoFinanceiro.SAIDA, new BigDecimal("300.00"), "Despesa principal"));
        movimentoService.criarManual(movimento(empresa, forma, cliente, null, outroCentro, receita,
                TipoMovimentoFinanceiro.ENTRADA, new BigDecimal("200.00"), "Receita outro centro"));
        ativarEmpresa(outraEmpresa.getId());
        movimentoService.criarManual(movimento(outraEmpresa, forma, cliente, null, null, null,
                TipoMovimentoFinanceiro.ENTRADA, new BigDecimal("900.00"), "Sem plano ignorado"));
        ativarEmpresa(empresa.getId());
        var cancelado = movimentoService.criarManual(movimento(empresa, forma, cliente, null, centro, receita,
                TipoMovimentoFinanceiro.ENTRADA, new BigDecimal("500.00"), "Receita cancelada"));
        movimentoService.cancelar(cancelado.id());

        var dreEmpresa = resumoService.gerarDre(DATA, DATA, null, null, null);
        var dreCentro = resumoService.gerarDre(DATA, DATA, null, centro.getId(), null);
        var drePlanoReceita = resumoService.gerarDre(DATA, DATA, null, null, receita.getId());

        assertThat(dreEmpresa.receitaBruta()).isEqualByComparingTo("1200.00");
        assertThat(dreEmpresa.despesasOperacionais()).isEqualByComparingTo("300.00");
        assertThat(dreEmpresa.resultadoOperacional()).isEqualByComparingTo("900.00");
        assertThat(dreEmpresa.receitas()).extracting("codigo").containsExactly(receita.getCodigo());
        assertThat(dreEmpresa.despesas()).extracting("codigo").containsExactly(despesa.getCodigo());

        assertThat(dreCentro.receitaBruta()).isEqualByComparingTo("1000.00");
        assertThat(dreCentro.despesasOperacionais()).isEqualByComparingTo("300.00");
        assertThat(drePlanoReceita.receitaBruta()).isEqualByComparingTo("1200.00");
        assertThat(drePlanoReceita.despesasOperacionais()).isZero();
    }

    private MovimentoFinanceiroRequest movimento(Empresa empresa, FormaPagamento forma, Cliente cliente,
            Fornecedor fornecedor, CentroCusto centro, PlanoConta plano, TipoMovimentoFinanceiro tipo,
            BigDecimal valor, String descricao) {
        return new MovimentoFinanceiroRequest(null, forma.getId(),
                cliente == null ? null : cliente.getId(),
                fornecedor == null ? null : fornecedor.getId(),
                centro == null ? null : centro.getId(),
                plano == null ? null : plano.getId(),
                null,
                tipo, OrigemMovimentoFinanceiro.AJUSTE_MANUAL, UUID.randomUUID(), valor, DATA, descricao, null);
    }

    private PlanoConta plano(Empresa empresa, String codigo, String nome, NaturezaPlanoConta natureza) {
        PlanoConta plano = new PlanoConta();
        plano.atualizar(empresa, null, codigo, nome, null, TipoPlanoConta.ANALITICA, natureza);
        return plano;
    }

    private CentroCusto centro(Empresa empresa, String codigo) {
        CentroCusto centro = new CentroCusto();
        centro.atualizar(empresa, null, codigo, "Centro " + codigo, null, TipoCentroCusto.AMBOS);
        return centro;
    }

    private FormaPagamento formaPagamento(String sufixo) {
        FormaPagamento forma = new FormaPagamento();
        forma.atualizar("DRE-" + sufixo, "PIX DRE", TipoFormaPagamento.PIX, false, false);
        return forma;
    }

    private Empresa empresa(String nome, String base) {
        return new Empresa(nome, nome, documento(base), null, null, null);
    }

    private Cliente cliente(String nome, String base) {
        Pessoa pessoa = pessoaRepository.save(pessoa(nome, base));
        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.atualizar(BigDecimal.ZERO, null, null, null);
        return cliente;
    }

    private Fornecedor fornecedor(String nome, String base) {
        Pessoa pessoa = pessoaRepository.save(pessoa(nome, base));
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoa(pessoa);
        fornecedor.atualizar(null, 0, null);
        return fornecedor;
    }

    private Pessoa pessoa(String nome, String base) {
        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, nome, null, documento(base),
                null, null, null, null, null);
        return pessoa;
    }

    private String documento(String base) {
        String numeros = base.replaceAll("\\D", "");
        return (numeros + "00000000000000").substring(0, 14);
    }

    private String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
