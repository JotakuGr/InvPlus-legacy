package br.com.invplus.businessflow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import br.com.invplus.common.enums.TipoPessoa;
import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.UnidadeMedidaRepository;
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
import br.com.invplus.modules.financeiro.enums.RegraVencimento;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamentoParcela;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Pessoa;
import br.com.invplus.modules.pessoas.repository.ClienteRepository;
import br.com.invplus.modules.pessoas.repository.PessoaRepository;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoPrecoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoUnidadeRepository;
import br.com.invplus.modules.vendas.dto.request.PedidoVendaItemRequest;
import br.com.invplus.modules.vendas.dto.request.PedidoVendaRequest;
import br.com.invplus.modules.vendas.dto.request.VendaFaturamentoRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaResponse;
import br.com.invplus.modules.vendas.dto.response.VendaFaturamentoResponse;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.service.PedidoVendaItemService;
import br.com.invplus.modules.vendas.service.PedidoVendaService;
import br.com.invplus.modules.vendas.service.VendaFaturamentoService;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import br.com.invplus.support.context.TestEmpresaContext;

/** Prova o isolamento empresarial no ciclo venda, estoque e financeiro. */
@SpringBootTest
@ActiveProfiles("test")
class VendaContextoEmpresarialBusinessFlowIntegrationTest {

    @Autowired private TestEmpresaContext empresaContext;
    @Autowired private PedidoVendaService pedidoService;
    @Autowired private PedidoVendaItemService itemService;
    @Autowired private VendaFaturamentoService faturamentoService;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadeMedidaRepository unidadeRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ProdutoPrecoRepository precoRepository;
    @Autowired private ProdutoUnidadeRepository produtoUnidadeRepository;
    @Autowired private DepositoRepository depositoRepository;
    @Autowired private LocalizacaoEstoqueRepository localizacaoRepository;
    @Autowired private EstoqueRepository estoqueRepository;
    @Autowired private ReservaEstoqueRepository reservaRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired private CondicaoPagamentoRepository condicaoRepository;
    @Autowired private ContaReceberRepository contaReceberRepository;

