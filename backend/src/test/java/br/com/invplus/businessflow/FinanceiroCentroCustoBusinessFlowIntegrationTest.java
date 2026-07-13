package br.com.invplus.businessflow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import br.com.invplus.support.context.AbstractContextoEmpresarialIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaPagarRequest;
import br.com.invplus.modules.financeiro.dto.request.ContaReceberRequest;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.modules.financeiro.entity.ParcelaPagar;
import br.com.invplus.modules.financeiro.entity.ParcelaReceber;
import br.com.invplus.modules.financeiro.entity.Recebimento;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.relatorio.FinanceiroResumoService;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.MovimentoFinanceiroRepository;
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

/**
 * Prova a classificacao e propagacao de CentroCusto no ciclo financeiro.
 */
@SuppressWarnings("unused")
@SpringBootTest
@ActiveProfiles("test")
class FinanceiroCentroCustoBusinessFlowIntegrationTest extends AbstractContextoEmpresarialIntegrationTest {

    private static final LocalDate DATA = LocalDate.of(2098, 6, 15);

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
    private CentroCustoRepository centroCustoRepository;
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
    void deveClassificarContasPropagarMovimentosFiltrarERelatar() {
        String sufixo = sufixo();
        Empresa empresa = empresaRepository.save(empresa("Empresa CC Financeiro " + sufixo, "12" + sufixo));
        ativarEmpresa(empresa.getId());
        Empresa outraEmpresa = empresaRepository.save(empresa("Outra Empresa CC " + sufixo, "34" + sufixo));
        Cliente cliente = clienteRepository.save(cliente("Cliente CC " + sufixo, "56" + sufixo));
        Fornecedor fornecedor = fornecedorRepository.save(fornecedor("Fornecedor CC " + sufixo, "78" + sufixo));
        CentroCusto receita = centroCustoRepository.save(centro(empresa, "REC-" + sufixo,
                "Receitas", TipoCentroCusto.RECEITA));
        CentroCusto despesa = centroCustoRepository.save(centro(empresa, "DES-" + sufixo,
                "Despesas", TipoCentroCusto.DESPESA));
        CentroCusto outraEmpresaCentro = centroCustoRepository.save(centro(outraEmpresa, "OUT-" + sufixo,
                "Outra empresa", TipoCentroCusto.AMBOS));

        var receberResponse = contaReceberService.criar(contaReceberRequest(
                empresa, cliente, receita, "CR-" + sufixo));
        var pagarResponse = contaPagarService.criar(contaPagarRequest(
                empresa, fornecedor, despesa, "CP-" + sufixo));

        assertThat(receberResponse.centroCustoId()).isEqualTo(receita.getId());
        assertThat(pagarResponse.centroCustoId()).isEqualTo(despesa.getId());
        assertThat(contaReceberService.listarComFiltros(null, null, null, receita.getId(),
                null, null, null, null, null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(contaPagarService.listarComFiltros(null, null, null, despesa.getId(),
                null, null, null, null, null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);

        assertThatThrownBy(() -> contaReceberService.criar(contaReceberRequest(
                empresa, cliente, despesa, "CR-INV-" + sufixo)))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> contaPagarService.criar(contaPagarRequest(
                empresa, fornecedor, receita, "CP-INV-" + sufixo)))
                .isInstanceOf(InvalidOperationException.class);
        assertThatThrownBy(() -> contaReceberService.criar(contaReceberRequest(
                empresa, cliente, outraEmpresaCentro, "CR-OUT-" + sufixo)))
                .isInstanceOf(InvalidOperationException.class);

        ContaReceber contaReceber = contaReceberRepository.findById(receberResponse.id()).orElseThrow();
        ContaPagar contaPagar = contaPagarRepository.findById(pagarResponse.id()).orElseThrow();
        FormaPagamento forma = formaPagamentoRepository.save(formaPagamento(sufixo));
        Recebimento recebimento = recebimento(contaReceber, forma);
        Pagamento pagamento = pagamento(contaPagar, forma);

        var entrada = movimentoService.registrarRecebimento(recebimento);
        var saida = movimentoService.registrarPagamento(pagamento);

        assertThat(entrada.getCentroCusto().getId()).isEqualTo(receita.getId());
        assertThat(saida.getCentroCusto().getId()).isEqualTo(despesa.getId());
        assertThat(movimentoService.listarComFiltros(null, null,
                OrigemMovimentoFinanceiro.RECEBIMENTO, null, null, null, null, receita.getId(),
                null, null, null, null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);

        var fluxoReceita = resumoService.fluxoCaixa(DATA, DATA, receita.getId());
        var fluxoDespesa = resumoService.fluxoCaixa(DATA, DATA, despesa.getId());
        assertThat(fluxoReceita.totalEntradas()).isEqualByComparingTo("500.00");
        assertThat(fluxoReceita.totalSaidas()).isZero();
        assertThat(fluxoDespesa.totalSaidas()).isEqualByComparingTo("200.00");
        assertThat(fluxoDespesa.totalEntradas()).isZero();
        assertThat(movimentoRepository.findById(entrada.getId())).isPresent();
    }

    private ContaReceberRequest contaReceberRequest(
            Empresa empresa, Cliente cliente, CentroCusto centro, String numero) {
        return new ContaReceberRequest(null, cliente.getId(), null, centro.getId(), null,
                numero, "Conta classificada", DATA, new BigDecimal("500.00"), null);
    }

    private ContaPagarRequest contaPagarRequest(
            Empresa empresa, Fornecedor fornecedor, CentroCusto centro, String numero) {
        return new ContaPagarRequest(null, fornecedor.getId(), null, centro.getId(), null,
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

    private CentroCusto centro(Empresa empresa, String codigo, String nome, TipoCentroCusto tipo) {
        CentroCusto centro = new CentroCusto();
        centro.atualizar(empresa, null, codigo, nome, null, tipo);
        return centro;
    }

    private FormaPagamento formaPagamento(String sufixo) {
        FormaPagamento forma = new FormaPagamento();
        forma.atualizar("CC-" + sufixo, "PIX Centro Custo", TipoFormaPagamento.PIX, false, false);
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
