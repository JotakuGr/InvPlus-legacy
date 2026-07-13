package br.com.invplus.shared.domain.service;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.entity.MovimentacaoEstoque;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import br.com.invplus.modules.estoque.service.MovimentacaoEstoqueService;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;

/**
 * Testes unitarios do ponto unico de alteracao de saldos e movimentacoes.
 */
@ExtendWith(MockitoExtension.class)
class EstoqueDomainServiceTest {

    @Mock
    private MovimentacaoEstoqueService movimentacaoService;

    private EstoqueDomainService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new EstoqueDomainService(movimentacaoService);
    }

    @Test
    void deveRegistrarEntradaEAtualizarUltimoMovimento() {
        Estoque estoque = estoque("10.000");
        UUID movimentacaoId = UUID.randomUUID();
        when(movimentacaoService.registrarInterna(eq(estoque), eq(TipoMovimentacaoEstoque.ENTRADA),
                any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class),
                eq(MovimentacaoOrigem.COMPRA), any(UUID.class), eq("NF-1"), eq("Entrada por compra")))
                .thenReturn(movimentacao(movimentacaoId));

        service.entrada(estoque, bd("5.000"), MovimentacaoOrigem.COMPRA, UUID.randomUUID(), "NF-1", "Entrada por compra");

        assertThat(estoque.getQuantidade()).isEqualByComparingTo("15.000");
        assertThat(estoque.getQuantidadeDisponivel()).isEqualByComparingTo("15.000");
        assertThat(estoque.getUltimoMovimentoId()).isEqualTo(movimentacaoId);
        assertThat(estoque.getUltimoMovimentoEm()).isNotNull();
    }

    @Test
    void deveReservarCancelarEBaixarMantendoDisponivelSincronizado() {
        Estoque estoque = estoque("20.000");
        when(movimentacaoService.registrarInterna(eq(estoque), any(TipoMovimentacaoEstoque.class),
                any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class),
                eq(MovimentacaoOrigem.RESERVA), any(UUID.class), any(), any()))
                .thenReturn(movimentacao(UUID.randomUUID()));

        service.reservar(estoque, bd("6.000"), UUID.randomUUID(), "PV-1", "Reserva");
        assertThat(estoque.getQuantidade()).isEqualByComparingTo("20.000");
        assertThat(estoque.getQuantidadeReservada()).isEqualByComparingTo("6.000");
        assertThat(estoque.getQuantidadeDisponivel()).isEqualByComparingTo("14.000");

        service.cancelarReserva(estoque, bd("2.000"), UUID.randomUUID(), "PV-1", "Cancelamento parcial");
        assertThat(estoque.getQuantidadeReservada()).isEqualByComparingTo("4.000");
        assertThat(estoque.getQuantidadeDisponivel()).isEqualByComparingTo("16.000");

        service.baixarReserva(estoque, bd("4.000"), UUID.randomUUID(), "PV-1", "Baixa");
        assertThat(estoque.getQuantidade()).isEqualByComparingTo("16.000");
        assertThat(estoque.getQuantidadeReservada()).isEqualByComparingTo("0.000");
        assertThat(estoque.getQuantidadeDisponivel()).isEqualByComparingTo("16.000");
    }

    @Test
    void naoDeveRegistrarAjusteQuandoSaldoNaoMudou() {
        Estoque estoque = estoque("8.000");

        service.ajuste(estoque, bd("8.000"), MovimentacaoOrigem.API, estoque.getId(), null, "Sem alteracao");

        verify(movimentacaoService, never()).registrarInterna(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private MovimentacaoEstoque movimentacao(UUID id) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setId(id);
        return movimentacao;
    }

    private Estoque estoque(String quantidade) {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.atualizar("Empresa Estoque", "Empresa", "12345678000199", "ISENTO", null, "NORMAL", 3,
                "71999990000", "empresa@teste.com");

        Deposito deposito = new Deposito();
        deposito.setId(UUID.randomUUID());
        deposito.setEmpresa(empresa);
        deposito.atualizar("DEP-01", "Deposito", "Teste", TipoDeposito.CENTRAL,
                false, true, true, true, true, false);

        LocalizacaoEstoque localizacao = new LocalizacaoEstoque();
        localizacao.setId(UUID.randomUUID());
        localizacao.setDeposito(deposito);
        localizacao.atualizar("A-01", "Rua A", "Rua A", "03", "02", "04", "15", false, false, "Teste");

        Estoque estoque = new Estoque();
        estoque.setId(UUID.randomUUID());
        estoque.setProduto(produto());
        estoque.setEmpresa(empresa);
        estoque.setDeposito(deposito);
        estoque.setLocalizacao(localizacao);
        estoque.atualizarSaldos(bd(quantidade), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null, null, null);
        return estoque;
    }

    private Produto produto() {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.atualizar(null, "Categoria", "Teste", 0);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setId(UUID.randomUUID());
        unidade.atualizar("UN", "Unidade", (short) 0);

        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(unidade);
        produto.atualizar("PROD-01", null, null, null, "Produto", "Teste", TipoProduto.PRODUTO,
                NaturezaProduto.AMBOS, StatusProduto.ATIVO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                false, false, false, true, true, true);
        return produto;
    }

    private BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }
}