    @Test
    void devePreservarEmpresaEmTodoOCicloEIsolarAConsultaAposTrocaDeContexto() {
        Empresa empresaA = empresa("A");
        Empresa empresaB = empresa("B");
        UUID usuarioId = UUID.randomUUID();
        empresaContext.ativarEmpresa(usuarioId, empresaA.getId());

        Cliente cliente = cliente();
        Produto produto = produto();
        ProdutoPreco preco = preco(empresaA, produto);
        ProdutoUnidade unidade = produtoUnidade(produto);
        Estoque estoque = estoque(empresaA, produto);

        PedidoVendaResponse pedido = pedidoService.criar(new PedidoVendaRequest(
                null,
                cliente.getId(),
                null,
                "PV-CTX-" + codigo(),
                LocalDate.of(2026, 7, 10),
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "Venda da empresa A"
        ));
        itemService.criar(new PedidoVendaItemRequest(
                pedido.id(),
                produto.getId(),
                preco.getId(),
                unidade.getId(),
                new BigDecimal("2.000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "Item da empresa A"
        ));

        PedidoVendaResponse aprovado = pedidoService.aprovar(pedido.id());
        assertThat(aprovado.status()).isEqualTo(StatusPedidoVenda.APROVADO);
        assertThat(reservaRepository
                .findByOrigemAndOrigemIdAndStatusAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                        MovimentacaoOrigem.VENDA, pedido.id(), StatusReservaEstoque.ATIVA, empresaA.getId()))
                .hasSize(1);

        VendaFaturamentoResponse faturamento = faturamentoService.faturar(new VendaFaturamentoRequest(
                pedido.id(),
                condicao().getId(),
                "VF-CTX-" + codigo(),
                "Conta da venda A",
                "Faturamento contextual"
        ));

        assertThat(faturamento.empresaId()).isEqualTo(empresaA.getId());
        assertThat(estoqueRepository.findById(estoque.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("98.000");
        assertThat(movimentacaoRepository.findByOrigemAndOrigemIdAndEmpresaIdOrderByDataMovimentacaoDesc(
                MovimentacaoOrigem.VENDA, pedido.id(), empresaA.getId()))
                .anyMatch(movimento -> movimento.getTipoMovimentacao() == TipoMovimentacaoEstoque.BAIXA_RESERVA);
        assertThat(contaReceberRepository
                .findByVendaFaturamentoIdAndAtivoTrueAndDeletedAtIsNull(faturamento.id()))
                .isPresent()
                .get()
                .satisfies(conta -> assertThat(conta.getEmpresa().getId()).isEqualTo(empresaA.getId()));

        empresaContext.ativarEmpresa(usuarioId, empresaB.getId());

        assertThatThrownBy(() -> pedidoService.buscar(pedido.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> faturamentoService.buscar(faturamento.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(pedidoService.listarComFiltros(
                null, null, null, null, null, null, null, true, PageRequest.of(0, 20)))
                .isEmpty();
        assertThat(faturamentoService.listarComFiltros(
                null, null, null, null, null, true, PageRequest.of(0, 20)))
                .isEmpty();
    }

    @AfterEach
    @SuppressWarnings("unused")
    void limparContexto() {
        empresaContext.limpar();
    }

    private Empresa empresa(String sufixo) {
        Empresa empresa = new Empresa();
        String documento = cnpj();
        empresa.atualizar("Empresa Venda Contexto " + sufixo + " " + documento,
                "Empresa Venda " + sufixo, documento, "ISENTO", null,
                "NORMAL", 3, "71999990000", "empresa-" + documento + "@teste.com");
        return empresaRepository.saveAndFlush(empresa);
    }

    private Cliente cliente() {
        String documento = cnpj();
        Pessoa pessoa = new Pessoa();
        pessoa.atualizar(TipoPessoa.JURIDICA, "Cliente Contexto " + documento, "Cliente Contexto",
                documento, "ISENTO", "7133334444", null, "cliente-" + documento + "@teste.com", null);
        pessoa = pessoaRepository.saveAndFlush(pessoa);
        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.atualizar(BigDecimal.ZERO, null, null, "Cliente contexto");
        return clienteRepository.saveAndFlush(cliente);
    }

    private Produto produto() {
        Categoria categoria = new Categoria();
        categoria.atualizar(null, "Categoria Contexto " + codigo(), "Teste", 0);
        categoria = categoriaRepository.saveAndFlush(categoria);
        UnidadeMedida unidade = new UnidadeMedida();
        unidade.atualizar("C" + codigo().substring(0, 5), "Unidade contexto", (short) 0);
        unidade = unidadeRepository.saveAndFlush(unidade);
        Produto produto = new Produto();
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-CTX-" + codigo(), null, null, null, "Produto Contexto", "Teste",
                TipoProduto.PRODUTO, NaturezaProduto.AMBOS, StatusProduto.ATIVO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true);
        return produtoRepository.saveAndFlush(produto);
    }

    private ProdutoPreco preco(Empresa empresa, Produto produto) {
        ProdutoPreco preco = new ProdutoPreco();
        preco.setEmpresa(empresa);
        preco.setProduto(produto);
        preco.atualizar(TipoPrecoProduto.VENDA, new BigDecimal("25.00"),
                LocalDate.of(2026, 7, 1), null, true, "Preco contextual");
        return precoRepository.saveAndFlush(preco);
    }

    private ProdutoUnidade produtoUnidade(Produto produto) {
        ProdutoUnidade unidade = new ProdutoUnidade();
        unidade.setProduto(produto);
        unidade.setUnidadeMedida(produto.getUnidadeMedida());
        unidade.atualizar(BigDecimal.ONE, true, false, true, true, false, "Unidade contextual");
        return produtoUnidadeRepository.saveAndFlush(unidade);
    }

    private Estoque estoque(Empresa empresa, Produto produto) {
        Deposito deposito = new Deposito();
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-CTX-" + codigo(), "Deposito Contexto", "Teste", TipoDeposito.CENTRAL,
                true, true, true, true, true, false);
        deposito = depositoRepository.saveAndFlush(deposito);
        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setDeposito(deposito);
        localizacao.atualizar("LOC-CTX-" + codigo(), "Localizacao Contexto", "A", "1", "1", "1", "1",
                true, false, "Teste");
        localizacao = localizacaoRepository.saveAndFlush(localizacao);
        Estoque estoque = new Estoque();
        estoque.setEmpresa(empresa);
        estoque.setProduto(produto);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(new BigDecimal("100.000"), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);
        return estoqueRepository.saveAndFlush(estoque);
    }

    private CondicaoPagamento condicao() {
        CondicaoPagamento condicao = new CondicaoPagamento();
        condicao.atualizar("AV-CTX-" + codigo(), "A vista contexto", BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, RegraVencimento.MANTER_DATA, true);
        CondicaoPagamentoParcela parcela = new CondicaoPagamentoParcela();
        parcela.atualizar(condicao, 1, 0, new BigDecimal("100.0000"));
        condicao.substituirParcelas(java.util.List.of(parcela));
        return condicaoRepository.saveAndFlush(condicao);
    }

    private String codigo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String cnpj() {
        return "62" + "%012d".formatted(Math.abs(UUID.randomUUID().getMostSignificantBits())
                % 1_000_000_000_000L);
    }
}
