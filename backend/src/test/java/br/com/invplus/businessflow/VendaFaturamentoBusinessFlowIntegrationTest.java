package br.com.invplus.businessflow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.modules.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.modules.estoque.service.ReservaEstoqueService;
import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.financeiro.repository.ParcelaReceberRepository;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamentoParcela;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.vendas.dto.request.VendaFaturamentoRequest;
import br.com.invplus.modules.vendas.dto.response.VendaFaturamentoResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.enums.StatusVendaFaturamento;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.modules.vendas.service.VendaFaturamentoService;
import br.com.invplus.support.context.TestEmpresaContext;

/**
 * Prova o fluxo PedidoVenda aprovado -> Faturamento -> baixa de ReservaEstoque -> MovimentacaoEstoque.
 */
@SpringBootTest
@ActiveProfiles("test")
class VendaFaturamentoBusinessFlowIntegrationTest {

    @Autowired
    private VendaFaturamentoService faturamentoService;
    @Autowired
    private ReservaEstoqueService reservaService;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UnidadeMedidaRepository unidadeRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private DepositoRepository depositoRepository;
    @Autowired
    private LocalizacaoEstoqueRepository localizacaoRepository;
    @Autowired
    private EstoqueRepository estoqueRepository;
    @Autowired
    private PedidoVendaRepository pedidoRepository;
    @Autowired
    private ReservaEstoqueRepository reservaRepository;
    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired
    private CondicaoPagamentoRepository condicaoPagamentoRepository;
    @Autowired
    private ContaReceberRepository contaReceberRepository;
    @Autowired
    private ParcelaReceberRepository parcelaReceberRepository;
    @Autowired
    private TestEmpresaContext testEmpresaContext;

    @Test
    void deveFaturarPedidoAprovadoBaixandoReservaEAtualizandoSaldo() {
        PedidoVenda pedido = criarPedidoAprovado();
        testEmpresaContext.ativarEmpresa(UUID.randomUUID(), pedido.getEmpresa().getId());
        Estoque estoque = criarEstoque(pedido.getEmpresa());
        reservaService.criar(new ReservaEstoqueRequest(
                estoque.getId(),
                new BigDecimal("20.000"),
                MovimentacaoOrigem.VENDA,
                pedido.getId(),
                null,
                "Reserva do fluxo de faturamento"
        ));

        VendaFaturamentoResponse response = faturamentoService.faturar(new VendaFaturamentoRequest(
                pedido.getId(),
                condicaoPagamento().getId(),
                "VF-FLOW-" + codigoCurto(),
                "Conta gerada pelo fluxo",
                "Faturamento do fluxo"
        ));

        PedidoVenda pedidoAtualizado = pedidoRepository.findById(pedido.getId()).orElseThrow();
        Estoque estoqueAtualizado = estoqueRepository.findById(estoque.getId()).orElseThrow();
        var reservas = reservaRepository.findByOrigemAndOrigemIdAndAtivoTrueAndDeletedAtIsNull(
                MovimentacaoOrigem.VENDA,
                pedido.getId()
        );
        var movimentacoes = movimentacaoRepository.findByOrigemAndOrigemIdOrderByDataMovimentacaoDesc(
                MovimentacaoOrigem.VENDA,
                pedido.getId()
        );

        assertThat(response.status()).isEqualTo(StatusVendaFaturamento.FATURADO);
        assertThat(response.contaReceberId()).isNotNull();
        assertThat(pedidoAtualizado.getStatus()).isEqualTo(StatusPedidoVenda.FATURADO);
        assertThat(contaReceberRepository.findByVendaFaturamentoIdAndAtivoTrueAndDeletedAtIsNull(response.id()))
                .isPresent()
                .get()
                .satisfies(conta -> {
                    assertThat(conta.getValorTotal()).isEqualByComparingTo("200.00");
                    assertThat(parcelaReceberRepository.findByContaReceberIdAndAtivoTrueAndDeletedAtIsNull(conta.getId()))
                            .hasSize(1)
                            .first()
                            .satisfies(parcela -> assertThat(parcela.getValor()).isEqualByComparingTo("200.00"));
                });
        assertThat(reservas).hasSize(1);
        assertThat(reservas.getFirst().getStatus()).isEqualTo(StatusReservaEstoque.BAIXADA);
        assertThat(estoqueAtualizado.getQuantidade()).isEqualByComparingTo("80.000");
        assertThat(estoqueAtualizado.getQuantidadeReservada()).isEqualByComparingTo("0.000");
        assertThat(estoqueAtualizado.getQuantidadeDisponivel()).isEqualByComparingTo("80.000");
        assertThat(movimentacoes).anyMatch(movimentacao ->
                movimentacao.getTipoMovimentacao() == TipoMovimentacaoEstoque.BAIXA_RESERVA);
    }

