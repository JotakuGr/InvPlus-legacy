package br.com.invplus.modules.vendas.entity;

import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.entity.ProdutoPreco;
import br.com.invplus.modules.produtos.entity.ProdutoUnidade;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Produto vendido dentro de um pedido, com preco e unidade congelados para auditoria comercial.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pedido_venda_item",
        indexes = {
                @Index(name = "idx_pedido_venda_item_pedido", columnList = "pedido_venda_id"),
                @Index(name = "idx_pedido_venda_item_produto", columnList = "produto_id"),
                @Index(name = "idx_pedido_venda_item_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class PedidoVendaItem extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_venda_id", nullable = false)
    private PedidoVenda pedidoVenda;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_preco_id", nullable = false)
    private ProdutoPreco produtoPreco;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_unidade_id", nullable = false)
    private ProdutoUnidade produtoUnidade;

    @NotNull
    @DecimalMin(value = "0.000001")
    @Column(name = "quantidade", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal valorUnitario = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "acrescimo", nullable = false, precision = 15, scale = 2)
    private BigDecimal acrescimo = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Recalcula o total usando o preco resolvido pelo backend, nunca por valor enviado pelo frontend.
     */
    public void atualizar(BigDecimal quantidade, BigDecimal valorUnitario,
                          BigDecimal desconto, BigDecimal acrescimo, String observacao) {
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario == null ? BigDecimal.ZERO : valorUnitario;
        this.desconto = desconto == null ? BigDecimal.ZERO : desconto;
        this.acrescimo = acrescimo == null ? BigDecimal.ZERO : acrescimo;
        BigDecimal subtotal = this.quantidade.multiply(this.valorUnitario);
        this.valorTotal = subtotal.subtract(this.desconto).add(this.acrescimo).setScale(2, RoundingMode.HALF_UP);
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }

    public BigDecimal subtotal() {
        return quantidade.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
    }
}
