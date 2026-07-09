package br.com.invplus.businessflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaPagarRequest;
import br.com.invplus.modules.financeiro.dto.request.ContaReceberRequest;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.entity.Recebimento;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.financeiro.relatorio.FinanceiroResumoService;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.service.ContaPagarService;
import br.com.invplus.modules.financeiro.service.ContaReceberService;
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
 * Prova a classificacao contabil por PlanoConta no ciclo financeiro.
 */
@SpringBootTest
@ActiveProfiles("test")
class FinanceiroPlanoContaBusinessFlowIntegrationTest {

    private static final LocalDate DATA = LocalDate.of(2098, 7, 9);

    @Autowired
    private ContaReceberService contaReceberService;
    @Autowired
    private ContaPagarService contaPagarService;
    @Autowired
    private MovimentoFinanceiroService movimentoService;
    @Autowired
    private FinanceiroResumoService resumoService;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PlanoContaRepository planoContaRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;
    @Autowired
    private ContaReceberRepository contaReceberRepository;
    @Autowired
    private ContaPagarRepository contaPagarRepository;
    @Autowired
    private MovimentoFinanceiroRepository movimentoRepository;

    @Test
    @Transactional
    void deveClassificarContasPropagarMovimentosFiltrarERelatarPorPlanoConta() {
        String sufixo = sufixo();
        Empresa empresa = empresaRepository.save(empresa("Empresa PC Financeiro " + sufixo, "12" + sufixo));
        Empresa outraEmpresa = empresaRepository.save(empresa("Outra Empresa PC " + sufixo, "34" + sufixo));
        Cliente cliente = clienteRepository.save(cliente("Cliente PC " + sufixo, "56" + sufixo));
        Fornecedor fornecedor = fornecedorRepository.save(fornecedor("Fornecedor PC " + sufixo, "78" + sufixo));
        PlanoConta receita = planoContaRepository.save(plano(empresa, "3.1." + sufixo,
                "Receita de Vendas", TipoPlanoConta.ANALITICA, NaturezaPlanoConta.RECEITA));
        PlanoConta despesa = planoContaRepository.save(plano(empresa, "4.1." + sufixo,
                "Despesa com Fornecedor", TipoPlanoConta.ANALITICA, NaturezaPlanoConta.DESPESA));
        PlanoConta sintetica = planoContaRepository.save(plano(empresa, "3." + sufixo,
                "Receitas", TipoPlanoConta.SINTETICA, NaturezaPlanoConta.RECEITA));
        PlanoConta outraEmpresaPlano = planoContaRepository.save(plano(outraEmpresa, "9." + sufixo,
                "Outra empresa", TipoPlanoConta.ANALITICA, NaturezaPlanoConta.RECEITA));

        var receberResponse = contaReceberService.criar(contaReceberRequest(
                empresa, cliente, receita, "CR-PC-" + sufixo));
        var pagarResponse = contaPagarService.criar(contaPagarRequest(
                empresa, fornecedor, despesa, "CP-PC-" + sufixo));

        assertThat(receberResponse.planoContaId()).isEqualTo(receita.getId());
        assertThat(receberResponse.planoContaNatureza()).isEqualTo(NaturezaPlanoConta.RECEITA);
        assertThat(pagarResponse.planoContaId()).isEqualTo(despesa.getId());
        assertThat(pagarResponse.planoContaNatureza()).isEqualTo(NaturezaPlanoConta.DESPESA);
        assertThat(contaReceberService.listarComFiltros(null, null, null, null, null, receita.getId(),
                null, null, null, null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(contaPagarService.listarComFiltros(null, null, null, null, null, despesa.getId(),
                null, null, null, null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);

        assertThatThrownBy(() -> contaReceberService.criar(contaReceberRequest(
                empresa, cliente, despesa, "CR-PC-INV-" + sufixo)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("RECEITA");
        assertThatThrownBy(() -> contaPagarService.criar(contaPagarRequest(
                empresa, fornecedor, receita, "CP-PC-INV-" + sufixo)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("DESPESA");
        assertThatThrownBy(() -> contaReceberService.criar(contaReceberRequest(
                empresa, cliente, sintetica, "CR-PC-SIN-" + sufixo)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ANALITICA");
        assertThatThrownBy(() -> contaReceberService.criar(contaReceberRequest(
                empresa, cliente, outraEmpresaPlano, "CR-PC-OUT-" + sufixo)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("mesma empresa");

        ContaReceber contaReceber = contaReceberRepository.findById(receberResponse.id()).orElseThrow();
        ContaPagar contaPagar = contaPagarRepository.findById(pagarResponse.id()).orElseThrow();
        FormaPagamento forma = formaPagamentoRepository.save(formaPagamento(sufixo));
        Recebimento recebimento = recebimento(contaReceber, forma);
        Pagamento pagamento = pagamento(contaPagar, forma);

        var entrada = movimentoService.registrarRecebimento(recebimento);
        var saida = movimentoService.registrarPagamento(pagamento);

        assertThat(entrada.getPlanoConta().getId()).isEqualTo(receita.getId());
        assertThat(saida.getPlanoConta().getId()).isEqualTo(despesa.getId());
        assertThat(movimentoService.listarComFiltros(null, null, TipoMovimentoFinanceiro.ENTRADA,
                OrigemMovimentoFinanceiro.RECEBIMENTO, null, null, null, null, null, receita.getId(),
                null, null, null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);

        var fluxoReceita = resumoService.fluxoCaixa(DATA, DATA, null, receita.getId());
        var fluxoDespesa = resumoService.fluxoCaixa(DATA, DATA, null, despesa.getId());
        assertThat(fluxoReceita.totalEntradas()).isEqualByComparingTo("500.00");
        assertThat(fluxoReceita.totalSaidas()).isZero();
        assertThat(fluxoDespesa.totalSaidas()).isEqualByComparingTo("200.00");
        assertThat(fluxoDespesa.totalEntradas()).isZero();
        assertThat(movimentoRepository.findById(entrada.getId())).isPresent();
    }

    private ContaReceberRequest contaReceberRequest(
            Empresa empresa, Cliente cliente, PlanoConta plano, String numero) {
        return new ContaReceberRequest(empresa.getId(), null, cliente.getId(), null, null, plano.getId(),
                numero, "Conta classificada", DATA, new BigDecimal("500.00"), null);
    }

    private ContaPagarRequest contaPagarRequest(
            Empresa empresa, Fornecedor fornecedor, PlanoConta plano, String numero) {
        return new ContaPagarRequest(empresa.getId(), null, fornecedor.getId(), null, null, plano.getId(),
                numero, "Conta classificada", DATA, new BigDecimal("200.00"), null);
    }

    private Recebimento recebimento(ContaReceber conta, FormaPagamento forma) {
        ParcelaReceber parcela = new ParcelaReceber();
        parcela.setContaReceber(conta);
        Recebimento recebimento = new Recebimento();
        recebimento.setId(UUID.randomUUID());
        recebimento.registrar(parcela, forma, new BigDecimal("500.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, DATA, null);
        return recebimento;
    }

    private Pagamento pagamento(ContaPagar conta, FormaPagamento forma) {
        ParcelaPagar parcela = new ParcelaPagar();
        parcela.setContaPagar(conta);
        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.registrar(parcela, forma, new BigDecimal("200.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, DATA, null);
        return pagamento;
    }

    private PlanoConta plano(Empresa empresa, String codigo, String nome,
            TipoPlanoConta tipo, NaturezaPlanoConta natureza) {
        PlanoConta plano = new PlanoConta();
        plano.atualizar(empresa, null, codigo, nome, null, tipo, natureza);
        return plano;
    }

    private FormaPagamento formaPagamento(String sufixo) {
        FormaPagamento forma = new FormaPagamento();
        forma.atualizar("PC-" + sufixo, "PIX Plano Conta", TipoFormaPagamento.PIX, false, false);
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
