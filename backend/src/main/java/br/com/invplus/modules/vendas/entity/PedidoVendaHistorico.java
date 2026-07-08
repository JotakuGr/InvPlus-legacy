package br.com.invplus.modules.vendas.entity;

import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.shared.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Historico imutavel das transicoes de status do pedido de venda.
 */
@Getter
@Setter
@Entity
@Table(
        name = "pedido_venda_historico",
        indexes = @Index(name = "idx_pedido_venda_historico_pedido", columnList = "pedido_venda_id,criado_em")
)
@NoArgsConstructor
public class PedidoVendaHistorico extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_venda_id", nullable = false)
    private PedidoVenda pedidoVenda;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusPedidoVenda statusAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 30)
    private StatusPedidoVenda statusNovo;

    @Column(name = "observacao")
    private String observacao;
}
