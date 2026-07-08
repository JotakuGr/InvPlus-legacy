package br.com.invplus.modules.vendas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;

import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import br.com.invplus.modules.produtos.repository.ProdutoPrecoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoUnidadeRepository;
import br.com.invplus.modules.vendas.dto.request.PedidoVendaItemRequest;
import br.com.invplus.modules.vendas.dto.response.PedidoVendaItemResponse;
import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.PedidoVendaItem;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.mapper.PedidoVendaItemMapper;
import br.com.invplus.modules.vendas.repository.PedidoVendaItemRepository;
import br.com.invplus.modules.vendas.repository.PedidoVendaRepository;
import br.com.invplus.modules.vendas.validator.PedidoVendaItemValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Testes unitarios das regras de item do pedido de venda.
 */
@ExtendWith(MockitoExtension.class)
class PedidoVendaItemServiceTest {

    @Mock
    private PedidoVendaItemRepository repository;

    @Mock
    private PedidoVendaRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoPrecoRepository produtoPrecoRepository;

    @Mock
    private ProdutoUnidadeRepository produtoUnidadeRepository;

    @Mock
    private PedidoVendaService pedidoVendaService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PedidoVendaItemService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new PedidoVendaItemService(
                repository,
                new PedidoVendaItemMapper(),
                new PedidoVendaItemValidator(),
                pedidoRepository,
                produtoRepository,
                produtoPrecoRepository,
                produtoUnidadeRepository,
                pedidoVendaService,
                eventPublisher
        );
    }

    /**
     * Cria item buscando o preco no ProdutoPreco e recalculando total no backend.
     */
    @Test
    void deveCriarItemComPrecoDoProdutoPreco() {
        var fixture = fixture(StatusPedidoVenda.ABERTO);
        configurarBusca(fixture);
        when(repository.save(any(PedidoVendaItem.class))).thenAnswer(invocation -> {
            PedidoVendaItem item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });

        PedidoVendaItemResponse response = service.criar(request(fixture, BigDecimal.valueOf(3), BigDecimal.ONE, BigDecimal.valueOf(2)));

        assertThat(response.precoUtilizado()).isEqualByComparingTo("10.5000");
        assertThat(response.subtotal()).isEqualByComparingTo("31.50");
        assertThat(response.valorTotal()).isEqualByComparingTo("32.50");
        verify(pedidoVendaService).recalcularTotal(fixture.pedido());
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void deveAtualizarItemERecalcularPedido() {
        var fixture = fixture(StatusPedidoVenda.ABERTO);
        PedidoVendaItem item = item(fixture);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(item.getId())).thenReturn(Optional.of(item));
        configurarBusca(fixture);

        PedidoVendaItemResponse response = service.atualizar(item.getId(),
                request(fixture, BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ZERO));

        assertThat(response.valorTotal()).isEqualByComparingTo("21.00");
        verify(pedidoVendaService).recalcularTotal(fixture.pedido());
    }

    @Test
    void deveExcluirLogicamenteERecalcularPedido() {
        var fixture = fixture(StatusPedidoVenda.ABERTO);
        PedidoVendaItem item = item(fixture);
        when(repository.findByIdAndAtivoTrueAndDeletedAtIsNull(item.getId())).thenReturn(Optional.of(item));

        service.excluir(item.getId());

        assertThat(item.getAtivo()).isFalse();
        assertThat(item.getDeletedAt()).isNotNull();
        verify(pedidoVendaService).recalcularTotal(fixture.pedido());
    }

    @Test
    void deveBloquearProdutoInexistente() {
        var fixture = fixture(StatusPedidoVenda.ABERTO);
        when(pedidoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fixture.pedido().getId()))
                .thenReturn(Optional.of(fixture.pedido()));
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fixture.produto().getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request(fixture, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto nao encontrado");
    }

    @Test
    void deveBloquearPedidoCancelado() {
        var fixture = fixture(StatusPedidoVenda.CANCELADO);
        configurarBusca(fixture);

        assertThatThrownBy(() -> service.criar(request(fixture, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Pedido cancelado ou faturado nao pode ser alterado");
    }

    @Test
    void deveBloquearPrecoDeOutroProduto() {
        var fixture = fixture(StatusPedidoVenda.ABERTO);
        Produto outroProduto = produto("OUTRO");
        fixture.preco().setProduto(outroProduto);
        configurarBusca(fixture);

        assertThatThrownBy(() -> service.criar(request(fixture, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ProdutoPreco nao pertence");
    }

    @Test
    void deveBloquearUnidadeDeOutroProduto() {
        var fixture = fixture(StatusPedidoVenda.ABERTO);
        Produto outroProduto = produto("OUTRO");
        fixture.unidade().setProduto(outroProduto);
        configurarBusca(fixture);

        assertThatThrownBy(() -> service.criar(request(fixture, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ProdutoUnidade nao pertence");
    }

    @Test
    void deveBloquearPrecoForaDaVigencia() {
        var fixture = fixture(StatusPedidoVenda.ABERTO);
        fixture.preco().setVigenciaInicio(LocalDate.of(2026, 8, 1));
        configurarBusca(fixture);

        assertThatThrownBy(() -> service.criar(request(fixture, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("preco vigente");
    }

    @Test
    void deveBloquearTotalNegativo() {
        var fixture = fixture(StatusPedidoVenda.ABERTO);
        configurarBusca(fixture);

        assertThatThrownBy(() -> service.criar(request(fixture, BigDecimal.ONE, BigDecimal.valueOf(99), BigDecimal.ZERO)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Valor total do item nao pode ser negativo");
    }

    @SuppressWarnings("unchecked")
    @Test
    void deveListarComFiltrosPaginado() {
        when(repository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.listarComFiltros(UUID.randomUUID(), UUID.randomUUID(), true, PageRequest.of(0, 20));

        verify(repository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class));
    }

    private void configurarBusca(Fixture fixture) {
        when(pedidoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fixture.pedido().getId()))
                .thenReturn(Optional.of(fixture.pedido()));
        when(produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fixture.produto().getId()))
                .thenReturn(Optional.of(fixture.produto()));
        when(produtoPrecoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fixture.preco().getId()))
                .thenReturn(Optional.of(fixture.preco()));
        when(produtoUnidadeRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(fixture.unidade().getId()))
                .thenReturn(Optional.of(fixture.unidade()));
    }

    private PedidoVendaItemRequest request(Fixture fixture, BigDecimal quantidade, BigDecimal desconto, BigDecimal acrescimo) {
        return new PedidoVendaItemRequest(
                fixture.pedido().getId(),
                fixture.produto().getId(),
                fixture.preco().getId(),
                fixture.unidade().getId(),
                quantidade,
                desconto,
                acrescimo,
                "Item de venda"
        );
    }

    private PedidoVendaItem item(Fixture fixture) {
        PedidoVendaItem item = new PedidoVendaItem();
        item.setId(UUID.randomUUID());
        item.setPedidoVenda(fixture.pedido());
        item.setProduto(fixture.produto());
        item.setProdutoPreco(fixture.preco());
        item.setProdutoUnidade(fixture.unidade());
        item.atualizar(BigDecimal.ONE, fixture.preco().getPreco(), BigDecimal.ZERO, BigDecimal.ZERO, "Item");
        return item;
    }

    private Fixture fixture(StatusPedidoVenda status) {
        Produto produto = produto("PROD-VENDA");
        PedidoVenda pedido = new PedidoVenda();
        pedido.setId(UUID.randomUUID());
        pedido.setStatus(status);
        pedido.setDataEmissao(LocalDate.of(2026, 7, 7));

        ProdutoPreco preco = new ProdutoPreco();
        preco.setId(UUID.randomUUID());
        preco.setProduto(produto);
        preco.atualizar(TipoPrecoProduto.VENDA, BigDecimal.valueOf(10.5000), LocalDate.of(2026, 7, 1),
                null, true, "Preco venda");

        ProdutoUnidade unidade = new ProdutoUnidade();
        unidade.setId(UUID.randomUUID());
        unidade.setProduto(produto);
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setId(UUID.randomUUID());
        unidadeMedida.atualizar("UN", "Unidade", (short) 0);
        unidade.setUnidadeMedida(unidadeMedida);
        unidade.atualizar(BigDecimal.ONE, true, false, true, true, false, "Unidade venda");
        return new Fixture(pedido, produto, preco, unidade);
    }

    private Produto produto(String codigo) {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCodigo(codigo);
        produto.setNome("Produto Venda");
        produto.setDescricao("Descricao venda");
        return produto;
    }

    private record Fixture(PedidoVenda pedido, Produto produto, ProdutoPreco preco, ProdutoUnidade unidade) {
    }
}
