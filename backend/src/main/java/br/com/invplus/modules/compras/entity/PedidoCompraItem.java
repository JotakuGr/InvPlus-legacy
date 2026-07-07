package br.com.invplus.modules.compras.entity;

import br.com.invplus.modules.produtos.entity.Produto;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Produto solicitado dentro de um pedido de compra.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pedido_compra_item",
        indexes = {
                @Index(name = "idx_pedido_compra_item_pedido", columnList = "pedido_compra_id"),
                @Index(name = "idx_pedido_compra_item_produto", columnList = "produto_id")
        }
)
@NoArgsConstructor
public class PedidoCompraItem extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_compra_id", nullable = false)
    private PedidoCompra pedidoCompra;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_unidade_id")
    private ProdutoUnidade produtoUnidade;

    @NotNull
    @DecimalMin(value = "0.000001")
    @Column(name = "quantidade", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorUnitario = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza quantidade e valores recalculando o total do item.
     */
    public void atualizar(BigDecimal quantidade, BigDecimal valorUnitario, String observacao) {
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario == null ? BigDecimal.ZERO : valorUnitario;
        this.valorTotal = this.quantidade.multiply(this.valorUnitario);
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }
}