    private PedidoVenda criarPedidoAprovado() {
        Empresa empresa = empresa();
        Cliente cliente = cliente();
        PedidoVenda pedido = new PedidoVenda();
        pedido.setEmpresa(empresa);
        pedido.setCliente(cliente);
        pedido.atualizar("PV-FLOW-FAT-" + codigoCurto(), LocalDate.now(), null, BigDecimal.ZERO, BigDecimal.ZERO, "Pedido");
        pedido.atualizarValorTotal(new BigDecimal("200.00"));
        pedido.setStatus(StatusPedidoVenda.APROVADO);
        return pedidoRepository.saveAndFlush(pedido);
    }

    private Estoque criarEstoque(Empresa empresa) {
        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-" + codigoCurto(), "Deposito Fluxo Fat", "Teste", TipoDeposito.CENTRAL,
                false, true, true, true, true, false);
        deposito = depositoRepository.saveAndFlush(deposito);

        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setDeposito(deposito);
        localizacao.atualizar("LOC-" + codigoCurto(), "Rua A", "Rua A", "01", "01", "01", "01",
                false, false, "Teste");
        localizacao = localizacaoRepository.saveAndFlush(localizacao);

        Estoque estoque = new Estoque();
        estoque.setProduto(produto());
        estoque.setEmpresa(empresa);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(new BigDecimal("100.000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null, null, null);
        return estoqueRepository.saveAndFlush(estoque);
    }

    private Empresa empresa() {
        Empresa empresa = new Empresa();
        String cnpj = cnpjUnico("64");
        empresa.atualizar("Empresa Fluxo Fat " + cnpj, "Empresa Fluxo Fat", cnpj, "ISENTO", null,
                "NORMAL", 3, "71999990000", "empresa-fluxo-fat-" + cnpj + "@teste.com");
        return empresaRepository.saveAndFlush(empresa);
    }

    private Cliente cliente() {
        String cnpj = cnpjUnico("63");
        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente Fluxo Fat " + cnpj, "Cliente Fluxo Fat", cnpj, "ISENTO",
                "7133334444", null, "cliente-fluxo-fat-" + cnpj + "@teste.com", null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.atualizar(BigDecimal.ZERO, null, null, "Cliente fluxo faturamento");
        return clienteRepository.saveAndFlush(cliente);
    }

    private Produto produto() {
        Categoria categoria = new Categoria();
        categoria.atualizar(null, "Categoria Fluxo Fat " + codigoCurto(), "Teste", 0);
        categoria = categoriaRepository.saveAndFlush(categoria);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("F" + codigoCurto().substring(0, 6), "Unidade fluxo fat", (short) 0);
        unidade = unidadeRepository.saveAndFlush(unidade);

        Produto produto = new Produto();
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-FLOW-FAT-" + codigoCurto(), null, null, null, "Produto Fluxo Fat", "Teste",
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, false, true, true, true);
        return produtoRepository.saveAndFlush(produto);
    }

    private CondicaoPagamento condicaoPagamento() {
        CondicaoPagamento condicao = new CondicaoPagamento();
        condicao.atualizar("AV-FLOW-" + codigoCurto(), "A vista fluxo", BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, RegraVencimento.MANTER_DATA, true);
        CondicaoPagamentoParcela parcela = new CondicaoPagamentoParcela();
        parcela.atualizar(condicao, 1, 0, new BigDecimal("100.0000"));
        condicao.substituirParcelas(java.util.List.of(parcela));
        return condicaoPagamentoRepository.saveAndFlush(condicao);
    }

    private String codigoCurto() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String cnpjUnico(String prefixo) {
        return prefixo + "%012d".formatted(Math.abs(UUID.randomUUID().getMostSignificantBits()) % 1_000_000_000_000L);
    }

    @AfterEach
    @SuppressWarnings("unused")
    void limparContexto() {
        testEmpresaContext.limpar();
    }
}
